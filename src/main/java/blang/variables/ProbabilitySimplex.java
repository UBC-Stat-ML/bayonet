package blang.variables;

import blang.annotations.Processors;
import blang.annotations.Samplers;
import blang.factors.IIDRealVectorGenerativeFactor.VectorNormProcessor;
import blang.mcmc.ProbabilitySimplexMHProposal;

/**
 * This is a data type that would be a realization of a draw from Dirichlet distribution
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
@Samplers({ProbabilitySimplexMHProposal.class})
@Processors({VectorNormProcessor.class})
public class ProbabilitySimplex implements RealVectorInterface
{
	public static double THRESHOLD = 1e-4;
	
	private double [] probs;
	
	public ProbabilitySimplex(double [] probs)
	{
		this.probs = new double[probs.length];
		setAndCheck(probs, this.probs);
	}
	
	public static ProbabilitySimplex rep(int dim, double val)
	{
		return new ProbabilitySimplex(RealVector.rep(dim, val).getVector());
	}
		
	@Override
  public double [] getVector() 
	{
	  return probs;
  }

	@Override
	public void setVector(double [] probs) 
	{
		if (probs.length != this.probs.length)
			throw new RuntimeException();
		
		setAndCheck(probs, this.probs);

	}
	
	public static void setAndCheck(double [] src, double [] dest)
	{
		double sum = 0.0;
		for (int i = 0; i < src.length; i++)
		{
			double x = src[i];
			if (x > 1.0 || x < 0.0)
				throw new RuntimeException("Not a probability.");
			sum += x;
			dest[i] = x;
		}
		
		if (Math.abs(sum - 1.0) > THRESHOLD)
			throw new RuntimeException("Not a probability simplex.");

	}
	
	public ProbabilitySimplex deepCopy()
	{
		double [] copy = new double[this.getDim()];
		System.arraycopy(this.probs, 0, copy, 0, getDim());
		return new ProbabilitySimplex(copy);
	}
	
	@Override
  public int getDim() 
	{
	  return this.probs.length;
  }

}
