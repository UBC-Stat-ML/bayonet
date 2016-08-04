package bayonet.distributions;

import java.util.Random;



/**
 * Continuous uniform priors on an interval [a, b).
 * 
 * P is the type of the parameterization.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Uniform//<P extends Uniform.Parameters> //implements GenerativeFactor, UnivariateRealDistribution
{
 
  
  /**
   * Simulate uniformly on the interval [min, max).
   * 
   * @param rand The source of pseudo-randomness 
   * @param min The left boundary of the inteval.
   * @param max The right boundary of the inteval.
   */
  public static double generate(Random rand, double min, double max)
  {
    if (min > max)
      throw new RuntimeException();
    return min + (max-min) * rand.nextDouble();
  }
  

}