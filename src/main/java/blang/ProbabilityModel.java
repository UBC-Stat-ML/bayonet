package blang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.plaf.ListUI;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fig.basic.ListUtils;

import bayonet.graphs.GraphUtils;
import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.annotations.DefineFactor;
import blang.factors.Factor;
import briefj.BriefLists;
import briefj.BriefStrings;
import briefj.ReflexionUtils;


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
  private final UndirectedGraph<Node,?> graph = GraphUtils.newUndirectedGraph();
  
  private final Set<Node> 
    stochasticVariables = Sets.newLinkedHashSet(),
    factors = Sets.newLinkedHashSet(),
    observedVariables = Sets.newLinkedHashSet();
  
  private final VariableNames variableNames = new VariableNames();
  
  public ProbabilityModel(Object specification)
  {
    parse(specification, rootFieldPath);
  }
  
  public ProbabilityModel() {}
  
  public ProbabilityModel deepCopy()
  {
    Kryo kryo = new Kryo();
    ProbabilityModel copy = kryo.copy(this);
    return copy;
  }
  
  public void parse(Object specification)
  {
    parse(specification, rootFieldPath);
  }
  
  public void addFactor(Factor f, FieldPath fieldsPath)
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
  
  public void setObserved(Object variable)
  {
    observedVariables.add(variableNode(variable));
  }
  
  public void setVariablesInFactorAsObserved(Factor factor)
  {
    for (Pair<ProbabilityModel.FieldPath, Object> pair : listArguments(factor, ProbabilityModel.rootFieldPath))
    {
      Field field = pair.getLeft().getLastField();
      FactorArgument annotation = field.getAnnotation(FactorArgument.class);
      if (annotation.makeStochastic())
        setObserved(ReflexionUtils.getFieldValue(field, pair.getRight()));
    }
  }
  
  public int nObservedNodes()
  {
    return observedVariables.size();
  }

  
  @SuppressWarnings("unchecked")
  public <T> List<T> getLatentVariables(Class<T> ofType)
  {
    List<T> result = Lists.newArrayList();
    for (Object variable : getLatentVariables())
      if (ofType.isAssignableFrom(variable.getClass())) // i.e. if variable extends/implements ofType
        result.add((T) variable);
    return result;
  }
  
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
      ensureVariableAdded(variable, fieldsPath.extendBy(argumentField)); 
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
        
      addFactor((Factor) toAdd, fieldsPath.extendBy(field));
      
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
      result.add(comp.getName() + " = " + componentInstance.getClass().getSimpleName() + "(\n" +
          BriefStrings.indent(argumentsAndComponentsToString(componentInstance)) + "\n)");
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
      return variableNode.getPayload().toString();
  }
  
  private static Node factorNode(Factor f) 
  {
    return new Node(f, true);
  }
  
  private static Node variableNode(Object variable)
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
  private static class Node
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
  }
  
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
    private final Map<Node,String> shortNames = Maps.newHashMap();
    private final Map<Node,FieldPath> longNames = Maps.newHashMap();
    private final Set<String> 
      allShortNames = Sets.newHashSet(),
      collisions = Sets.newHashSet();   
    
    public void add(Node variable, FieldPath fieldsPath)
    {
      if (variable.isFactor())
        throw new RuntimeException();
      String shortName = fieldsPath.getLastField().getName();
      if (allShortNames.contains(shortName))
        collisions.add(shortName);
      allShortNames.add(shortName);
      shortNames.put(variable, shortName);
      longNames.put(variable, fieldsPath);
    }
    
    public String get(Node variable)
    {
      String shortName = shortNames.get(variable);
      if (collisions.contains(shortName))
        return longNames.get(variable).toString();
      else
        return shortName;
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
