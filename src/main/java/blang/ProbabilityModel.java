package blang;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.StringNameProvider;

import bayonet.graphs.GraphUtils;
import blang.MCMCFactory.Factories;
import blang.MCMCFactory.MCMCOptions;
import blang.annotations.DefineFactor;
import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.Factor;
import briefj.BriefLists;
import briefj.BriefStrings;
import briefj.ReflexionUtils;

//import com.esotericsoftware.kryo.Kryo;
//import com.esotericsoftware.kryo.Registration;
//import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rits.cloning.Cloner;


/**
 * Holds a factor graph. Factors are instance of Factor, variables are arbitrary
 * objects. 
 * 
 * Also contains facilities to discover the structure of a Factor graph using
 * reflexion and annotations.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class ProbabilityModel
{
  
  /*
     TODO: RandomVariable<T> instead of VariableNode
   */
  
  private final UndirectedGraph<Node,?> graph = GraphUtils.newUndirectedGraph();
  
  private final Set<Node> 
    stochasticVariables = Sets.newLinkedHashSet(),
    factors = Sets.newLinkedHashSet(),
    observedVariables = Sets.newLinkedHashSet();
  
  private final Map<String,Node> variableName2Object;
  
  private final VariableNames variableNames = new VariableNames();
  
  public ProbabilityModel(Object specification)
  {
    parse(specification, rootFieldPath);
    variableName2Object = createVariableNameInverseMap(variableNames);
  }


  public static ProbabilityModel parse(Object modelSpecification)
  {
    return new ProbabilityModel(modelSpecification);
  }
  
  public static ProbabilityModel parse(Object modelSpecification, boolean clone)
  {
    if (clone)
      return cloneAndParse(modelSpecification);
    else
      return new ProbabilityModel(modelSpecification);
  }
  
  public static ProbabilityModel cloneAndParse(Object specification)
  {
    /*
     Note: we are not using Kryo because it requires no arg constructors
           deep clone library (https://code.google.com/p/cloning/wiki/Usage) supports it through objenesis (http:objenesis.org/)
    
     Note: this issue is due to a limitation of the reflection API. objenesis is a workaround hack not fully JVM indep
    
     Addendum: actually, might need kryo/xstream anyways for check points/reload? deep clone lib does not do this
               see https:github.com/eishay/jvm-serializers/wiki for speed comparison of different methods
    
     Problem with kryo: dumps are binary. some fast JSON are attractive but JSON does have limitations. probably too naive
     use binary with good tooling (main issue is versioning)
    
     Kryo version:
    
          Kryo kryo = new Kryo();
          FieldSerializer<ModelRunner> fs = new FieldSerializer<ModelRunner>(kryo,ModelRunner.class); 
          for (Field f : ReflexionUtils.getDeclaredFields(ModelRunner.class, true))
            fs.removeField(f.getName());
           Use your specific serializer instance with an excluded field for the Bla class
          kryo.register(ModelRunner.class, fs); 
          Object clonedSpec = kryo.copy(specification);
          return new ProbabilityModel(clonedSpec);
    */
    
    Cloner cloner = new Cloner();
    // avoid infinite loops if used in conjunction with the ModelRunner
    cloner.setNullTransient(true);
    cloner.nullInsteadOfClone(MCMCOptions.class);
    cloner.nullInsteadOfClone(Factories.class);
    Object clonedSpec = cloner.deepClone(specification);
    // use the standard parser on the cloned specifications
    return new ProbabilityModel(clonedSpec);
  }
  
  private void addFactor(Factor f, FieldPath fieldsPath)
  {
    Node factorNode = factorNode(f);
    if (factors.contains(factorNode))
      throw new RuntimeException("Trying to insert the same factor twice: " + fieldsPath);
    factors.add(factorNode);
    graph.addVertex(factorNode);
    addLinks(f, fieldsPath);
    if (Graphs.neighborListOf(graph, factorNode(f)).isEmpty())
      throw new RuntimeException("Problem in " + fieldsPath + ": a factor should contain at least one FactorArgument " +
      		"defining a random variable, or at least one FactorComponent recursively satisfying that property. " +
      		"See FactorArgument.makeStochastic()");
  }
  
  public double logDensity()
  {
    double sum = 0.0;
    for (Factor f : allFactors())
      sum += f.logDensity();
    return sum;
  }

  public String getName(Object variable)
  {
    return variableNames.get(variableNode(variable));
  }
  
  public List<Object> getLatentVariables()
  {
    List<Object> result = Lists.newArrayList();
    for (Node n : stochasticVariables)
      if (!observedVariables.contains(n))
        result.add(n.getAsVariable());
    return result;
  }
  
