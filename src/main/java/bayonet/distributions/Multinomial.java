package bayonet.distributions;

import java.util.Random;

import bayonet.math.NumericalUtils;
import bayonet.math.SpecialFunctions;
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
	private int numSamples;

	@FactorArgument(makeStochastic=true) public final IntegerValuedVector realization;
	@FactorArgument public final ProbabilitySimplex parameters;

	public Multinomial(int numSamples, IntegerValuedVector realization, ProbabilitySimplex parameters)
	{
		this.numSamples = numSamples;
		this.realization = realization;
		this.parameters = parameters;
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
		// TODO: improve on this -- there is a faster way to do this
		int d = this.realization.getDim();
		double [] sample = new double[d];
		for (int i = 0; i < numSamples; i++)
		{
			int index = sampleMultinomial(random, this.parameters.getVector());
			sample[index] += 1;
		}
		realization.setVector(sample);
	}

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
	 * Compute the MLE estimate of the parameters for the realization vector
	 * @param vector
	 * @return
	 */
	public static double [] mle(IntegerValuedVector vector)
	{
		final int dim = vector.getDim(); 
		int N = vector.componentSum();
		double [] probs = new double[dim];
		double [] vec = vector.getVector();
		for (int i = 0; i < dim; i++)
		{
			probs[i] = (double)vec[i]/N;
		}
		
		return probs;
	}
	
	public static double logDensity(IntegerValuedVector realization, double [] probs)
	{
		int N = realization.componentSum(); 
		//double logDensity = Math.log(CombinatoricsUtils.factorialDouble(N));
		double logDensity = SpecialFunctions.logFactorial(N);
		double [] counts = realization.getVector();
		for (int d = 0; d < realization.getDim(); d++)
		{
			//logDensity -= (Math.log(CombinatoricsUtils.factorialDouble((int)counts[d])));
			logDensity -= SpecialFunctions.logFactorial((int)counts[d]);
			logDensity += (counts[d] * Math.log(probs[d]));
		}		

		return logDensity;
	}

	/**
	 * Make another version for the object version (Double []) see above for the primitive version (double [])
	 * @param realization
	 * @param probs
	 * @return
	 */
	/* not necessary
	public static double logDensity(IntegerValuedVector realization, Double [] probs)
	{
		int N = realization.componentSum(); 
		double logDensity = Math.log(CombinatoricsUtils.factorialDouble(N));
		double [] counts = realization.getVector();
		for (int d = 0; d < realization.getDim(); d++)
		{
			logDensity -= (Math.log(CombinatoricsUtils.factorialDouble((int)counts[d])));
			logDensity += (counts[d] * Math.log(probs[d]));
		}		

		return logDensity;
	}
	*/

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

		return new Multinomial(realization.componentSum(), realization, new ProbabilitySimplex(parameters));
	}

	public Multinomial with(ProbabilitySimplex parameters)
	{
		return new Multinomial(realization.componentSum(), realization, parameters);
	}
	
	/**
	 * Convenience method that returns a new Multinomial object
	 */
	public static Multinomial newMultinomial(int dim)
	{
		IntegerValuedVector realization = IntegerValuedVector.ones(dim);
		ProbabilitySimplex parameters = ProbabilitySimplex.rep(dim, 1.0/dim);
		return new Multinomial(dim, realization, parameters);
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
        throw new RuntimeException("Negative values not allowed in multinomial parameters.");
      sum += x;
    }
    return sum;
  }
  
  public static void main(String [] args)
  {
  	// do simple sanity checks
  	IntegerValuedVector realization = new IntegerValuedVector(new double[]{30, 20, 50});
  	double [] mleParams = Multinomial.mle(realization);
  	StringBuilder sb = new StringBuilder();
  	sb.append("( ");
  	for (double param : mleParams)
  	{
  		sb.append(param + " ");
  	}
  	sb.append(")");
  	System.out.println(sb.toString());
  	
  	Multinomial mult = Multinomial.on(realization).with(new ProbabilitySimplex(mleParams)); // 0.3, 0.2, 0.5
  	System.out.println(mult.logDensity()); // should be -4.697546
  }
}
