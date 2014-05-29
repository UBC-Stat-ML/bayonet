package blang.variables;

import blang.annotations.Samplers;
import blang.mcmc.ProbabilitySimplexMHProposal;

@Samplers({ProbabilitySimplexMHProposal.class})
public class ProbabilitySimplex implements RealVectorInterface {

	private double [] probs;
	
	public ProbabilitySimplex(double [] probs)
	{
		this.probs = probs; 
	}

	@Override
	public double[] getVector() 
	{
		return probs;
	}

	@Override
	public void setVector(double[] probs) 
	{
		this.probs = probs;
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
	

}
