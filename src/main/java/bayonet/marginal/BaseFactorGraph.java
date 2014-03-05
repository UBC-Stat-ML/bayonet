package bayonet.marginal;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.UndirectedGraph;

import com.google.common.collect.Maps;



public abstract class BaseFactorGraph<V> implements FactorGraph<V>
{
  protected final Map<V, UnaryFactor<V>> unaries = Maps.newHashMap();
  protected final Map<Pair<V,V>, BinaryFactor<V>> binaries = Maps.newHashMap();
  protected final UndirectedGraph<V, ?> topology;
  
  public BaseFactorGraph(UndirectedGraph<V, ?> topology)
  {
    this.topology = topology;
  }

  @Override
  public UndirectedGraph<V, ?> getTopology()
  {
    return topology;
  }
  
  @Override
  public UnaryFactor<V> getUnary(V node)
  {
    if (!topology.containsVertex(node))
      throw new RuntimeException();
    return unaries.get(node);
  }
  
  public void setUnary(V node, UnaryFactor<V> unary)
  {
    if (unaries.containsKey(node))
      throw new RuntimeException("Overwriting factors is forbidden");
    unaries.put(node, unary);
  }
  
  @Override
  public BinaryFactor<V> getBinary(V marginalizedNode, V otherNode)
  {
    if (!topology.containsEdge(marginalizedNode, otherNode))
      throw new RuntimeException();
    return binaries.get(Pair.of(marginalizedNode, otherNode));
  }
  
  public void setBinary(V marginalizedNode, V otherNode, BinaryFactor<V> factor)
  {
    Pair<V,V> key = Pair.of(marginalizedNode, otherNode);
    if (binaries.containsKey(key))
      throw new RuntimeException("Overwriting factors is forbidden");
    binaries.put(key, factor);
  }
}
