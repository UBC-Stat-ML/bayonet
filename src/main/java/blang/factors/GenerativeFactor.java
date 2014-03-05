package blang.factors;

import java.util.Random;


/**
 * A factor that also supports forward simulation of a realization
 * given parameter assignments.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface GenerativeFactor extends Factor
{
  /**
   * Uses the current assignment of the parameters to sample a new 
   * random variable. Write this value in 
   * the field corresponding to the random variable realization.
   * 
   * @param random
   */
  public void generate(Random random);
}
