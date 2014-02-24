package blang;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import bayonet.graphs.GraphUtils;
import blang.factors.Factor;
import blang.parsing.Node;
import blang.parsing.VariableNames;
import briefj.BriefStrings;
import briefj.ReflexionUtils;

import static blang.parsing.Node.*;

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
  
  @SuppressWarnings("unchecked")
  public ProbabilityModel(Object specification)
  {
    parse(specification, Collections.EMPTY_LIST);
  }
  
  public void parse(Object o, List<Field> fieldsPath) 
  { 
    try {  _parse(o, fieldsPath); } 
    catch (Exception e) { throw new RuntimeException(e); }
  }
  
  public void addFactor(Factor f, List<Field> fieldsPath)
  {
    Node factorNode = factorNode(f);
    if (factors.contains(factorNode))
      throw new RuntimeException("Trying to insert the same factor twice");
    factors.add(factorNode);
    graph.addVertex(factorNode);
    addLinks(f, f, fieldsPath);
    if (Graphs.neighborListOf(graph, factorNode(f)).isEmpty())
      throw new RuntimeException("Problem in " + fieldsPathToString(fieldsPath) + ": a factor should contain at least one FactorArgument " +
      		"defining a random variable, or at least one FactorComponent recursively satisfying that property. " +
      		"See FactorArgument.makeStochastic()");
  }
  
  public static String fieldsPathToString(List<Field> fieldsPath)
  {
    List<String> result = Lists.newArrayList();
    for (Field f : fieldsPath)
      result.add(f.getName());
    return Joiner.on(".").join(result);
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
  
  private void addLinks(Factor f, Object o, List<Field> fieldsPath)
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
        ensureVariableAdded(variable, growList(fieldsPath, argumentField)); 
        graph.addEdge(variableNode(variable), factorNode(f));
      }
    }
    for (Field componentField : ReflexionUtils.getAnnotatedDeclaredFields(o.getClass(), FactorComponent.class, true))
    {
      if (!Modifier.isFinal(componentField.getModifiers()))
        throw new RuntimeException("Fields annotated with FactorComponent should be final.");
      Object value = ReflexionUtils.getFieldValue(componentField, o);
      if (value != null)
        addLinks(f, value, growList(fieldsPath, componentField)); 
    }
  }
  
  private void ensureVariableAdded(Object variable, List<Field> fieldsPath)
  {
    if (graph.containsVertex(variableNode(variable))) 
      return;
    graph.addVertex(variableNode(variable));
    variableNames.add(variableNode(variable), fieldsPath); 
  }
  
  private static <T> List<T> growList(List<T> old, T newItem)
  {
    List<T> result = Lists.newArrayList(old);
    result.add(newItem);
    return result;
  }

  
  private void _parse(Object o, List<Field> fieldsPath) 
  {
    for (Field field : ReflexionUtils.getAnnotatedDeclaredFields(o.getClass(), Let.class, true))
    {
      Object toAdd = ReflexionUtils.getFieldValue(field, o);
      if (toAdd == null) continue;
      boolean isFactor = toAdd instanceof Factor;
      if (!isFactor)
        throw new RuntimeException("@Factor annotation only permitted on objects of type of Factor");
        
      addFactor((Factor) toAdd, growList(fieldsPath, field));
      
      _parse(toAdd, growList(fieldsPath, field)); 
    }
  }
  
  public String toString(Factor f)
  {
    return f.getClass().getSimpleName() + "(\n" + BriefStrings.indent(argumentsAndComponentsToString(f)) + "\n)"; //+ Joiner.on(", ").join(assignments) + ")";
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
  
  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    for (Factor f : allFactors())
      result.append(toString(f) + "\n");
    return result.toString();
  }

}
