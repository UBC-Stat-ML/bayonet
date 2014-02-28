package blang.mcmc;

import java.util.Random;



public interface MHProposalDistribution extends Operator
{
  /**
   * 
   * @param rand
   * @return log proposal ratio
   */
  public Proposal propose(Random rand);
  
  public static interface Proposal
  {
    public double logProposalRatio();
    public void acceptReject(boolean accept);
  }
}
