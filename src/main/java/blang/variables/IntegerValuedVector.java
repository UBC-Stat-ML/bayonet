package blang.variables;

import blang.annotations.Processors;
import blang.annotations.Samplers;
import blang.factors.IIDRealVectorGenerativeFactor.RandomNormProcessor;
import blang.mcmc.IntegerValuedVectorMHProposal;

/**
 * Implements RealVectorInterface and behaves exactly as RealVector except that it has a sampler specific to IntegerValuedVector
 * TODO: code refactoring
 * 
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
@Samplers({IntegerValuedVectorMHProposal.class})
@Processors({RandomNormProcessor.class})
public class IntegerValuedVector implements RealVectorInterface
{
	private double [] vector;

	public IntegerValuedVector(double [] values)
	{
		vector = values;
				
	}
	
	public static IntegerValuedVector ones(int dim)
	{
		return rep(dim, 1);
	}
	
	public static IntegerValuedVector rep(int dim, int val)
	{
		double [] values = new double[dim];
		for (int i = 0; i < dim; i++)
		{
			values[i] = val;
		}

		return new IntegerValuedVector(values);
	}
	
	@Override
	public void setVector(double [] vector)
	{
		if (this.vector == null)
			this.vector = new double[vector.length];
		
		System.arraycopy(vector, 0, this.vector, 0, vector.length);
	}
	
	public double [] getVector()
	{
		return vector;
	}
	
	public void increment(int index)
	{
		vector[index]++;
	}
	
	public int componentSum()
	{
		int sum = 0;
		for (int i = 0; i < vector.length; i++)
		{
			sum += vector[i];
		}
		return sum;
	}
	
	public int getDim()
	{
		return vector.length;
	}
	
	public IntegerValuedVector deepCopy()
	{
		double [] copy = new double[this.getDim()];
		System.arraycopy(this.vector, 0, copy, 0, getDim());
		return new IntegerValuedVector(copy);
	}
		
}
