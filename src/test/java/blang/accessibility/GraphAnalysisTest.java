package blang.accessibility;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.junit.Assert;
import org.junit.Test;

import blang.accessibility.AccessibilityGraph.DotExporter;
import blang.accessibility.AccessibilityGraph.Node;
import blang.accessibility.AccessibilityGraphTest.ModelModel;



public class GraphAnalysisTest
{
  AccessibilityGraphTest examples = new AccessibilityGraphTest();
  
  @Test
  public void testClosure()
  {
    ModelModel m = new ModelModel();

    final LinkedHashSet<Node> generator = new LinkedHashSet<>();
    generator.add(new ObjectNode<>(m.cd2));
    generator.add(new ObjectNode<>(m.gd1));
    
    AccessibilityGraph ag = new AccessibilityGraph();
    ag.add(m);
    
    DotExporter<Node, Pair<Node, Node>> dotExporter = ag.toDotExporter();
    dotExporter.addVertexAttribute("fillcolor", node -> generator.contains(node) ? "grey" : "white");
    dotExporter.addVertexAttribute("style", node -> generator.contains(node) ? "filled" : "");
    dotExporter.export(new File("doc/before-gen.dot"));
    
    LinkedHashSet<Node> closure = GraphAnalysis.closure(ag.graph, generator);
    
    dotExporter = ag.toDotExporter();
    dotExporter.addVertexAttribute("fillcolor", node -> closure.contains(node) ? "grey" : "white");
    dotExporter.addVertexAttribute("style", node -> closure.contains(node) ? "filled" : "");
    dotExporter.export(new File("doc/after-gen.dot"));
    
    check(closure, ag.graph);
    
//    DotExporter<Node, Pair<Node, Node>> dotExporter = ag.toDotExporter();
  }
  
  public <V,E> void check(Set<V> closure, DirectedGraph<V, E> graph)
  {
    for (E e : graph.edgeSet())
    {
      V src  = graph.getEdgeSource(e),
        dest = graph.getEdgeTarget(e);
      
      if (closure.contains(src))
        Assert.assertTrue(closure.contains(dest));
    }
  }
}
