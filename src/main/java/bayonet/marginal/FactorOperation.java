package bayonet.marginal;

import java.util.List;



public interface FactorOperation<V>
{
  public UnaryFactor<V> pointwiseProduct(List<? extends UnaryFactor<V>> unaries);
  
  /**
   * 
   * @param <V>
   * @param binary
   * @param unariesOnMarginalized Unary factors on the variable to marginalize
   * @return a unary on the other variable
   */
  public UnaryFactor<V> marginalize(BinaryFactor<V> binary, List<UnaryFactor<V>> unariesOnMarginalized);
}
