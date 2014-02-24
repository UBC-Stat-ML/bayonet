package blang.moves;

import java.util.List;
import java.util.Random;

import blang.RealVariable;
import blang.factors.Factor;
import blang.factors.StandardFactor;



public class RealVariableMHProposal implements MHProposalDistribution
{
  @SampledVariable RealVariable variable;
  
  @ConnectedFactor List<StandardFactor> connectedFactors;

  // TODO: implement some adaptation/optimization
  
  private double old = Double.NaN;
  
  @Override
  public Proposal propose(Random rand)
  {
    if (!Double.isNaN(old))
      throw new RuntimeException();
    old = variable.getValue();
    final double newValue = old + rand.nextGaussian();
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
        variable.setValue(old);
      old = Double.NaN;
    }
  }
  
}
