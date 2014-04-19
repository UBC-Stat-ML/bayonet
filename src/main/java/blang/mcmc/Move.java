package blang.mcmc;

import java.util.List;
import java.util.Random;



/**
 * A move is an MCMC kernel, assume to be invariant with respect
 * to the target distribution but not necessarily irreducible (the
 * final MCMCAlgorithm will usually contain a mixture of Moves).
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 */
public interface Move extends Operator
{
  /**
   * Perform the move in place. 
   * 
   * @param rand
   */
  public void execute(Random rand);
  
  /**
   * Which variables are resampled by this move.
   * 
   * @return
   */
  public List<?> variablesCovered();
}
