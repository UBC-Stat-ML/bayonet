package blang.mcmc;

import java.util.List;
import java.util.Random;

import blang.factors.Factor;
import blang.variables.RealVariable;
import briefj.BriefIO;
import briefj.BriefLog;
import briefj.BriefStrings;



public class RealVariableMHProposal implements MHProposalDistribution
{
  @SampledVariable RealVariable variable;
  
  @ConnectedFactor List<Factor> connectedFactors;

  // TODO: implement some adaptation/optimization
  
  private double savedValue = Double.NaN;
  
  @Override
  public Proposal propose(Random rand)
  {
    if (!Double.isNaN(savedValue))
      throw new RuntimeException();
    savedValue = variable.getValue();
    double nextGaussian = rand.nextGaussian();
    
    final double newValue = savedValue + nextGaussian;
    variable.setValue(newValue);
    return new ProposalRealization();
  }
  
  private class ProposalRealization implements Proposal
  {

    @Override
    public double logProposalRatio() { return 0.0; } // proposal log ratio is zero since Gaussian is symmetric

    @Override
    public void acceptReject(boolean accept)
    {
      if (!accept)
        variable.setValue(savedValue);
      savedValue = Double.NaN;
    }
  }
  
}
