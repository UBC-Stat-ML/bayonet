package bayonet.marginal;

import java.util.Random;




public interface Sampler<V>
{
  /**
   * Sample from a unary factor.
   * 
   * @param rand
   * @param factor
   * @return A dirac delta on the sample
   */
  public UnaryFactor<V> sample(Random rand, UnaryFactor<V> factor);
}