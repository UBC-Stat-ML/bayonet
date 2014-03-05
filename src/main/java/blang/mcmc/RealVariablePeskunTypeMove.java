package blang.mcmc;

import java.util.List;
import java.util.Random;

import bayonet.distributions.Multinomial;
import blang.factors.Factor;
import blang.variables.RealVariable;


/**
 * This move proceeds as follows:
 * 1. Pick a delta according to a chi-squared distribution. 
 * 2. Look at the likelihood at the current point +/- delta.
 * 3. Propose proportional to these two probabilities.
 * 4. Accept/reject. Note: this require the likelihood of the original
 *    point as well as an extra one, two deltas away from current point.
 *    
 * This can be viewed as an infinite mixture of Peskun-type 
 * Gibbs proposal.
 * 
 * We use this instead of the standard MH proposal since the 
 * latter has potential theoretical problems when the support is
 * not the full real line (the reverse move can become hard to compute).
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class RealVariablePeskunTypeMove extends NodeMove
{
  /**
   * The real variable being resampled.
   * Automatically filled in via reflection.
   */
  @SampledVariable RealVariable variable;
  
  /**
   * The factors connected to this variable.
   * Automatically filled in via reflection.
   */
  @ConnectedFactor List<Factor> connectedFactors;
  
  /**
   * Used as temporary array when proposing.
   */
  private transient double [] proposalArray = null;

  /**
   * Performs one sample step.
   */
  @Override
  public void execute(Random rand)
  {
    final double normal = rand.nextGaussian();
    final double delta = normal * normal;
    final double xOriginal =  variable.getValue();
    if (proposalArray == null)
      proposalArray = new double[2];
    proposalArray[0] = computeLogUnnormalizedPotentials(xOriginal - delta);
    proposalArray[1] = computeLogUnnormalizedPotentials(xOriginal + delta);
    if (proposalArray[0] == Double.NEGATIVE_INFINITY && proposalArray[1] == Double.NEGATIVE_INFINITY)
    {
      // both points are invalid: stay where you are
      variable.setValue(xOriginal);
      return;
    }
    final double forwardLogNorm = Multinomial.expNormalize(proposalArray);
    final int sampledIndex = Multinomial.sampleMultinomial(rand, proposalArray);
    final double moveSign = sampledIndex == 0 ? -1 : +1;
    final double xPrime = xOriginal + delta * moveSign;
    // consider the point two deltas aways from the initial one, in the direction
    // that was proposed. needed for the reverse proposal probability.
    final double xPrimePrime = xPrime + delta * moveSign; 
    proposalArray[0] = computeLogUnnormalizedPotentials(xOriginal);
    proposalArray[1] = computeLogUnnormalizedPotentials(xPrimePrime);
    final double backwardLogNorm = Multinomial.expNormalize(proposalArray);
    // Note: the acceptance simplifies to the ratio of the proposal normalizations
    final double acceptRatio = Math.exp(forwardLogNorm - backwardLogNorm);
    if (rand.nextDouble() < acceptRatio)
      variable.setValue(xPrime);
    else
      variable.setValue(xOriginal);
  }

  /**
   * Sets the variable to the given value, then compute the unnormalized
   * density of the relevant factors.
   * WARNING: should be used with care as it does not set back the original 
   * value.
   * @param value
   * @return
   */
  private double computeLogUnnormalizedPotentials(double value)
  {
    variable.setValue(value);
    double result = 0.0;
    for (Factor f : connectedFactors)
      result += f.logDensity();
    return result;
  }
}
