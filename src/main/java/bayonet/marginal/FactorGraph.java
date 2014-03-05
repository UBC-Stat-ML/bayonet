package bayonet.marginal;

import org.jgrapht.UndirectedGraph;


public interface FactorGraph<V>
{
  public UndirectedGraph<V,?> getTopology();
  public FactorOperation<V> marginalizationOperation();
  
  /**
   * @param node
   * @return The unary at that node, or null if none are specified
   */
  public UnaryFactor<V> getUnary(V node);
  public BinaryFactor<V> getBinary(V marginalizedNode, V otherNode);
}
