package bayonet.marginal;

import java.util.List;


/**
 * The algorithms that do marginalization and pointwise products.
 * These are used by the sum product (meta)-algorithm to do probabilistic
 * inference.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface FactorOperations<V>
{
  /**
   * 
   * @param unaries
   * @return
   */
  public UnaryFactor<V> pointwiseProduct(List<? extends UnaryFactor<V>> unaries);
  
  /**
   * 
   * @param binary
   * @param unariesOnMarginalized Unary factors on the variable to marginalize
   * @return a unary on the other variable
   */
  public UnaryFactor<V> marginalize(BinaryFactor<V> binary, List<UnaryFactor<V>> unariesOnMarginalized);
}
