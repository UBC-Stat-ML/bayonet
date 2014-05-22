package bayonet.distributions;

import java.util.Random;

import org.apache.commons.math3.util.ArithmeticUtils;

import bayonet.math.NumericalUtils;
import blang.annotations.FactorArgument;
import blang.factors.GenerativeFactor;
import blang.variables.IntegerValuedVector;
import blang.variables.ProbabilitySimplex;


/**
 * Utilities for Multinomial distributions.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Multinomial implements GenerativeFactor
{
	private int N;
	
	@FactorArgument(makeStochastic=true) public final IntegerValuedVector realization;
	@FactorArgument public final ProbabilitySimplex parameters;

	public Multinomial(int N, IntegerValuedVector realization, ProbabilitySimplex parameters)
	{
		this.N = N;
		this.realization = realization;
		this.parameters = parameters;
	}
	
	/**
	 * Set the parameters
	 * @param probs
	 */
	public void setProbs(double [] probs)
	{
		this.parameters.setVector(probs);
	}
	
	public double [] getProbs()
	{
		return this.parameters.getVector();
	}
	

	@Override
	public double logDensity() 
	{
	  return logDensity(realization, parameters.getVector());
	}

	@Override
	public void generate(Random random) 
	{
		// set the value for the realization -- repeatedly draw from sampleMultinomial()
		// TODO: improve on this -- may be there is a faster way to do this
		int d = this.realization.getDim();
		int [] sample = new int[d];
		for (int i = 0; i < N; i++)
		{
			int index = sampleMultinomial(random, this.getProbs());
			sample[index] += 1;
		}
		realization.setVector(sample);
	}
	
	public static int [] generate(Random random, int N, double [] probs)
	{
		int [] values = new int[probs.length];
		for (int i = 0; i < N; i++)
		{
			int index = sampleMultinomial(random, probs);
			values[index] += 1;
		}
		return values;
	}

	public static double [] mle(IntegerValuedVector vector)
	{
		final int dim = vector.getDim(); 
		int N = vector.getSum();
		double [] probs = new double[dim];
		int [] vec = vector.getVector();
		for (int i = 0; i < dim; i++)
		{
			probs[i] = (double)vec[i]/N;
		}
		
		return probs;
	}
	
	public static double logDensity(IntegerValuedVector realization, double [] probs)
	{
		int N = realization.getSum();
		double logDensity = Math.log(ArithmeticUtils.factorialDouble(N));
		int [] counts = realization.getVector();
		for (int d = 0; d < realization.getDim(); d++)
		{
			logDensity -= (Math.log(ArithmeticUtils.factorialDouble(counts[d])));
			logDensity += (counts[d] * Math.log(probs[d]));
		}		

		return logDensity;
	}

	/**
	 * Create a new Multinomial object with default parameter (1/K, ..., 1/K)
	 * @param realization
	 * @return
	 */
	public static Multinomial on(IntegerValuedVector realization)
	{
		int dim = realization.getDim();
		double val = 1.0/dim;
		double [] parameters = new double[dim];
		for (int d = 0; d < dim; d++)
		{
			parameters[d] = val;
		}
		
		return new Multinomial(realization.getSum(), realization, new ProbabilitySimplex(parameters));
	}
	
	public Multinomial with(ProbabilitySimplex parameters)
	{
		return new Multinomial(realization.getSum(), realization, parameters);
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
   * Destructively normalize and returns the normalization
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
        throw new RuntimeException("Negative values not allowed in multinomial parameters.");
      sum += x;
    }
    return sum;
  }
}
