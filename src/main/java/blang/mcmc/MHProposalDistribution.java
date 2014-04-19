package blang.mcmc;

import java.util.Random;


/**
 * The interface to implement by developer of new MH moves.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface MHProposalDistribution extends Operator
{
  /**
   * This should do two thing:
   * - first, propose and modify in place variables in the model
   * - second return a Proposal object, containing the log of 
   * the ratio of 
   * backward and forward proposal kernel; and a way to undo or
   * confirm the proposal, again to be performed in place.
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
