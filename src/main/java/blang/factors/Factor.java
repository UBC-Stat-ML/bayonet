package blang.factors;


/**
 * A factor in a factor graph.
 * 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface Factor
{
  /**
   * Note: the density should be normalized, and in log scale.
   * 
   * @return The log of the density for the current
   *  assignment of parameters and realization.
   *  
   */
  public double logDensity();
}