//  public List<RandomVariable<?>> getLatentRandomVariables()
//  {
//    List<RandomVariable<?>> result = Lists.newArrayList();
//    for (Node n : stochasticVariables)
//      if (!observedVariables.contains(n))
//        result.add(n);
//    return result;
//  }
  
  public List<String> getLatentVariableNames()
  {
    List<String> result = Lists.newArrayList();
    for (Object o : getLatentVariables())
      result.add(getName(o));
    return result;
  }
  
  public Object getVariableWithName(String string)
  {
    return variableName2Object.get(string).getAsVariable();
  }
  
  private void setObserved(Object variable)
  {
    observedVariables.add(variableNode(variable));
  }
  
  private void setVariablesInFactorAsObserved(Factor factor)
  {
    for (Pair<ProbabilityModel.FieldPath, Object> pair : listArguments(factor, ProbabilityModel.rootFieldPath))
    {
      Field field = pair.getLeft().getLastField();
      FactorArgument annotation = field.getAnnotation(FactorArgument.class);
      if (annotation.makeStochastic())
        setObserved(pair.getRight());
    }
  }
  
  public int nObservedNodes()
  {
    return observedVariables.size();
  }

  public <T> List<T> getLatentVariables(Class<T> ofType)
  {
    return ReflexionUtils.sublistOfGivenType(getLatentVariables(), ofType);
  }
  
