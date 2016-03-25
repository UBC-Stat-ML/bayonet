package blang.accessibility;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jgrapht.DirectedGraph;

import blang.accessibility.AccessibilityGraph.Node;

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
  
//  public static class Inputs
//  {
//    private final List<? extends Factor> factors;
//  }
//  
//  public static GraphAnalysis perform(Inputs inputs)
//  {
//    // 1- compute the full accessibility graph
//    AccessibilityGraph accessibilityGraph = new AccessibilityGraph();
//    for (Factor f : inputs.factors)
//      accessibilityGraph.add(f);
//    
//    // 2- identify the subset of classes
//  }
  
  private GraphAnalysis() 
  {
    
  }
  
  public static interface Factor
  {
    
  }
  
  static LinkedHashSet<ObjectNode<?>> latentVariables(
      Stream<Node> accessibleNodes, 
      final Set<Node> observedNodesClosure,
      Set<Class<?>> variableClasses
      )
  {
    LinkedHashSet<ObjectNode<?>> result = new LinkedHashSet<>();
    accessibleNodes
        .filter(node -> !observedNodesClosure.contains(node))
        .filter(node -> variableClasses.contains(node.getClass()))
        .map(node -> (ObjectNode<?>) node)
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
