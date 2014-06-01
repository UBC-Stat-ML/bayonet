package blang.mcmc;

import java.util.List;
import java.util.Random;

import bayonet.distributions.Exponential;
import blang.factors.Factor;
import blang.variables.RealVectorInterface;

public class RealVectorMHProposal implements MHProposalDistribution
{

	  @SampledVariable RealVectorInterface variable;	  
	  @ConnectedFactor List<Factor> connectedFactors;
	  
	  private double [] savedValue = null;

	  @Override
	  public Proposal propose(Random rand)
	  {
//	    System.out.println("Computing RealVectorHMProposal");
	    if (savedValue != null)
	      throw new RuntimeException();
	    double [] variableArray = variable.getVector();
	    savedValue = variableArray.clone();
	    
	    for (int i = 0; i < variableArray.length; i++)
	    {
	    	double x = Exponential.generate(rand, 1.0);
	      variableArray[i] += 0.01 * x;
	    }
	    variable.setVector(variableArray);
	    
	    return new ProposalRealization();
	  }
	  
	  private class ProposalRealization implements Proposal
	  {

	    @Override
	    public double logProposalRatio()
	    {
	      return 0;
	    }

	    @Override
	    public void acceptReject(boolean accept)
	    {
	      if (!accept)
	      {
	        double [] variableArray = variable.getVector();
	        for (int i = 0; i < variableArray.length; i++)
	          variableArray[i] = savedValue[i];
	        variable.setVector(variableArray);
	      }
	      savedValue = null;
	    }
	    
	  }

}