//  public <T> List<RandomVariable<T>> getLatentRandomVariables(Class<T> ofType)
//  {
//    List<RandomVariable<T>> result = Lists.newArrayList();
//    for (RandomVariable rv : getLatentRandomVariables())
//      if (ofType.isAssignableFrom(rv.getRealization().getClass())) 
//        result.add(rv);
//    return result;
//  }
  
  public List<Factor> linearizedFactors()
  {
    DirectedGraph<Node, ?> directedGraph = GraphUtils.newDirectedGraph();
    for (Node factorNode : factors)
    {
      directedGraph.addVertex(factorNode);
      boolean outgoingEdgeFound = false;
      for (Pair<FieldPath,Object> argument : listArguments(factorNode.getAsFactor(), rootFieldPath))
      {
        Node variableNode = variableNode(argument.getRight());
        directedGraph.addVertex(variableNode);
        // direction depends on the annotation
        FactorArgument annotation = argument.getLeft().getLastField().getAnnotation(FactorArgument.class);
        if (annotation.makeStochastic())
        {
          directedGraph.addEdge(factorNode, variableNode);
          outgoingEdgeFound = true;
        }
        else
          directedGraph.addEdge(variableNode, factorNode);
      }
      if (!outgoingEdgeFound)
        throw new RuntimeException("Linearization assumes that each Factor should have at least one " + 
            FactorArgument.class.getSimpleName() + " with makeStochastic set to true: " + factorNode.getClass());
    }
    List<Node> linearizedNodes = GraphUtils.linearization(directedGraph);
    List<Factor> result = Lists.newArrayList();
    for (Node node : linearizedNodes)
      if (node.isFactor())
        result.add(node.getAsFactor());
    return result;
  }
  
  public class BlangStringProvider extends StringNameProvider<Node>
  {
    
    @Override
    public String getVertexName(Node vertex)
    {
        String name = variableNames.get(vertex);
        if (name == "")
            name = vertex.toString();
        return "<" + name + ">"; 
        
    }
      
  }
    
  @SuppressWarnings({ "unchecked", "rawtypes" })
  /**
   * Generates a .dot file for the composite factor graph
   * saves to the provided file 
   * @param file
   */
  public void printGraph(File file)
  {
      BlangStringProvider p = new BlangStringProvider();
      DOTExporter export = new DOTExporter(p, p,null, null, null);
      try {
          export.export(new FileWriter(file), graph);
      }catch (IOException e){}
  }
  
  public List<Factor> neighborFactors(Object variable)
  {
    List<Factor> result = Lists.newArrayList();
    for (Node n : Graphs.neighborListOf(graph, variableNode(variable)))
      result.add(n.getAsFactor());
    return result;
  }
  
  public List<Object> neighborVariables(Factor f)
  {
    List<Object> result = Lists.newArrayList();
    for (Node n : Graphs.neighborListOf(graph, factorNode(f)))
      result.add(n.getAsVariable());
    return result;
  }
  
  public String toString(Factor f)
  {
    return f.getClass().getSimpleName() + "(\n" + BriefStrings.indent(argumentsAndComponentsToString(f)) + "\n)"; //+ Joiner.on(", ").join(assignments) + ")";
  }
  
  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    for (Factor f : allFactors())
      result.append(toString(f) + "\n");
    return result.toString();
  }
  
  // private static utilities
  
  private void addLinks(Factor f, FieldPath fieldsPath)
  {
    for (Pair<FieldPath, Object> argument : listArguments(f, fieldsPath))
    {
      Field argumentField = argument.getLeft().getLastField();
      Object variable = argument.getRight();
      if (argumentField.getAnnotation(FactorArgument.class).makeStochastic())
        stochasticVariables.add(variableNode(variable));
      ensureVariableAdded(variable, argument.getLeft());//fieldsPath.extendBy(argumentField)); 
      graph.addEdge(variableNode(variable), factorNode(f));
    }
  }
  
  private List<Pair<FieldPath, Object>> listArguments(Factor f, FieldPath fieldPath)
  {
    List<Pair<FieldPath, Object>> result = Lists.newArrayList();
    listArguments(f, fieldPath, result);
    return result;
  }
  
  private void listArguments(Object o, FieldPath fieldPath,
      List<Pair<FieldPath, Object>> result)
  {
    for (Field argumentField : ReflexionUtils.getAnnotatedDeclaredFields(o.getClass(), FactorArgument.class, true))
    {
      if (!Modifier.isFinal(argumentField.getModifiers()))
        throw new RuntimeException("Fields annotated with FactorArgument should be final.");
      Object variable = ReflexionUtils.getFieldValue(argumentField, o); 
      if (variable != null)
        result.add(Pair.of(fieldPath.extendBy(argumentField), variable));
    }
    for (Field componentField : ReflexionUtils.getAnnotatedDeclaredFields(o.getClass(), FactorComponent.class, true))
    {
      if (!Modifier.isFinal(componentField.getModifiers()))
        throw new RuntimeException("Fields annotated with FactorComponent should be final.");
      Object value = ReflexionUtils.getFieldValue(componentField, o);
      if (value != null)
        listArguments(value, fieldPath.extendBy(componentField), result);
    }
  }

  private void ensureVariableAdded(Object variable, FieldPath fieldsPath)
  {
    if (!graph.containsVertex(variableNode(variable))) 
    {
      graph.addVertex(variableNode(variable));
      variableNames.add(variableNode(variable), fieldsPath); 
    }
  }
  
  private void parse(Object o, FieldPath fieldsPath) 
  {
    for (Field field : ReflexionUtils.getAnnotatedDeclaredFields(o.getClass(), DefineFactor.class, true))
    {
      Object toAdd = ReflexionUtils.getFieldValue(field, o);
      if (toAdd == null) continue;
      boolean isFactor = toAdd instanceof Factor;
      if (!isFactor)
        throw new RuntimeException("@Factor annotation only permitted on objects of type of Factor");
        
      Factor factor = (Factor) toAdd;
      addFactor(factor, fieldsPath.extendBy(field));
      if (field.getAnnotation(DefineFactor.class).onObservations())
        setVariablesInFactorAsObserved(factor);
      
      parse(toAdd, fieldsPath.extendBy(field));
    }
  }
  
  private String argumentsAndComponentsToString(Object f)
  {
    List<String> result = Lists.newArrayList();
    for (Field arg : ReflexionUtils.getAnnotatedDeclaredFields(f.getClass(), FactorArgument.class, true))
      result.add(arg.getName() + " = " + variableToString(variableNode(ReflexionUtils.getFieldValue(arg, f))));
    for (Field comp : ReflexionUtils.getAnnotatedDeclaredFields(f.getClass(), FactorComponent.class, true))
    {
      Object componentInstance = ReflexionUtils.getFieldValue(comp, f);
      result.add(comp.getName() + " = " + (componentInstance == null ? "null" : componentInstance.getClass().getSimpleName() + "(\n" +
          BriefStrings.indent(argumentsAndComponentsToString(componentInstance)) + "\n)"));
    }
    return Joiner.on("\n").join(result);
  }

  private String variableToString(Node variableNode)
  {
    if (stochasticVariables.contains(variableNode))
    {
      String result = "${" + variableNames.get(variableNode) + "}";
      if (observedVariables.contains(variableNode))
        result = "observed[" + result + " = " + variableNode.getPayload().toString() + "]";
      return result;
    }
    else
    {
      Object payload = variableNode.getPayload();
      return payload == null ? null : payload.toString();
    }
  }
  
  private Node factorNode(Factor f) 
  {
    return new Node(f, true);
  }
  
  private Node variableNode(Object variable)
  {
    return new Node(variable, false);
  }
  
  private List<Factor> allFactors()
  {
    List<Factor> result = Lists.newArrayList();
    for (Node n : factors)
      result.add(n.getAsFactor());
    return result;
  }
  
  private static Map<String, Node> createVariableNameInverseMap(
      VariableNames variableNames)
  {
    Map<String,Node> result = Maps.newHashMap();
    for (Node n : variableNames.longNames.keySet())
      result.put(variableNames.get(n), n);
    return result;
  }
  
  private static <T> List<T> growList(List<T> old, T newItem)
  {
    List<T> result = Lists.newArrayList(old);
    result.add(newItem);
    return result;
  }
  
  @SuppressWarnings("unchecked")
  private static final FieldPath rootFieldPath = new FieldPath(Collections.EMPTY_LIST);
  
  // private static classes
  
  /**
   * 
   * Contains either a factor or a variable. The main purpose of this union-like 
   * datastructure is to bypass the variable's .equal and .hashCode: we do not
   * want to consider two distinct variables to be the same even if they initially
   * have the same value, because after resampling they might take on different 
   * values.
   * 
   * At the same time, makes encapsulates the type heterogeity of the factor graph's
   * vertices.
   * 
   * Note that in principle this could have been done using identity hash maps, except that this
   * might have required modifying JGraphT's internals.
   * 
   * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
   *
   */
  private static class Node // implements RandomVariable
  {
    private final Object payload;
    private final boolean isFactor;

    private Node(Object payload, boolean isFactor)
    {
      this.payload = payload;
      this.isFactor = isFactor;
    }

    public Factor getAsFactor()
    {
      if (!isFactor)
        throw new RuntimeException();
      return (Factor) getPayload();
    }
    
    public Object getAsVariable()
    {
      if (isFactor)
        throw new RuntimeException();
      return getPayload();
    }
    
    /**
     * Note: it is critical to keep equality/hashCode by
     * identity here
     */
    @Override
    public boolean equals(Object _other)
    {
      Node other = (Node) _other;
      if (other.isFactor != this.isFactor)
        return false;
      return getPayload() == other.getPayload();
    }
    
    /**
     * Note: it is critical to keep equality/hashCode by
     * identity here
     */
    @Override
    public int hashCode()
    {
      return System.identityHashCode(getPayload());
    }

    public boolean isFactor()
    {
      return isFactor;
    }

    public Object getPayload()
    {
      return payload;
    }
    


//    @Override
//    public Object getRealization()
//    {
//      return getAsVariable();
//    }
//    
//    @Override
//    public String toString()
//    {
//      if (isFactor)
//        return payload.toString();
//      else
//        return getName(payload);
//    }
  }
  
