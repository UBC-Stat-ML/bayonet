package blang.accessibility;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import blang.accessibility.AccessibilityGraph.Node;

import com.google.common.collect.LinkedHashMultimap;


/**
 * Analysis of an accessibility graph for the purpose of building a factor graph.
 * 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class FactorAccessibilityGraphAnalyzer
{
  // note: can include nested factors
  private final ArrayList<AccessibilityGraph> factorAccessibilityGraphs;
  
  private final LinkedHashSet<Node> observed;
  
  question: is the savings of having all the factors in the AccessibilityGraph worth it?
  
  public FactorAccessibilityGraphAnalyzer(List<Factor> factors, List<Node> recursivelyObserved, List<Node> nonRecursivelyObserved)
  {
    factorAccessibilityGraphs = new ArrayList<>();
    for (Factor f : factors)
      factorAccessibilityGraphs.add()
  }
  
  public static interface Factor
  {
    
  }
  
  private static LinkedHashMultimap<Factor, Node> createMutableToFactorCache(ArrayList<AccessibilityGraph> factorAccessibilityGraphs)
  {
    final LinkedHashMultimap<Factor, Node> result = LinkedHashMultimap.create();
    
    for (AccessibilityGraph g : factorAccessibilityGraphs)
    {
      if (!(g.root instanceof Factor))
        throw new RuntimeException("Top level model elements should be instances of Factor");
      final Factor f = (Factor) g.root;
      g.getMutableNodes().forEach(node -> result.put(f, node));
    }
    return result;
  }
  
  public static void main(String [] args)
  {
//    AccessibilityGraph g = AccessibilityGraph.inferGraph(new Object());
//    g.graph.vertexSet().stream().filter(node -> node instanceof ObjectNode).; //node instanceof ObjectNode)
  }
}
