package blang.accessibility;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jgrapht.DirectedGraph;

import blang.accessibility.AccessibilityGraph.Node;
import briefj.BriefCollections;

import com.google.common.collect.LinkedHashMultimap;
import com.sun.accessibility.internal.resources.accessibility;


/**
 * Analysis of an accessibility graph for the purpose of building a factor graph.
 * 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class GraphAnalysis
{
  // note: can include nested factors
//  private final ArrayList<AccessibilityGraph> factorAccessibilityGraphs;
//  
//  private final LinkedHashSet<Node> observed;
  
//  question: is the savings of having all the factors in the AccessibilityGraph worth it?
      
//  todo: use predicates to print dot files
  
//  public FactorAccessibilityGraphAnalyzer(List<Factor> factors, List<Node> recursivelyObserved, List<Node> nonRecursivelyObserved)
//  {
//    factorAccessibilityGraphs = new ArrayList<>();
//    for (Factor f : factors)
//      factorAccessibilityGraphs.add()
//  }
  
  public static class Inputs
  {
    private final AccessibilityGraph accessibilityGraph;
    private final Set<Node> nonRecursiveObservedNodes, recursiveObservedNodes;
    private final Predicate<Class<?>> isVariablePredicate;
  }
  
  public static GraphAnalysis create(Inputs inputs)
  {
    if (!inputs.accessibilityGraph.graph.vertexSet().containsAll(inputs.nonRecursiveObservedNodes) ||
        !inputs.accessibilityGraph.graph.vertexSet().containsAll(inputs.recursiveObservedNodes))
      throw new RuntimeException("Observed variables should be subsets of the accessibility graph");
    
    if (BriefCollections.intersects(inputs.nonRecursiveObservedNodes, inputs.recursiveObservedNodes))
      throw new RuntimeException("A variable should be either recursively observable, observable, or neither");
    
    // 1- compute the closure of observed variables
    LinkedHashSet<Node> observedNodesClosure = new LinkedHashSet<>();
    observedNodesClosure.addAll(inputs.nonRecursiveObservedNodes);
    observedNodesClosure.addAll(closure(inputs.accessibilityGraph.graph, inputs.recursiveObservedNodes, true));
    
    // 2- find the nodes that are ancestor to some observed variables
    //    (i.e. nodes n such that there is an n' accessible from n and where n' is observed
    LinkedHashSet<Node> ancestorsToObserved = closure(inputs.accessibilityGraph.graph, observedNodesClosure, false);
    
    // 2- identify the variables
    LinkedHashSet<ObjectNode<?>> latentVariables = latentVariables(inputs.accessibilityGraph, observedNodesClosure, inputs.isVariablePredicate);
    
    // 3- subset of variables that only accept non-recursive samplers
    
  }
  
  private GraphAnalysis() 
  {
    
  }
  
  public static interface Factor
  {
    
  }
  
  /*
   * Terminology:
   * 
   *   - variable = attach point for a sampler
   *   - mutables = things we need to take care of in the fully observed case
   *   - observed = mechanism that subsets the mutables
   * 
   * 
   * Responsabilities:
   * 
   *   - making sure all mutable unobserved are covered
   *   - proving facilities to samplers to know what is observed [and skip matches where nothing under is unobserved mutable]
   *   - providing sublists of connected factors to each latent variable
   * 
   */
  
  static LinkedHashSet<ObjectNode<?>> latentVariables(
      AccessibilityGraph accessibilityGraph,
      final Set<Node> observedNodesClosure,
      Predicate<Class<?>> isVariablePredicate
      )
  {
    // find the ObjectNode's which are not in the closure of the observed nodes
    LinkedHashSet<ObjectNode<?>> unobservedNodes = new LinkedHashSet<>();
    accessibilityGraph.getAccessibleNodes()
        .filter(node -> node instanceof ObjectNode<?>)
        .map(node -> (ObjectNode<?>) node)
        .filter(node -> !observedNodesClosure.contains(node))
        .forEachOrdered(unobservedNodes::add);
    
    // for efficiency, apply the predicate on the set of the associated classes
    LinkedHashSet<Class<?>> matchedVariableClasses = new LinkedHashSet<>();
    {
      LinkedHashSet<Class<?>> associatedClasses = new LinkedHashSet<>();
      unobservedNodes.stream()
          .map(node -> node.object.getClass())
          .forEachOrdered(associatedClasses::add);
      
      associatedClasses.stream()
          .filter(isVariablePredicate)
          .forEachOrdered(matchedVariableClasses::add);
    }
    
    // return the unobserved nodes which have a class identified to be a variable
    LinkedHashSet<ObjectNode<?>> result = new LinkedHashSet<>();
    unobservedNodes.stream()
        .filter(node -> matchedVariableClasses.contains(node.getClass()))
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
  
  static LinkedHashMultimap<ObjectNode<? extends Factor>, Node> createMutableToFactorCache(AccessibilityGraph accessibilityGraph)
  {
    final LinkedHashMultimap<ObjectNode<? extends Factor>, Node> result = LinkedHashMultimap.create();
    for (ObjectNode<?> root : accessibilityGraph.roots)
    {
      if (!(root.object instanceof Factor))
        throw new RuntimeException("Top level model elements should be instances of Factor");
      @SuppressWarnings("unchecked")
      final ObjectNode<? extends Factor> factorNode = (ObjectNode<? extends Factor>) root;
      accessibilityGraph.getAccessibleNodes(factorNode).filter(node -> node.isMutable()).forEach(node -> result.put(factorNode, node));
    }
    return result;
  }
  
  public static void main(String [] args)
  {
//    AccessibilityGraph g = AccessibilityGraph.inferGraph(new Object());
//    g.graph.vertexSet().stream().filter(node -> node instanceof ObjectNode).; //node instanceof ObjectNode)
  }
}
