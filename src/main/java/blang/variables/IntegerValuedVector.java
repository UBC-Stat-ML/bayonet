package blang.variables;

import org.apache.commons.math3.util.CombinatoricsUtils;

import blang.annotations.Processors;
import blang.annotations.Samplers;
import blang.mcmc.IntegerValuedVectorMHProposal;

@Samplers({IntegerValuedVectorMHProposal.class})
@Processors({IntegerValuedVectorProcessor.class})
public class IntegerValuedVector implements RealVectorInterface
{
	private double [] vector; // hack, should be an integer array -- need to ensure that only the integers are tracked
	private TestFunction g;

	public IntegerValuedVector(int [] values, TestFunction g)
	{
		vector = new double[values.length];
		for (int i = 0; i < values.length; i++)
		{
			vector[i] = values[i];
		}
		
		this.g = g;
	}
	
	public IntegerValuedVector(int [] values)
	{
		this(values, new DefaultTestFunction());
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
		int [] copy = new int[this.getDim()];
		System.arraycopy(this.vector, 0, copy, 0, getDim());
		return new IntegerValuedVector(copy, this.g);
	}
		
	@Override
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
			// TODO: until you find a better default test function... average of the pairwise differences?
			int D = vector.getDim();
			double [] vec = vector.getVector();
			double diffSum = 0.0;
			for (int i = 0; i < D; i++)
			{
				for (int j = (i+1); j < D; j++)
				{
					diffSum += Math.abs(vec[i] - vec[j]);
				}
			}
			diffSum /= CombinatoricsUtils.binomialCoefficientDouble(D, 2);
			return diffSum;
		}
		
	}
}
