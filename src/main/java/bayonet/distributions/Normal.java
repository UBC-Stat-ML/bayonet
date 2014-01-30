package bayonet.distributions;


/**
 * Utilities for Normal distributions.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Normal
{
  public final static double LOG_INV_SQRT_2_PI = -Math.log(Math.sqrt(2*Math.PI));
  
  /**
   * The product of  normal densities for n iid observations x_i
   * @param mean
   * @param var
   * @param sum \sum_i x 
   * @param sumSq \sum_i x^2
   * @param n
   * @return
   */
  public static double logProb(double mean, double var, double sum, double sumSq, double n) 
  {
    return -0.5*(sumSq - 2*mean*sum + mean*mean)/var + n*(LOG_INV_SQRT_2_PI - 0.5*Math.log(var));
  }
}
