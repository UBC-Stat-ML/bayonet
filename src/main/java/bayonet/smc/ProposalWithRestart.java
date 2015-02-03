package bayonet.smc;

import org.apache.commons.lang3.tuple.Pair;



/**
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 * 
 * The result of a sequence of calls
 * should be deterministic (for example the result of calling nextLogWeight()
 * after calling two times nextLogWeightSamplePair() should be the same, after 
 * calling restart(), 
 * as the result of calling nextLogWeight() after calling once nextLogWeightSamplePair()
 * and once nextLogWeight()). Here by 'equal' we mean == of double's, with no
 * numerical imprecision allowed.
 * 
 * Note that implementation will generally contain state information. 
 *
 * @param <S>
 */
public interface ProposalWithRestart<S>
{

  /**
   * @return a pair of (LOG unnormalized weight, particle)
   */
  public Pair<Double, S> nextLogWeightSamplePair();
  
  /**
   * @return LOG unnormalized weight
   */
  default public double nextLogWeight()
  {
    return nextLogWeightSamplePair().getLeft(); 
  }
  
  /**
   * @return The number of times next..() has been called. 
   */
  public int numberOfCalls();
  
  /**
   * @return A new instance that will replay the randomness of the proposal.
   */
  public ProposalWithRestart<S> restart();
  
}