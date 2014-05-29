package blang.mcmc;

import java.util.List;
import java.util.Random;

import blang.factors.Factor;
import blang.variables.RealVariable;


/**
 * A move for real variables constrained to be positive.
 * See appendix of Lakner et al., Efficiency of Markov Chain Monte 
 * Carlo Tree Proposals in Bayesian Phylogenetics.
 * 
 * TODO: implement some adaptation/optimization
 * TODO: not currently used, need to finalize a mechanisms to choose
 * moves depending on the support of the random variables.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class MultiplicativeRealVariableMHProposal implements MHProposalDistribution
{
  @SampledVariable RealVariable variable;
  
  @ConnectedFactor List<Factor> connectedFactors;

  private double savedValue = Double.NaN;
  
  private static final double lambda = 2.0 * Math.log(2.0);
  
  @Override
  public Proposal propose(Random rand)
  {
    if (!Double.isNaN(savedValue))
      throw new RuntimeException();
    savedValue = variable.getValue();
    double u = rand.nextDouble();
    double m = Math.exp(lambda * (u - 0.5));
    
    final double newValue = savedValue * m;
    variable.setValue(newValue);
    return new ProposalRealization(Math.log(m));
  }
  
  private class ProposalRealization implements Proposal
  {
    private final double logm;
    
    private ProposalRealization(double d) { logm = d; }

    @Override
    public double logProposalRatio() { return logm; } 

    @Override
    public void acceptReject(boolean accept)
    {
      if (!accept)
        variable.setValue(savedValue);
      savedValue = Double.NaN;
    }
  }
  
}
