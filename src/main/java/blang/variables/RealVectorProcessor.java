package blang.variables;

import blang.processing.NodeProcessor;

import blang.processing.ProcessorContext;

public class RealVectorProcessor implements NodeProcessor<RealVector>
{
	private RealVector vector;

	@Override
	public void process(ProcessorContext context) 
	{
		
	}

	@Override
	public void setReference(RealVector vector) 
	{
		this.vector = vector;
	}

}
