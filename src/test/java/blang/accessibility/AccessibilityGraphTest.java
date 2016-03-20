package blang.accessibility;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import blang.accessibility.AccessibilityGraph.Node;
import blang.accessibility.ExplorationRules.ObjectArrayView;



public class AccessibilityGraphTest
{
  @Test
  public void testSpecialRules() // and self loops
  {
    TestComplexClass 
      obj1 = buildComplexClass();
    
    ObjectArrayView<Mutable> obj2 = new ObjectArrayView<>(ImmutableList.of(1), obj1.array);
    
    AccessibilityGraph g = AccessibilityGraph.inferGraph(Arrays.asList(obj1, obj2));
    
    g.toDotFile(new File("doc/example-accessibility-graph.dot"));
    
    Assert.assertTrue(g.graph.outDegreeOf(new ObjectNode(obj1)) == 2);
    Assert.assertTrue(g.graph.outDegreeOf(new ObjectNode(obj1.array)) == 2);
    Assert.assertTrue(g.graph.outDegreeOf(new ObjectNode(obj2)) == 1);
    Assert.assertTrue(g.graph.vertexSet().size() == 21);

    checkInvariants(g);
  }
  
  public void checkInvariants(AccessibilityGraph g)
  {
    for (Node n : g.graph.vertexSet())
    {
      // object nodes points to constituent nodes only; and constituent nodes points to object nodes only.
      int type1 = n instanceof ObjectNode ? 1 : 0;
      for (Pair<Node,Node> e : g.graph.outgoingEdgesOf(n))
      {
        int type2 = e.getRight() instanceof ObjectNode ? 1 : 0;
        Assert.assertTrue(type1 + type2 == 1);
      }
      
      // No! see array of primitive view example!
//      // constituent nodes have exactly one incoming edge
//      if (n instanceof ConstituentNode<?>)
//        Assert.assertTrue(g.graph.inDegreeOf(n) == 1);
      
      // Constituent nodes always have at most one outgoing edge (zero in the case of primitives)
      if (n instanceof ConstituentNode<?>)
        Assert.assertTrue(g.graph.outDegreeOf(n) <= 1);
    }
  }
  
  static TestComplexClass buildComplexClass()
  {
    Mutable [] arr = new Mutable[2];
    for (int i = 0; i < 2; i++)
    {
      Mutable m = new Mutable();
      m.value = "#" + i;
      arr[i] = m;
    }
    
    TestComplexClass result = new TestComplexClass(arr);
    result.selfRef = result;
    
    return result;
  }
  
  
  static class TestComplexClass
  {
    TestComplexClass selfRef;
    final Mutable [] array;
    TestComplexClass(Mutable [] array) { this.array = array;}
  }
  
  static class Mutable
  {
    String value;
    @Override
    public String toString()
    {
      return value;
    }
  }
  
  //
  
  @Test
  public void testComplexClasses()
  {
    C2 c2 = new C2();
    C2.C3 c3 = c2.new C3();
    C2.C3.C4 c4 = c3.new C4();
    AccessibilityGraph g = AccessibilityGraph.inferGraph(c4);
    Assert.assertTrue(g.graph.vertexSet().size() == 10);
    g.toDotFile(new File("doc/example-accessibility-graph2.dot"));
  }
  
  static class C1
  {
    int i1;
  }
  
  static class C2 extends C1
  {
    int i2;
    
    class C3 extends C1
    {
      int i3;
      
      class C4
      {
        int i4;
      }
    }
  }
  
  //
  
  @Test
  public void testAnon()
  {
    Outer out = new Outer();
    Object anon = out.newAnon();
    AccessibilityGraph g = AccessibilityGraph.inferGraph(anon);
    Assert.assertTrue(g.graph.vertexSet().size() == 5);
    g.toDotFile(new File("doc/example-accessibility-graph3.dot"));
  }
  
  static class Outer
  {
    String s1;
    
    public Object newAnon()
    {
      return new Object() 
      {
        @SuppressWarnings("unused")
        String s2;
      };
    }
  }
}
