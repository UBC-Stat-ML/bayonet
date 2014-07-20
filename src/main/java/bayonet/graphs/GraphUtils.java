package bayonet.graphs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import bayonet.marginal.algo.EdgeSorter;
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
   * A reasonable default implementation for undirected graphs.
   * @param <V>
   * @return
   */
  public static <V> UndirectedGraph<V,UnorderedPair<V, V>> newUndirectedGraph()
  {
    EdgeFactory<V, UnorderedPair<V, V>> factory = undirectedEdgeFactory();
    return new SimpleGraph<V, UnorderedPair<V, V>>(factory);
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
   * A reasonable default implementation for directed graphs..
   * 
   * @param <V>
   * @return
   */
  public static <V> DirectedGraph<V, Pair<V, V>> newDirectedGraph()
  {
    EdgeFactory<V, Pair<V, V>> factory = directedEdgeFactory();
    return new SimpleDirectedGraph<V, Pair<V,V>>(factory);
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
   * @param <V>
   * @param <E>
   * @param graph
   * @return
   */
  public static <V,E> List<Set<V>> connectedComponents(UndirectedGraph<V, E> graph)
  {
    ConnectivityInspector<V, E> connComponentsCalculator = new ConnectivityInspector<V,E>(graph);
    return connComponentsCalculator.connectedSets();
  }
  
  /**
   * Return all (np -> n1) st. np not equal to n2
   * @param <N>
   * @param f
   * @param e = (n1 -> n2)
   * @return
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
   * @param <V>
   * @param <E>
   * @param graph
   * @return
   */
  public static <V,E> List<V> linearization(
      DirectedGraph<V, E> graph)
  {
    GraphIterator<V, E> iter = new TopologicalOrderIterator<V, E>(graph);
    List<V> result = Lists.newArrayList();
    while (iter.hasNext())
      result.add(iter.next());
    return result;
  }
  
  /**
   * http://en.wikipedia.org/wiki/Complete_graph
   * 
   * @param <S>
   * @param states
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


}
