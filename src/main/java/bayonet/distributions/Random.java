package bayonet.distributions;


import java.util.SplittableRandom;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;

import bayonet.math.NumericalUtils;

/**
 * A no-nonsense replacement for Random which by default uses under the hood 
 * the Math Commons implementation of MarsenneTwister. 
 * 
 * Can also be used in Math Commons whenever a RandomGenerator is used. 
 * But much less awkward to use than Math Commons adaptors.
 * 
 * Also allows facilitates testing in some applications via the 
 * ExhaustiveDebugRandom superclass.
 */
public class Random extends RandomAdaptor
{
  private static final long serialVersionUID = 4421628819155562219L;
  
  /**
   * Create a new instance based on the Mersenne Twister algorithm
   */
  public Random(long seed)
  {
    super(new MersenneTwister(seed));
  }
  
  /**
   * Adapts the math commons interface
   */
  public Random(RandomGenerator randomGenerator)
  {
    super(randomGenerator);
  }
  
  /**
   * Adapts the jdk interface
   */
  public Random(java.util.Random jdkRandom)
  {
    super(new Random2RandomGenerator(jdkRandom));
  }

  public boolean nextBernoulli(double p)
  {
    return nextBernoulli(this, p);
  }
  
  public static boolean nextBernoulli(java.util.Random random, double p)
  {
    if (!(p >= 0.0 || p <= 1.0))
      throw new IllegalArgumentException("Parameter should be a probability: " + p);
    return random.nextDouble() < p ? true : false;
  }
  
  public int nextCategorical(double [] probabilities)
  {
    return nextCategorical(this, probabilities);
  }
  
  /**
   * For repeated sampling, use instead ResamplingScheme which has a better 
   * amortized cost.
   */
  public static int nextCategorical(java.util.Random random, double [] probabilities)
  {
    double v = random.nextDouble();
    double sum = 0; 
    int sample = -1;
    for (int i = 0; i < probabilities.length; i++) 
    {
      double current = probabilities[i];
      if (current < 0.0)
        throw new IllegalArgumentException("Parameter should be a probability vector; index " + i + " = " + current);
      sum += current; 
      if (v < sum && sample == -1) 
        sample = i;
    }
    if (sample == -1) 
      throw new IllegalArgumentException("Parameter should be a probability vector; normalization = " + sum);
    NumericalUtils.checkIsClose(1.0, sum);
    return sample;
  }
  
  public static java.util.Random [] parallelRandomStreams(java.util.Random seeder, int nStreams)
  {
    return _parallelRandomStreams(seeder, nStreams);
  }

  public static bayonet.distributions.Random [] parallelRandomStreams(bayonet.distributions.Random seeder, int nStreams)
  {
    return (bayonet.distributions.Random[]) _parallelRandomStreams(seeder, nStreams);
  }
  
  private static java.util.Random [] _parallelRandomStreams(java.util.Random seeder, int nStreams)
  {
    boolean useBlangRandom = seeder instanceof bayonet.distributions.Random;
    java.util.Random [] result = useBlangRandom ? new bayonet.distributions.Random[nStreams] : new java.util.Random[nStreams]; 
    if (seeder instanceof ExhaustiveDebugRandom) 
      for (int i = 0; i < nStreams; i++) // assume in this case parallelization will not be used
        result[i] = seeder;
    else 
    {
      SplittableRandom splitRandom = new SplittableRandom(seeder.nextLong());
      for (int i = 0; i < nStreams; i++)
        result[i] = useBlangRandom ? 
            new bayonet.distributions.Random(splitRandom.split().nextLong()) : 
            new java.util.Random(splitRandom.split().nextLong());
    }
    return result;
  } 
  
  /**
   * Used by InstrumentedRandom to disable all continuous sampling facilities.
   */
  protected Random()
  {
    super(null);
  }
}
