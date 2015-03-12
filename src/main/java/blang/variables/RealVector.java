package blang.variables;

import blang.annotations.Processors;
import blang.factors.IIDRealVectorGenerativeFactor.VectorNormProcessor;

/**
 * Simplest implementation of RealVectorInterface
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
//@Samplers({ // see Issue #25
//	RealVectorMHProposal.class
//})
@Processors({
	VectorNormProcessor.class
})
public class RealVector implements RealVectorInterface
{
	protected int dim;
	protected double [] vector;

	public RealVector(double [] vector)
	{
		this.dim = vector.length;
		this.vector = vector;
	}
		
	public static RealVector ones(int dim)
	{
		return rep(dim, 1.0);
	}
	
	public static RealVector rep(int dim, double val)
	{
		double [] vector = new double[dim];
		for (int d = 0; d < dim; d++)
		{
			vector[d] = val;
		}
		return new RealVector(vector);
	}
	
	@Override
	public double [] getVector() 
	{
		return vector;
	}

	@Override
	public void setVector(double [] values) 
	{
		if (this.vector == null)
			this.vector = new double[values.length];
		
		for (int d = 0; d < dim; d++)
		{
				vector[d] = values[d];
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int d = 0; d < dim; d++)
		{
			sb.append(this.vector[d]);
			if (d < (dim - 1))
			{
				sb.append(", ");
			}
		}
		return ("(" + sb.toString() + ")");
	}
	
	@Override
	public int getDim()
	{
		return dim;
	}
}
