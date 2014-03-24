package bayonet.marginal;


/**
 * Represents an unnormalized measure.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <V>
 */
public interface UnaryFactor<V>
{

  
  /**
   * 
   * @return
   */
  public double logNormalization();
}
