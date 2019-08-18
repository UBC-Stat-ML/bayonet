package bayonet.graphs;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.EdgeSetFactory;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import bayonet.marginal.algo.EdgeSorter;
import bayonet.math.CoordinatePacker;
import briefj.BriefIO;
import briefj.collections.UnorderedPair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;




/**
 * Utilities around functionalities in JGraphT.
 * 
 * Note: although some of the functionalities in JGraphT are based
 * on hashes, the iterators are deterministic since the hashing structures
 * are based on LinkedHashMap and LinkedHashSet.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class GraphUtils 
{
  /**
   * A reasonable default implementation for undirected graphs. Self-loop or repeated edges not permitted.
   * 
   * Fix some performance issues in jgrapht, avoiding when possible costs proportional 
   * to the degree. 
   */
  public static <V> UndirectedGraph<V,UnorderedPair<V, V>> newUndirectedGraph()
  {
    EdgeFactory<V, UnorderedPair<V, V>> factory = undirectedEdgeFactory();
    SimpleGraph<V,UnorderedPair<V, V>> result =  new SimpleGraph<V, UnorderedPair<V, V>>(factory) {
      private static final long serialVersionUID = 1170402040454757448L;
      @Override
      public UnorderedPair<V, V> getEdge(V sourceVertex, V targetVertex) {
        // Default implementation is O(degree), avoid that problem.
        // Important since addEdge calls this.
        if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
          return null;
        UnorderedPair<V, V> e = UnorderedPair.of(sourceVertex, targetVertex);
        if (containsEdge(e))
          return e;
        else 
          return null;
      }
    };
    fixEdgeMapImplementation(result);
    return result;
  }
  
  public static <V,E> boolean hasEdge(Graph<V,E> graph, V v1, V v2) {
    return graph.getEdge(v1, v2) != null;
  }

  /**
   * Return a new instance that is a deep copy of the provided model.
   */
  public static <V> UndirectedGraph<V, UnorderedPair<V, V>> newUndirectedGraph(
      UndirectedGraph<V, UnorderedPair<V, V>> model)
  {
    UndirectedGraph<V, UnorderedPair<V, V>> result = newUndirectedGraph();
    for (V vertex : model.vertexSet())
      result.addVertex(vertex);
    for (UnorderedPair<V, V> edge : model.edgeSet())
      result.addEdge(edge.getFirst(), edge.getSecond());
    return result;
  }
  
  /**
   * A reasonable default implementation for directed graphs. Self-loop or repeated edges not permitted.
   * 
   * Fix some performance issues in jgrapht, avoiding when possible costs proportional 
   * to the degree. 
   */
  public static <V> DirectedGraph<V, Pair<V, V>> newDirectedGraph()
  {
    EdgeFactory<V, Pair<V, V>> factory = directedEdgeFactory();
    // NB: switch from SimpleGraph to DirectedPseudograph has critical performance implications
    // beyond the superficial corner cases of allowing loops and multiple edges:
    // This is a workaround to prevent addEdge from calling getEdge which has cost 
    // proportional to the degree which is unacceptable e.g. for large graphical models 
    // with static parameters. See https://github.com/UBC-Stat-ML/blangSDK/issues/71
    SimpleDirectedGraph<V, Pair<V, V>> result = new SimpleDirectedGraph<V, Pair<V,V>>(factory) {
      private static final long serialVersionUID = 8322272935312664834L;
      @Override
      public Pair<V, V> getEdge(V sourceVertex, V targetVertex) {
        // Default implementation is O(degree), avoid that problem.
        // Important since addEdge calls this.
        if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
          return null;
        Pair<V,V> e = Pair.of(sourceVertex, targetVertex);
        if (outgoingEdgesOf(sourceVertex).contains(e))
          return e;
        else 
          return null;
      }
    };
    fixEdgeMapImplementation(result);
    return result;
  }
  
  /**
   * Remove bad default in the library, which uses ArrayUnenforcedSet which 
   * saves a constant amount of space at the cost of edge sets having linear 
   * instead of constant access! see https://github.com/UBC-Stat-ML/blangSDK/issues/71 
   * for an example where this becomes a serious performance issue
   */
  public static <V,E> void fixEdgeMapImplementation(AbstractBaseGraph<V, E> graph) 
  {
    graph.setEdgeSetFactory(new EdgeSetFactory<V, E>() {
      @Override
      public Set<E> createEdgeSet(V vertex) {
        return new LinkedHashSet<>(4); // still try to be conscientious regarding to space
      }
    });
  }
  
  /**
   * An undirected edge, i.e. an UnorderedPair
   * @param <V>
   * @param first
   * @param second
   * @return
   */
  public static <V> UnorderedPair<V, V> undirectedEdge(V first, V second)
  {
    return new UnorderedPair<V, V>(first, second);
  }

  private static <V> EdgeFactory<V, UnorderedPair<V, V>> undirectedEdgeFactory()
  {
    return new EdgeFactory<V, UnorderedPair<V,V>>() {

      @Override
      public UnorderedPair<V, V> createEdge(V sourceVertex, V targetVertex)
      {
        return undirectedEdge(sourceVertex, targetVertex);
      }
    };
  }
  
  /**
   * A directed edge, i.e. a Pair
   * @param <V>
   * @param first
   * @param second
   * @return
   */
  public static <V> Pair<V, V> directedEdge(V first, V second)
  {
    return Pair.of(first, second);
  }
  
  private static <V> EdgeFactory<V, Pair<V, V>> directedEdgeFactory()
  {
    return new EdgeFactory<V, Pair<V,V>>() {

      @Override
      public Pair<V, V> createEdge(V sourceVertex, V targetVertex)
      {
        return directedEdge(sourceVertex, targetVertex);
      }
    };
  }
  
  /**
   * List nodes in post order.
   * 
   * Note: will only list vertices in the connected component of 
   * the given vertex, i.e. lastForwardVertex.
   * 
   * @param <V> 
   * @param <E>
   * @param graph
   * @param root
   * @return
   */
  public static <V,E> ArrayList<V> postorder(UndirectedGraph<V, E> graph, final V root)
  {
    final ArrayList<V> result = Lists.newArrayList();
    DepthFirstIterator<V, E> iterator = new DepthFirstIterator<V, E>(graph,root);
    iterator.addTraversalListener(new TraversalListener<V, E>() {
      @Override public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {}
      @Override public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {}
      @Override public void edgeTraversed(EdgeTraversalEvent<V, E> e) {}
      @Override public void vertexTraversed(VertexTraversalEvent<V> e) {}
      @Override public void vertexFinished(VertexTraversalEvent<V> e)
      {
        result.add(e.getVertex());
      }
    });
    while (iterator.hasNext())
      iterator.next();
   
    return result;
  }
  
  /**
   * 
   * @param <V>
   * @param graph
   * @param root
   * @return A map where each non-root key is given as a value its parent relative to root
   */
  public static <V> Map<V,V> parentPointers(UndirectedGraph<V, ?> graph, final V root)
  {
    Map<V,V> result = Maps.newHashMap();
    
    EdgeSorter<V> sorter = EdgeSorter.newEdgeSorter(graph, root);
    
    for (Pair<V,V> edge : sorter.forwardMessages())
      result.put(edge.getLeft(), edge.getRight());
    
    return result;
  }
  
  /**
   * List the connected components. 
   */
  public static <V,E> List<Set<V>> connectedComponents(UndirectedGraph<V, E> graph)
  {
    ConnectivityInspector<V, E> connComponentsCalculator = new ConnectivityInspector<V,E>(graph);
    return connComponentsCalculator.connectedSets();
  }
  
  /**
   * Return all (np to n1) st. np not equal to n2
   */
  public static <V,E> List<Pair<V,V>> distinctIncoming(UndirectedGraph<V,E> f, Pair<V,V> e)
  {
    return neighbors(f, e.getLeft(), e.getRight(), true);
  }
  
  private static <V,E> List<Pair<V,V>> neighbors(UndirectedGraph<V, E> graph, V node, V excluded, boolean incoming)
  {
    List<Pair<V,V>> result = Lists.newArrayList();
    for (V neighbor : Graphs.neighborListOf(graph, node)) 
      if (!neighbor.equals(excluded))
      {
        Pair<V,V> edge = Pair.of(incoming ? neighbor : node, incoming ? node : neighbor);
        result.add(edge);
      }
    return result;
  }
  
  /**
   * Return the linearization or topological order of a given directed graph.
   * 
   * In contrast to TopologicalOrderIterator, order is always deterministic and 
   * cycle are detected first.
   */
  public static <V,E> List<V> linearization(
      DirectedGraph<V, E> graph)
  {
    // check for cycles first
    CycleDetector<V, E> cycleDetector = new CycleDetector<>(graph);
    if (cycleDetector.detectCycles())
      throw new RuntimeException("Linearization undefined when cycles are present.");
    
    // Used to break ties according to the order the vertices appear in the vertex set LinkedHashSet
    PriorityQueue<V> appearenceOrdering = new PriorityQueue<>(new AppearanceOrderComparator<>(graph.vertexSet()));
    GraphIterator<V, E> iter = new TopologicalOrderIterator<V, E>(graph, appearenceOrdering);
    List<V> result = Lists.newArrayList();
    while (iter.hasNext())
      result.add(iter.next());
    return result;
  }
  
  private static class AppearanceOrderComparator<T> implements Comparator<T>
  {
    private Map<T, Integer> order = new IdentityHashMap<>();
    
    public AppearanceOrderComparator(Iterable<T> objects) 
    {
      int i = 0;
      for (T object : objects) 
        if (!order.containsKey(object))
          order.put(object, i);
    }

    @Override
    public int compare(T o1, T o2)
    {
      Integer 
        i1 = order.get(o1),
        i2 = order.get(o2);
      return i1.compareTo(i2);
    }
  }
  
  /**
   * http://en.wikipedia.org/wiki/Complete_graph
   * 
   * @return A complete graph on the provided set of states.
   */
  public static <S> UndirectedGraph<S,?> completeGraph(final Set<S> states)
  {
    UndirectedGraph<S,?> result = GraphUtils.newUndirectedGraph();
    
    for (S state : states)
      result.addVertex(state);
    
    for (S state : states)
    {
      for (S state2 : states)
        if (state != state2)
          result.addEdge(state, state2);
    }
    return result;
  }
  
  public static UndirectedGraph<Integer, ?> createChainTopology(int len)
  {
    UndirectedGraph<Integer, ?> result = GraphUtils.newUndirectedGraph();
    
    for (int i = 0; i < len; i++)
      result.addVertex(i);
    
    for (int i = 0; i < len - 1; i++)
      result.addEdge(i, i+1);
    
    return result;
  }
  
  public static UndirectedGraph<Integer, UnorderedPair<Integer, Integer>> grid(CoordinatePacker packer)
  {
    UndirectedGraph<Integer, UnorderedPair<Integer, Integer>> result = GraphUtils.newUndirectedGraph();
    for (int node = 0; node < packer.max; node++)
      result.addVertex(node);
    for (int node = 0; node < packer.max; node++)
    {
      int [] nodeCoord = packer.int2coord(node);
      for (int dim = 0; dim < 2; dim++)
        for (int delta = -1; delta <= +1; delta += 2)
        {
          int [] otherCoord = nodeCoord.clone();
          otherCoord[dim] += delta;
          if (otherCoord[dim] >= 0 && otherCoord[dim] < packer.getSize(dim))
          {
            int otherNode = packer.coord2int(otherCoord);
            result.addEdge(node, otherNode);
          }
        }
    }
    return result;
  }
  
  /**
   * Pick the end of an edge other than the one in variable node
   * 
   * @param <T>
   * @param pair
   * @param node
   * @return
   */
  public static <T> T pickOther(UnorderedPair<T,T> pair, T node)
  {
    if (node.equals(pair.getFirst() )) return pair.getSecond();
    if (node.equals(pair.getSecond())) return pair.getFirst();
    throw new RuntimeException();
  }


  /**
   * 
   * @param <N>
   * @param topology
   * @return The nodes that have strictly more than one neighbors.
   */
  public static <N> List<N> internalNodes(UndirectedGraph<N,UnorderedPair<N, N>> topology)
  {
    List<N> result = Lists.newArrayList();
    for (N vertex : topology.vertexSet())
      if (topology.degreeOf(vertex) > 1)
        result.add(vertex);
    return result;
  }


  /**
   * 
   * @param <N>
   * @param topology
   * @return The nodes that have one or zero neighbors.
   */
  public static <N> List<N> leaves(UndirectedGraph<N,UnorderedPair<N, N>> topology)
  {
    List<N> result = Lists.newArrayList();
    for (N vertex : topology.vertexSet())
      if (topology.degreeOf(vertex) <= 1)
        result.add(vertex);
    return result;
  }


  /**
   * @param <N>
   * @param edge
   * @param topology
   * @return The edges connected to leaves.
   */
  public static <N> boolean isTip(UnorderedPair<N, N> edge, UndirectedGraph<N,UnorderedPair<N, N>> topology)
  {
    return topology.degreeOf(edge.getFirst()) == 1 || topology.degreeOf(edge.getSecond()) == 1;
  }

  public static <V,E> void toDotFile(Graph<V, E> graph, File f)
  {
    PrintWriter output = BriefIO.output(f);
    DOTExporter<V, E> exporter = new DOTExporter<>(new IntegerNameProvider<>(), new StringNameProvider<>(), null);
    exporter.export(output, graph);
    output.close();
  }
}
