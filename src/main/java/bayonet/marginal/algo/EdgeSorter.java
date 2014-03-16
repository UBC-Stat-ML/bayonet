package bayonet.marginal.algo;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;

import bayonet.graphs.GraphUtils; 

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * Utilities to list edges of an undirected graph in a way 
 * suitable for the sum product algorithm.
 * 
 * Also useful in other contexts where the edges of an undirected graph 
 * needs to be oriented towards or away from a given node (directed
 * views of an undirected graphs).
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <V>
 */
public class EdgeSorter<V>
{
  private final Map<V, Integer> postorderMap;
  private final ArrayList<V> postorder;
  private final UndirectedGraph<V, ?> graph;
  
  /**
   * Note: will only list edges in the connected component of 
   * the given vertex, i.e. lastForwardVertex
   * 
   * @param <V>
   * @param graph The undirected graph.
   * @param lastForwardVertex One of the vertices with respect to
   *   which the edges will be oriented.
   * @return A new instance.
   */
  public static <V> EdgeSorter<V> newEdgeSorter(
      UndirectedGraph<V, ?> graph,
      V lastForwardVertex)
  {
    ArrayList<V> postorder = GraphUtils.postorder(graph, lastForwardVertex);
    Map<V, Integer> postorderMap = Maps.newHashMap();
    for (int i = 0; i < postorder.size(); i++)
      postorderMap.put(postorder.get(i), i);
    return new EdgeSorter<V>(postorderMap, postorder, graph);
  }
  
  /**
   * 
   * @param forward Should we list the edges in an order suitable for 
   *   the forward or backward pass of sum product?
   * @return
   */
  public ArrayList<Pair<V,V>> messages(boolean forward)
  {
    return forward ? forwardMessages() : backwardMessages();
  }
  
  /**
   * List edges pointing towards the root in post-order.
   * @return
   */
  public ArrayList<Pair<V,V>> forwardMessages()
  {
    ArrayList<Pair<V,V>> result = Lists.newArrayList();
    for (int i = 0; i < postorder.size(); i++)
      if (i != postorder.size() - 1) // in a tree, there are 1 less edge than the number of vertices
      {
        V source = postorder.get(i);
        V destination = postorderNeighborSuccessor(source);
        result.add(Pair.of(source, destination));
      }
    return result;
  }
  
  /**
   * List edges pointing away from the root. This method uses the reversal of the
   * order used in forwardMessages() (and also reverse the direction of each edge).
   * @return
   */
  public ArrayList<Pair<V,V>> backwardMessages()
  {
    ArrayList<Pair<V,V>> result = Lists.newArrayList();
    for (int i = postorder.size() - 1; i >= 0; i--)
      if (i != postorder.size() - 1) // in a tree, there are 1 less edge than the number of vertices
      {
        V destination = postorder.get(i);
        V source = postorderNeighborSuccessor(destination);
        result.add(Pair.of(source, destination));
      }
    return result;
  }
  
  private EdgeSorter(
      Map<V, Integer> postorderMap,
      ArrayList<V> postorder, UndirectedGraph<V, ?> graph)
  {
    this.postorderMap = postorderMap;
    this.postorder = postorder;
    this.graph = graph;
  }

  private final V postorderNeighborSuccessor(V vertex)
  {
    int vertexOrder = postorderMap.get(vertex);
    V result = null;
    for (V neighbor : Graphs.neighborListOf(graph, vertex))
    {
      int neighborOrder = postorderMap.get(neighbor);
      if (neighborOrder > vertexOrder)
      {
        if (result != null)
          throw new RuntimeException();
        result = neighbor;
      }
    }
    return result;
  }
}
