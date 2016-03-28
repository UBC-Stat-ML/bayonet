package blang.accessibility;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;

import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;

import bayonet.graphs.DotExporter;
import bayonet.graphs.GraphUtils;
import blang.accessibility.AccessibilityGraph.Node;
import briefj.BriefCollections;
import briefj.collections.UnorderedPair;

import com.google.common.collect.LinkedHashMultimap;


/**
 * Analysis of an accessibility graph for the purpose of building a factor graph.
 * 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class GraphAnalysis
{
  public static class Inputs
  {
    public final AccessibilityGraph accessibilityGraph = new AccessibilityGraph();
    private final LinkedHashSet<Node> 
      nonRecursiveObservedNodes = new LinkedHashSet<>(), 
      recursiveObservedNodes = new LinkedHashSet<>();
    
    private final LinkedHashSet<ObjectNode<? extends Factor>> factors = new LinkedHashSet<>();
    
    /*
     * This will typically be based on annotations, but perhaps additional hooks 
     * needed for built-in types?
     */
    private final Predicate<Class<?>> isVariablePredicate;

    public Inputs(Predicate<Class<?>> isVariablePredicate)
    {
      this.isVariablePredicate = isVariablePredicate;
    }
    
    public void addFactor(Factor f)
    {
      ObjectNode<Factor> factorNode = new ObjectNode<>(f);
      accessibilityGraph.add(factorNode);
      factors.add(factorNode);
    }
    
    public void addVariable(Object variable)
    {
      // TODO: discover names
      accessibilityGraph.add(variable);
    }
  }
  
  public static GraphAnalysis create(Inputs inputs)
  {
    GraphAnalysis result = new GraphAnalysis();
    result.accessibilityGraph = inputs.accessibilityGraph;
    result.factorNodes = inputs.factors;
    
    if (!inputs.accessibilityGraph.graph.vertexSet().containsAll(inputs.nonRecursiveObservedNodes) ||
        !inputs.accessibilityGraph.graph.vertexSet().containsAll(inputs.recursiveObservedNodes))
      throw new RuntimeException("Observed variables should be subsets of the accessibility graph");
    
    if (BriefCollections.intersects(inputs.nonRecursiveObservedNodes, inputs.recursiveObservedNodes))
      throw new RuntimeException("A variable should be either recursively observable, observable, or neither");
    
    // 1- compute the closure of observed variables
    result.observedNodesClosure = new LinkedHashSet<>();
    result.observedNodesClosure.addAll(inputs.nonRecursiveObservedNodes);
    result.observedNodesClosure.addAll(closure(inputs.accessibilityGraph.graph, inputs.recursiveObservedNodes, true));
    
    // 2- find the unobserved mutable nodes
    result.unobservedMutableNodes = new LinkedHashSet<>();
    inputs.accessibilityGraph.getAccessibleNodes()
        .filter(node -> node.isMutable())
        .filter(node -> !result.observedNodesClosure.contains(node))
        .forEachOrdered(result.unobservedMutableNodes::add);
    
    // 3- identify the latent variables
    result.latentVariables = latentVariables(inputs.accessibilityGraph, result.unobservedMutableNodes, inputs.isVariablePredicate);
    
    // 4- prepare the cache
    result.mutableToFactorCache = LinkedHashMultimap.create();
    for (ObjectNode<? extends Factor> factorNode : inputs.factors) 
      inputs.accessibilityGraph.getAccessibleNodes(factorNode)
        .filter(node -> result.unobservedMutableNodes.contains(node))
        .forEachOrdered(node -> result.mutableToFactorCache.put(node, factorNode));
    
    return result;
  }
  
  private AccessibilityGraph accessibilityGraph;
  private LinkedHashSet<Node> observedNodesClosure;
  private LinkedHashSet<Node> unobservedMutableNodes;
  private LinkedHashSet<ObjectNode<?>> latentVariables;
  private LinkedHashMultimap<Node, ObjectNode<? extends Factor>> mutableToFactorCache;
  private LinkedHashSet<ObjectNode<? extends Factor>> factorNodes;
  
  private GraphAnalysis() 
  {
  }
  
