package bayonet.distributions;

import java.util.Random;

import bayonet.math.NumericalUtils;
import bayonet.math.SpecialFunctions;

/**
 * Utilities for Multinomial distributions.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Multinomial //implements GenerativeFactor, LogScaleFactor
{
	

	/**
	 * Returns double array but really contains only the integers
	 * @param random
	 * @param N
	 * @param probs
	 * @return
	 */
	public static double [] generate(Random random, int N, double [] probs)
	{
		double [] values = new double[probs.length];
		for (int i = 0; i < N; i++)
		{
			int index = sampleMultinomial(random, probs);
			values[index] += 1;
		}
		return values;
	}


	
  /**
   * Sample a single sample from a multinomial and the provided probabilities.
   * 
   * @param random
   * @param probs
   * @return The index in the array that was sampled.
   */
  public static int sampleMultinomial(Random random, double[] probs) 
  {
    NumericalUtils.checkIsClose(getNormalization(probs), 1.0, 1e-10);
    double v = random.nextDouble();
    double sum = 0; 
    for(int i = 0; i < probs.length; i++) 
    {
      sum += probs[i]; 
      if(v < sum) return i;
    }
    throw new RuntimeException();
  }
  
  /**
   * Input: log probabilities (unnormalized too), to be destructively exponentiated and normalized
   * Output: normalized probabilities
   * probs actually contains log probabilities; so we can add an arbitrary constant to make
   * the largest log prob 0 to prevent overflow problems
   * 
   * WARNING: modifies the provided logProbs in place
   * 
   * @param logProbs
   * @return The normalization in log space
   */
  public static double expNormalize(double[] logProbs) 
  {
    double max = Double.NEGATIVE_INFINITY;
    for(int i = 0; i < logProbs.length; i++)
      max = Math.max(max, logProbs[i]);
    for(int i = 0; i < logProbs.length; i++)
      logProbs[i] = Math.exp(logProbs[i]-max);
    return max + Math.log(normalize(logProbs));
  }
  
  /**
   * Destructively normalize and returns the normalization.
   * 
   * WARNING: modifies the provided logProbs in place
   * 
   * @param data
   * @return The normalization
   */
  public static double normalize(double[] data) 
  {
    double sum = getNormalization(data);
    
    if (sum == 0.0)
      throw new RuntimeException("Normalization should be positive.");
    
    if (sum != 1.0)
    {
      for(int i = 0; i < data.length; i++) 
        data[i] /= sum;
    }
    return sum;
  }
  
  /**
   * Get the normalization of the array.
   * @throws RuntimeException if some of the entries are negative.
   * @param data
   * @return
   */
  public static double getNormalization(double[] data)
  {
    double sum = 0;
    for(double x : data) 
    {
      if (x < 0.0)
        throw new RuntimeException("Negative values not allowed in multinomial parameters: " + x);
      sum += x;
    }
    return sum;
  }
  
 
}
