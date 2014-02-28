package bayonet.graphs;

import java.util.ArrayList;
import java.util.List;
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

import com.google.common.collect.Lists;

import fig.basic.UnorderedPair;




public class GraphUtils 
{
  public static <V> UndirectedGraph<V,UnorderedPair<V, V>> newUndirectedGraph()
  {
    EdgeFactory<V, UnorderedPair<V, V>> factory = undirectedEdgeFactory();
    return new SimpleGraph<V, UnorderedPair<V, V>>(factory);
  }
  
  public static <V> DirectedGraph<V, Pair<V, V>> newDirectedGraph()
  {
    EdgeFactory<V, Pair<V, V>> factory = directedEdgeFactory();
    return new SimpleDirectedGraph<V, Pair<V,V>>(factory);
  }
  
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
    
    if (result.size() != graph.vertexSet().size())
      throw new RuntimeException();
    
    if (!result.get(result.size() -1).equals(root))
      throw new RuntimeException("bad index: " + result.indexOf(root) + " out of " + result.size() + ", isConn=" + new ConnectivityInspector<V, E>(graph).connectedSets().size());
    
    return result;
  }
  
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
  
  public static <V,E> List<V> linearization(
      DirectedGraph<V, E> graph)
  {
    GraphIterator<V, E> iter = new TopologicalOrderIterator<V, E>(graph);
    List<V> result = Lists.newArrayList();
    while (iter.hasNext())
      result.add(iter.next());
    return result;
  }
}
