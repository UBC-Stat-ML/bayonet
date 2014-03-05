package bayonet.marginal;



public interface BinaryFactor<V>
{
  public V marginalizedNode();
  public V otherNode();
  
  // bad idea: one could want to use just the fwd methods
//  public BinaryFactor<V> transpose();
}