//  public static interface RandomVariable<T>
//  {
//    public T getRealization();
//  }
  
  /**
   * Creates unique names for variables. If possible, use only the 
   * name of the first field pointing to that variable (shortName). In case of 
   * conflict (cases where two such names would be identical), back 
   * off to using the names of the sequence of fields (FieldPath) 
   * by which the variable was first discovered, joined by '.' (longName).
   * 
   * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
   *
   */
  private static class VariableNames
  {
    private final Map<Node,FieldPath> longNames = Maps.newHashMap();

    
    public void add(Node variable, FieldPath fieldsPath)
    {
      if (variable.isFactor())
        throw new RuntimeException();
      longNames.put(variable, fieldsPath);
    }
    
    public String get(Node variable)
    {
        if(longNames.containsKey(variable))
                return longNames.get(variable).toString();
        return ""; 
    }
  }
  
  /**
   * FieldPath is used to record the stack of Fields that are recursively
   * visited to discover variables. They are used to create unique names for
   * variables automatically.
   * 
   * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
   *
   */
  private static class FieldPath
  {
    private final List<Field> sequence;
    
    private FieldPath(List<Field> seq) 
    { 
      this.sequence = seq; 
    }

    public FieldPath extendBy(Field field)
    {
      return new FieldPath(growList(this.sequence, field));
    }
    
    public Field getLastField()
    {
      return BriefLists.last(sequence);
    }
    
    @Override
    public String toString()
    {
      List<String> result = Lists.newArrayList();
      for (Field f : sequence)
        result.add(f.getName());
      return Joiner.on(".").join(result);
    }
  }




}
