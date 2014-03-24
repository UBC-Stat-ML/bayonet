package bayonet.distributions;

import bayonet.math.SpecialFunctions;


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
  
  /**
   * quantiles (=inverse cumulative density function)
   *
   * @param z  argument
   * @param m  mean
   * @param sd standard deviation
   * @return icdf at z
   */
  public static double quantile(double z, double m, double sd) {
      return m + Math.sqrt(2.0) * sd * SpecialFunctions.inverseErf(2.0 * z - 1.0);
  }
}