//  next: 
//    - instead of a factor graph object, use injections to annotated fields in the sampler?
//    - use dot stuff to create a factor graph viz based on getConnFactor
//    - naming facilities (based on AccessibilityGraph)
//    - mcmc matching (reuse blang2's NodeMoveUtils)
//    - parsing stuff (to make things observed, etc)
//    - question: how to deal with gradients?
//    - need to think about RF vs MH infrastructure
  
  public DotExporter<Node, UnorderedPair<Node, Node>> factorGraphVisualization()
  {
    
    UndirectedGraph<Node, UnorderedPair<Node, Node>> factorGraph = GraphUtils.newUndirectedGraph();
    
    // add factors
    for (ObjectNode<? extends Factor> f : factorNodes)
      factorGraph.addVertex(f);
    
    // add latent variables and connect them
    for (ObjectNode<?> l : latentVariables)
    {
      factorGraph.addVertex(l);
      for (Node n : getConnectedFactor(l))
        factorGraph.addEdge(n, l);
    }
    
    DotExporter<Node, UnorderedPair<Node,Node>> result = new DotExporter<>(factorGraph);
    result.vertexNameProvider = node -> node.toStringSummary();
    result.addVertexAttribute("shape", node -> factorNodes.contains(node) ? "box" : "");
    
    return result;
  }
        
  
  public LinkedHashSet<ObjectNode<? extends Factor>> getConnectedFactor(ObjectNode<?> latentVariable)
  {
    LinkedHashSet<ObjectNode<? extends Factor>> result = new LinkedHashSet<>();
    accessibilityGraph.getAccessibleNodes(latentVariable)
        .filter(node -> unobservedMutableNodes.contains(node))
        .forEachOrdered(node -> result.addAll(mutableToFactorCache.get(node)));
    return result;
  }
  
  public static interface Factor
  {
    
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Variable
  {
    
  }
  
  /**
   * A latent variable is an ObjectNode which has:
   * 1. some unobserved mutable nodes under it 
   * 2. AND a class identified to be a variable (i.e. such that samplers can attach to them)
   */
  private static LinkedHashSet<ObjectNode<?>> latentVariables(
      AccessibilityGraph accessibilityGraph,
      final Set<Node> unobservedMutableNodes,
      Predicate<Class<?>> isVariablePredicate
      )
  {
    // find the ObjectNode's which have some unobserved mutable nodes under them
    LinkedHashSet<ObjectNode<?>> ancestorsOfUnobservedMutableNodes = new LinkedHashSet<>();
    closure(accessibilityGraph.graph, unobservedMutableNodes, false).stream()
        .filter(node -> node instanceof ObjectNode<?>)
        .map(node -> (ObjectNode<?>) node)
        .forEachOrdered(ancestorsOfUnobservedMutableNodes::add);
    
    // for efficiency, apply the predicate on the set of the associated classes
    LinkedHashSet<Class<?>> matchedVariableClasses = new LinkedHashSet<>();
    {
      LinkedHashSet<Class<?>> associatedClasses = new LinkedHashSet<>();
      ancestorsOfUnobservedMutableNodes.stream()
          .map(node -> node.object.getClass())
          .forEachOrdered(associatedClasses::add);
      associatedClasses.stream()
          .filter(isVariablePredicate)
          .forEachOrdered(matchedVariableClasses::add);
    }
    
    // return the ObjectNode's which have some unobserved mutable nodes under them 
    // AND which have a class identified to be a variable (i.e. such that samplers can attach to them)
    LinkedHashSet<ObjectNode<?>> result = new LinkedHashSet<>();
    ancestorsOfUnobservedMutableNodes.stream()
        .filter(node -> matchedVariableClasses.contains(node.object.getClass()))
        .forEachOrdered(result::add);  
    return result;
  }
  
  static <V,E> LinkedHashSet<V> closure(
      DirectedGraph<V, E> graph, 
      final Set<V> generatingSet,
      boolean forward)
  {
    final LinkedHashSet<V> result = new LinkedHashSet<>();
    LinkedList<V> toExploreQueue = new LinkedList<>(generatingSet);
    
    while (!toExploreQueue.isEmpty())
    {
      V current = toExploreQueue.poll();
      result.add(current);
      for (E e : forward ? graph.outgoingEdgesOf(current) : graph.incomingEdgesOf(current))
      {
        V next = forward ? graph.getEdgeTarget(e) : graph.getEdgeSource(e);
        if (!result.contains(next))
          toExploreQueue.add(next);
      }
    }
    
    return result;
  }
}
