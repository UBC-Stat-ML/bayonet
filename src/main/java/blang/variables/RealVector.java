package blang.variables;

import blang.annotations.Processors;
import blang.annotations.Samplers;
import blang.mcmc.RealVectorMHProposal;

@Samplers({
	RealVectorMHProposal.class
})
@Processors({
	RealVectorProcessor.class
})
public class RealVector implements RealVectorInterface
{
	private int dim;
	private RealVariable [] vector;  
	
	public RealVector(double [] vector)
	{
		this.dim = vector.length;
		this.vector = new RealVariable[this.dim];
		for (int d = 0; d < dim; d++)
		{
			this.vector[d] = new RealVariable(vector[d]);
		}
	}
	
	public static RealVector ones(int dim)
	{
		double [] vector = new double[dim];
		for (int d = 0; d < dim; d++)
		{
			vector[d] = 1.0;
		}
		return new RealVector(vector);
	}
	
	public RealVariable getComponent(int d)
	{
		return vector[d];
	}
	
	public void setComponent(int d, double val)
	{
		if (vector[d] == null)
		{
			vector[d] = new RealVariable(val);
		}
		else
			vector[d].setValue(val);
	}

	@Override
	public double[] getVector() 
	{
		double [] vec = new double[dim];
		for (int d = 0; d < dim; d++)
		{
			vec[d] = vector[d].getValue();
		}
		
		return vec;
	}

	@Override
	public void setVector(double[] values) 
	{
		for (int d = 0; d < dim; d++)
		{
			if (vector[d] == null)
			{
				vector[d] = new RealVariable(values[d]);
			}
			else
			{
				vector[d].setValue(values[d]);
			}
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int d = 0; d < dim; d++)
		{
			sb.append(this.vector[d].getValue());
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
