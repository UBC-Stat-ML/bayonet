package blang.variables;

import blang.annotations.Samplers;
import blang.mcmc.ProbabilitySimplexMHProposal;

@Samplers({ProbabilitySimplexMHProposal.class})
public class ProbabilitySimplex implements RealVectorInterface 
{

	private double [] probs;
	private TestFunction g;
	
	public ProbabilitySimplex(double [] probs, TestFunction g)
	{
		this.probs = probs;
		this.g = g;
	}
	
	public ProbabilitySimplex(double [] probs)
	{
		this(probs, new DefaultTestFunction());
	}

	@Override
	public double[] getVector() 
	{
		return probs;
	}

	@Override
	public void setVector(double[] probs) 
	{
		if (probs.length != this.probs.length)
			throw new RuntimeException();
		
		System.arraycopy(probs, 0, this.probs, 0, probs.length);
	}
	
	@Override
	public int getDim()
	{
		return probs.length;
	}
	
	public ProbabilitySimplex deepCopy()
	{
		double [] copy = new double[getDim()];
		System.arraycopy(this.probs, 0, copy, 0, getDim());
		return new ProbabilitySimplex(copy);
	}
	
	public double evaluateTestFunction()
	{
		if (g == null)
			throw new RuntimeException();
		
		return g.eval(this);
	}
	
	public static class DefaultTestFunction implements TestFunction
	{

		@Override
		public double eval(RealVectorInterface vector) 
		{
			// TODO: what would be a good default test function?
			// Until you figure that out, just return the diff between maximum and minimum components?
			int D = vector.getDim();
			double [] vec = vector.getVector();
			double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
			for (int d = 0; d < D; d++)
			{
				if (vec[d] > max)
				{
					max = vec[d];
				}
				if (vec[d] < min)
				{
					min = vec[d];
				}
			}
			return (max - min);
		}
		
	}

}
