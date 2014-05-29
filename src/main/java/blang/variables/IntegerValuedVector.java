package blang.variables;

import blang.annotations.Samplers;
import blang.mcmc.IntegerValuedVectorMHProposal;

@Samplers({IntegerValuedVectorMHProposal.class})
public class IntegerValuedVector
{
	private int [] vector;
	
	public IntegerValuedVector(int [] values)
	{
		vector = new int[values.length];
		for (int i = 0; i < values.length; i++)
		{
			vector[i] = values[i];
		}
	}
	
	public void setVector(int [] vector)
	{
		//System.arraycopy(vector, 0, this.vector, 0, this.vector.length);
		this.vector = vector;
	}
	
	public int [] getVector()
	{
		return vector;
	}
	
	public void increment(int index)
	{
		vector[index] += 1;
	}
	
	public int getSum()
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
		return new IntegerValuedVector(copy);
	}
}
