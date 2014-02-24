package bayonet.blang;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import bayonet.blang.factors.Factor;
import bayonet.graphs.GraphUtils;
import briefj.ReflexionUtils;



/**
 * A semi-automatic MCMC framework focused on extensibility to 
 * rich data structures. 
 * 
 * Difference from previous art:
 * - vs. STAN: STAN is currently limited to continuous r.v., bayonet supports many combinatorial ones
 * - vs. pymc: pymc does not support in-place modification, an important requirement in combinatorial spaces
 * - vs. JAGS: does not support custom data types for the random variables
 * 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class ProbabilityModel
{
  
  // TODO: test case: randomness fixing
  // TODO: test case, several move sets doing the same
  // TODO: test case, test reject proposal by recomputing ll and comparing
  
  // TODO: always wrap variables in a variable object? (for pretty printing, etc) - probably not, more annoying than anything
  // unless can identify clear cases - e.g. when listing nodes without samplers should use class names instead anyways
  
  // TODO: need to clean this up, e.g. parsing should be in separate class
  
  // TODO: maintain list of Fields for each edge (could replace long name at same time)
  
  // TODO: enforce the philosophy:
  // - factors have managed pointers to variables
  // - variables should not have pointers to factors
  // - moves ask the model to reverse the pointers of a given node
  
  // TODO: save:
  // visit variables, create one file for each name (can make test using likelihood)
  // TODO: load:
  // - first, create the graph using the existing model code
  // - the, for each variable:
  //   - load new value using GSON
  //   - create a map oldVar -> newVar
  //   - revisit each factor
  //       - use the map to substitute variables
  
  private final UndirectedGraph<Node,?> graph = GraphUtils.newUndirectedGraph();
  
  private final Set<Node> 
    stochasticVariables = Sets.newLinkedHashSet(),
    factors = Sets.newLinkedHashSet(),
    observedVariables = Sets.newLinkedHashSet();
  
  private final VariableNames variableNames = new VariableNames();
  
  public ProbabilityModel(Object specification)
  {
    parse(specification, "");
  }
  
  private static Node factorNode(Factor f) 
  {
    return new Node(f, true);
  }
  
  private static Node variableNode(Object variable)
  {
    return new Node(variable, false);
  }
  
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
      return (Factor) payload;
    }
    
    public Object getAsVariable()
    {
      if (isFactor)
        throw new RuntimeException();
      return payload;
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
      return payload == other.payload;
    }
    
    /**
     * Note: it is critical to keep equality/hashCode by
     * identity here
     */
    @Override
    public int hashCode()
    {
      return System.identityHashCode(payload);
    }
  }
  
  public void parse(Object o, String prefix) 
  { 
    try {  _parse(o, prefix); } 
    catch (Exception e) { throw new RuntimeException(e); }
  }
  
  public void addFactor(Factor f, String namePrefix)
  {
    Node factorNode = factorNode(f);
    if (factors.contains(factorNode))
      throw new RuntimeException("Trying to insert the same factor twice");
    factors.add(factorNode);
    graph.addVertex(factorNode);
    addLinks(f, f, namePrefix);
    if (Graphs.neighborListOf(graph, factorNode(f)).isEmpty())
      throw new RuntimeException("Problem in " + namePrefix + ": a factor should contain at least one FactorArgument " +
      		"defining a random variable, or at least one FactorComponent recursively satisfying that property. " +
      		"See FactorArgument.makeStochastic()");
  }
  
  public String getName(Object variable)
  {
    return variableNames.get(variableNode(variable));
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List getLatentVariables()
  {
    List result = Lists.newArrayList();
    for (Node n : stochasticVariables)
      if (!observedVariables.contains(n))
        result.add(n.getAsVariable());
    return result;
  }

  
  public void setObserved(Object variable)
  {
    observedVariables.add(variableNode(variable));
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public <T> List<T> getLatentVariables(Class<T> ofType)
  {
    List result = Lists.newArrayList();
    for (Object variable : getLatentVariables())
      if (ofType.isAssignableFrom(variable.getClass())) // i.e. if variable extends/implements ofType
        result.add(variable);
    return result;
  }
  
  private List<Factor> allFactors()
  {
    List<Factor> result = Lists.newArrayList();
    for (Node n : factors)
      result.add(n.getAsFactor());
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
  
  private void addLinks(Factor f, Object o, String namePrefix)
  {
    for (Field argumentField : ReflexionUtils.getAnnotatedDeclaredFields(o.getClass(), FactorArgument.class, true))
    {
      if (!Modifier.isFinal(argumentField.getModifiers()))
        throw new RuntimeException("Fields annotated with FactorArgument should be final.");
      Object variable = ReflexionUtils.getFieldValue(argumentField, o); 
      if (variable != null)
      {
        if (argumentField.getAnnotation(FactorArgument.class).makeStochastic())
          stochasticVariables.add(variableNode(variable));
        ensureVariableAdded(variable, namePrefix, argumentField.getName());
        graph.addEdge(variableNode(variable), factorNode(f));
      }
    }
    for (Field componentField : ReflexionUtils.getAnnotatedDeclaredFields(o.getClass(), FactorComponent.class, true))
    {
      if (!Modifier.isFinal(componentField.getModifiers()))
        throw new RuntimeException("Fields annotated with FactorComponent should be final.");
      Object value = ReflexionUtils.getFieldValue(componentField, o);
      if (value != null)
        addLinks(f, value, growPrefix(namePrefix, componentField.getName()));
    }
  }
  
  private void ensureVariableAdded(Object variable, String prefix, String name)
  {
    if (graph.containsVertex(variableNode(variable))) 
      return;
    graph.addVertex(variableNode(variable));
    variableNames.add(variableNode(variable), name, growPrefix(prefix, name));
  }
  
  private static String growPrefix(String oldPrefix, String toAdd)
  {
    return oldPrefix.isEmpty() ? toAdd : oldPrefix + "." + toAdd;
  }
  
  private static class VariableNames
  {
    private final Map<Node,String> shortNames = Maps.newHashMap();
    private final Map<Node,String> longNames = Maps.newHashMap();
    private final Set<String> 
      allShortNames = Sets.newHashSet(),
      collisions = Sets.newHashSet();   
    
    public void add(Node variable, String shortName, String longName)
    {
      if (variable.isFactor)
        throw new RuntimeException();
      if (allShortNames.contains(shortName))
        collisions.add(shortName);
      allShortNames.add(shortName);
      shortNames.put(variable, shortName);
      longNames.put(variable, longName);
    }
    
    public String get(Node variable)
    {
      String shortName = shortNames.get(variable);
      if (collisions.contains(shortName))
        return longNames.get(variable);
      else
        return shortName;
    }
  }
  
  
  
  private void _parse(Object o, String prefix) 
  {
    for (Field field : ReflexionUtils.getDeclaredFields(o.getClass(), true))
    {
      Object toAdd = ReflexionUtils.getFieldValue(field, o);
      if (toAdd == null) continue;
      boolean isFactor = toAdd instanceof Factor;
      if (isFactor)
        addFactor((Factor) toAdd, growPrefix(prefix, field.getName()));
      
      if (field.isAnnotationPresent(SubModel.class))
        _parse(toAdd, growPrefix(prefix, field.getName()));
    }
  }
//  
//  public String getStochNameOrFixedValue(Object variable)
//  {
//    Node node = variableNode(variable);
//    if (stochasticVariables.contains(node))
//      return ;
//    else
//      return node.getAsVariable().toString();
//  }
  
  public String toString(Factor f)
  {
    List<String> assignments = Lists.newArrayList();
    for (Object variable : neighborVariables(f))
      assignments.add(variableNames.get(variableNode(variable)));
    return f.toString() + "(" + Joiner.on(", ").join(assignments) + ")";
  }
  
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    for (Factor f : allFactors())
      result.append(toString(f) + "\n");
    return result.toString();
  }


  
//  private boolean _parse(Object o) throws IllegalArgumentException, IllegalAccessException  
//  {
//    List<Field> fields = ReflexionUtils.getAnnotatedDeclaredFields(o.getClass(), AddToModel.class, true);
//    for (Field field : fields)
//    {
//      Object toAdd = field.get(o); 
//      boolean isFactor = toAdd instanceof Factor;
//      if (isFactor)
//        addRelation((Factor) toAdd);
//      boolean recDidSomething = _parse(toAdd);
//      if (!isFactor && !recDidSomething)
//        throw new RuntimeException("An annotation @AddToModel should be on a field of type Factor, or " +
//            "should recursively contain fields with the same annotation and that property.");
//    }
//    return !fields.isEmpty();
//  }
}
