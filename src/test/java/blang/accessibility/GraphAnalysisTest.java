package blang.accessibility;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.junit.Assert;
import org.junit.Test;

import bayonet.graphs.DotExporter;
import blang.accessibility.AccessibilityGraph.Node;
import blang.accessibility.AccessibilityGraphTest.IntEntry;
import blang.accessibility.AccessibilityGraphTest.ModelModel;
import blang.accessibility.GraphAnalysis.Inputs;
import blang.factors.Factor;



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
    
    LinkedHashSet<Node> closure = GraphAnalysis.closure(ag.graph, generator, true);
    
    dotExporter = ag.toDotExporter();
    dotExporter.addVertexAttribute("fillcolor", node -> closure.contains(node) ? "grey" : "white");
    dotExporter.addVertexAttribute("style", node -> closure.contains(node) ? "filled" : "");
    dotExporter.export(new File("doc/after-gen.dot"));
    
    check(closure, ag.graph);
    
    LinkedHashSet<Node> ancestors = GraphAnalysis.closure(ag.graph, closure, false);
    
    dotExporter = ag.toDotExporter();
    dotExporter.addVertexAttribute("fillcolor", node -> ancestors.contains(node) ? "grey" : "white");
    dotExporter.addVertexAttribute("style", node -> ancestors.contains(node) ? "filled" : "");
    dotExporter.export(new File("doc/final-gen.dot"));
    
//    DotExporter<Node, Pair<Node, Node>> dotExporter = ag.toDotExporter();
  }
  
  public static boolean typeHierarchyHasAnnotation(Class<?> root, Class<? extends Annotation> a)
  {
    LinkedList<Class<?>> toExplore = new LinkedList<>();
    toExplore.add(root);
    HashSet<Class<?>> explored = new HashSet<>();
    
    while (!toExplore.isEmpty())
    {
      Class<?> current = toExplore.poll();
      explored.add(current);
      if (current.isAnnotationPresent(a))
        return true;
      
      Class<?> parent = current.getSuperclass();
      if (parent != null && !explored.contains(parent))
        toExplore.add(parent);
      
      for (Class<?> anInterface : current.getInterfaces())
        if (!explored.contains(anInterface))
          toExplore.add(anInterface);
    }
    return false;
  }
  
//  public static boolean hasAnnotation(Class<?> c, Class<? extends Annotation> a, boolean recurse)
//  {
//    if (c == null)
//      return false;
//    if (c.isAnnotationPresent(a))
//      return true;
//    if (!recurse)
//      return false;
//    Class<?> parent = c.getSuperclass();
//    
//    return hasAnnotation(parent, a, recurse);
//  }
  
  @Test
  public void testFactorGraphCreation()
  {
    ModelModel m = new ModelModel();
    
    System.out.println(typeHierarchyHasAnnotation(IntEntry.class, Variable.class));
    
    Inputs inputs = new Inputs((Class<?> c) -> typeHierarchyHasAnnotation(c, Variable.class));
    
    for (Factor f : Arrays.asList(m.cd1, m.cd2, m.cd3, m.gd1, m.gd2))
      inputs.addFactor(f);
    
    GraphAnalysis ga = GraphAnalysis.create(inputs);
    
    inputs.accessibilityGraph.toDotExporter().export(new File("doc/corresponding-access-graph.dot"));
    
    ga.factorGraphVisualization().export(new File("doc/factor-graph.dot"));
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
