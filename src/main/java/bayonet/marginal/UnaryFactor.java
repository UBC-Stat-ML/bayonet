package bayonet.marginal;



public interface UnaryFactor<V>
{
  public V connectedVariable();
  
  public double logNormalization();
}
