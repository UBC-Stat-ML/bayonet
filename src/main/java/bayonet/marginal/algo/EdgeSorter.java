package bayonet.marginal.algo;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;

import bayonet.graphs.GraphUtils; 

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;



public class EdgeSorter<V>
{
  private final Map<V, Integer> postorderMap;
  private final ArrayList<V> postorder;
  private final UndirectedGraph<V, ?> graph;
  
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
  
  public ArrayList<Pair<V,V>> messages(boolean forward)
  {
    return forward ? forwardMessages() : backwardMessages();
  }
  
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
  
  // TODO change name of this to something more descriptive, in terms of orientation
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
  

  
//  public static void main(String [] args)
//  {
//    
//    SimpleGraph<String,DefaultEdge>  graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
//    graph.addVertex("z");
//    graph.addVertex("a");
////    graph.addVertex("ab");
////    graph.addVertex("ac");
////    graph.addVertex("ad");
////    graph.addVertex("aca");
////    graph.addVertex("acb");
//    graph.addEdge("z", "a");
////    graph.addEdge("a", "ab");
////    graph.addEdge("a", "ac");
////    graph.addEdge("a", "ad");
////    graph.addEdge("ac", "aca");
////    graph.addEdge("ac", "acb");
//    
//    System.out.println(EdgeSorter.newEdgeSorter(graph, "a").forwardMessages());
//    System.out.println(EdgeSorter.newEdgeSorter(graph, "a").backwardMessages());
//  }
}
