package bayonet.mcmc.moves;

import java.util.Random;



public interface MHProposal
{
  /**
   * 
   * @param rand
   * @return log proposal ratio
   */
  public double proposeInPlace(Random rand);
  
  public void acceptRejectInPlace(boolean accept);
  
}
