package blang.accessibility;

import java.io.File;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import bayonet.math.CoordinatePacker;
import blang.accessibility.AccessibilityGraph.Node;
import blang.factors.Factor;



public class AccessibilityGraphTest
{
  @Test
  public void testSpecialRules() // and self loops
  {
    TestComplexClass 
      obj1 = buildComplexClass();
    
    ObjectArrayView<Mutable> obj2 = new ObjectArrayView<>(ImmutableList.of(1), obj1.array);
    
    AccessibilityGraph g = new AccessibilityGraph();
    g.add(obj1);
    g.add(obj2);
    
    g.toDotExporter().export(new File("doc/example-accessibility-graph.dot"));
    
    Assert.assertTrue(g.graph.outDegreeOf(new ObjectNode<>(obj1)) == 2);
    Assert.assertTrue(g.graph.outDegreeOf(new ObjectNode<>(obj1.array)) == 2);
    Assert.assertTrue(g.graph.outDegreeOf(new ObjectNode<>(obj2)) == 1);
    Assert.assertTrue(g.graph.vertexSet().size() == 13);
    
    Assert.assertTrue(AccessibilityGraph.toStream(new BreadthFirstIterator<>(g.graph)).count() == 13);
    
    Assert.assertTrue(g.getAccessibleNodes(obj1.array).count() == 9);
    Assert.assertTrue(g.getAccessibleNodes(obj1.array).filter(node -> node.isMutable()).count() == 4);

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
  
  
  public static class TestComplexClass implements Factor
  {
    TestComplexClass selfRef;
    final Mutable [] array;
    TestComplexClass(Mutable [] array) { this.array = array;}
    @Override
    public double logDensity()
    {
      throw new RuntimeException();
    }
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
    AccessibilityGraph g = new AccessibilityGraph();
    g.add(c4);
    Assert.assertTrue(g.graph.vertexSet().size() == 10);
    g.toDotExporter().export(new File("doc/example-accessibility-graph2.dot"));
    
    checkInvariants(g);
  }
  
  static class C2 extends TestClass1
  {
    // test private field too
    @SuppressWarnings("unused")
    private int i2;
    
    class C3 extends TestClass1
    {
      @SuppressWarnings("unused")
      private int i3;
      
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
    Sneaky s = new Sneaky();
    Object anon = out.newAnon(s);
    AccessibilityGraph g = new AccessibilityGraph();
    g.add(anon);
//    g.add(s);
    g.toDotExporter().export(new File("doc/example-accessibility-graph3.dot"));
    Assert.assertTrue(g.graph.vertexSet().size() == 8);
    Assert.assertTrue(AccessibilityGraph.toStream(new BreadthFirstIterator<>(g.graph)).count() == 8);
  }
  
  static class Outer
  {
    String s1;
    
    public Object newAnon(final Sneaky s)
    {
      return new Object() 
      {
        @SuppressWarnings("unused")
        String s2;
        
        @Override
        public String toString()
        {
          return "" + s.sneaky;
        }
      };
    }
  }
  
  static class Sneaky
  {
    int sneaky = 123;
  }
  
  // model e.g. scaffold
  @Test
  public void testModelModel()
  {
    AccessibilityGraph g = buildModelModel();
    g.toDotExporter().export(new File("doc/example-accessibility-graph4.dot"));
    
//    Assert.assertTrue(g.graph.vertexSet().size() == 72);
    
    checkInvariants(g);
  }
  
  public static class ModelModel
  {
    DoubleMatrix rates = new DoubleMatrix(2,2);
    
    GammaDistribution gd1 = new GammaDistribution(rates.entry(0, 1), null);
    GammaDistribution gd2 = new GammaDistribution(rates.entry(1, 0), null);
    
    IntMatrix backbone = new IntMatrix(3, 1);
    
    CategoricalDistribution cd1 = new CategoricalDistribution(backbone.entry(0), null);
    CategoricalDistribution cd2 = new CategoricalDistribution(backbone.entry(1), new MyCatParams(backbone.entry(0), rates, 1.0));
    CategoricalDistribution cd3 = new CategoricalDistribution(backbone.entry(2), new MyCatParams(backbone.entry(1), rates, 1.0));
  }
  
  public AccessibilityGraph buildModelModel()
  {
    ModelModel m = new ModelModel();
    
    AccessibilityGraph g = new AccessibilityGraph();
    g.add(m.gd1);
    g.add(m.gd2);
    g.add(m.cd1);
    g.add(m.cd2);
    g.add(m.cd3);
    return g;
  }
  
  static class MyCatParams implements CatParams
  {
    final Int prev;
    final DoubleMatrix rates;
    final double delta;
    
    public MyCatParams(Int prev, DoubleMatrix rates, double delta)
    {
      this.prev = prev;
      this.rates = rates;
      this.delta = delta;
    }

    @Override
    public DoubleMatrix probabilities()
    {
      // set diagonals
      // P = exp(rates)
      // return P.row(prev.get());
      return null;
    }
  }
  
  @Variable
  static interface Real
  {
    public double get();
    public void set(double value);
  }
  
  @Variable
  static interface Int
  {
    public int get();
    public void set(int value);
  }
  
  static class GammaDistribution implements Factor
  {
    @SuppressWarnings("unused")
    private final Real realization;
    @SuppressWarnings("unused")
    private final GammaParams params;
    public GammaDistribution(Real realization, GammaParams params)
    {
      this.realization = realization;
      this.params = params;
    }
    public double logDensity()
    {
      throw new RuntimeException();
    }
  }
  
  static interface GammaParams
  {
  }
  
  static class CategoricalDistribution implements Factor
  {
    @SuppressWarnings("unused")
    private final Int realization;
    @SuppressWarnings("unused")
    private final CatParams params;
    public CategoricalDistribution(Int realization, CatParams params)
    {
      this.realization = realization;
      this.params = params;
    }
    public double logDensity()
    {
      throw new RuntimeException();
    }
  }
  
  static interface CatParams
  {
    public DoubleMatrix probabilities();
  }
  
  static class RealEntry implements Real
  {
    final DoubleArrayView view;
    public RealEntry(DoubleArrayView view)
    {
      this.view = view;
    }
    @Override
    public double get()
    {
      return view.get(0);
    }
    @Override
    public void set(double value)
    {
      view.set(0, value);
    }
  }
  
  static class IntEntry implements Int
  {
    final IntArrayView view;
    public IntEntry(IntArrayView view)
    {
      this.view = view;
    }
    @Override
    public int get()
    {
      return view.get(0);
    }
    @Override
    public void set(int value)
    {
      view.set(0, value);
    }
  }

  static class DoubleMatrix
  {
    private final double[] data;
    private final CoordinatePacker packer; // todo: replace by more efficient stuff
    
    public DoubleMatrix(int rows, int cols)
    {
      this.data = new double[rows*cols];
      this.packer = new CoordinatePacker(new int[]{rows, cols});
      cache = new Real[rows*cols];
    }

    private final Real [] cache;
    public Real entry(int i, int j)
    {
      final int entryIndex = packer.coord2int(i,j);
      
      if (cache[entryIndex] != null)
        return cache[entryIndex];
      
      final DoubleArrayView view = new DoubleArrayView(ImmutableList.of(entryIndex), data);
      return cache[entryIndex] = new RealEntry(view);
    }
  }
  
  static class IntMatrix
  {
    private final int[] data;
    private final CoordinatePacker packer; // todo: replace by more efficient stuff
    
    public IntMatrix(int rows, int cols)
    {
      this.data = new int[rows*cols];
      this.packer = new CoordinatePacker(new int[]{rows, cols});
      cache = new Int[rows*cols];
    }
    
    private final Int [] cache;
    public Int entry(final int entryIndex)
    {
      if (cache[entryIndex] != null)
        return cache[entryIndex];
      final IntArrayView view = new IntArrayView(ImmutableList.of(entryIndex), data);
      return cache[entryIndex] = new IntEntry(view);
    }

    public Int entry(int i, int j)
    {
      final int entryIndex = packer.coord2int(i,j);
      return entry(entryIndex);
    }
  }
  
  @Test
  public void testBFSFunctionality()
  {
    TestComplexClass 
      buildComplexClass1 = buildComplexClass(),
      buildComplexClass2 = buildComplexClass();
    
    AccessibilityGraph ag = new AccessibilityGraph();
    ag.add(buildComplexClass1);
    ag.add(buildComplexClass2);
    
    Assert.assertTrue(AccessibilityGraph.toStream(new BreadthFirstIterator<>(ag.graph)).count() == 24);
  }
  
}
