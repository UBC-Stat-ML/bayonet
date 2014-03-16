package blang.mcmc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import blang.factors.Factor;
import blang.mcmc.MHProposalDistribution.Proposal;


/**
 * A simple MH move for real random variables, using a standard 
 * normal to propose.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class MHMove implements Move
{


  //  final SummaryStatistics acceptanceProbabilities = new SummaryStatistics();
  private final MHProposalDistribution proposal;
  private final Collection<Factor> connectedFactors;
  private final List<?> variables;
  
  public MHMove(MHProposalDistribution proposal,
      Collection<Factor> connectedFactors,
      List<?> variables)
  {
    this.proposal = proposal;
    this.connectedFactors = connectedFactors;
    this.variables = variables;
  }

  @Override
  public void execute(Random rand)
  {
    final double logDensityBefore = computeLogUnnormalizedPotentials();
    Proposal proposalRealization = proposal.propose(rand);
    if (proposalRealization == null)
      return;
    final double propLogRatio = proposalRealization.logProposalRatio(); //proposal.proposeInPlace(rand);
    final double logDensityAfter = computeLogUnnormalizedPotentials();
    final double ratio = Math.exp(propLogRatio + logDensityAfter - logDensityBefore);
    final boolean accept = rand.nextDouble() < ratio;
    proposalRealization.acceptReject(accept);
//    proposal.acceptRejectInPlace(accept);
//    acceptanceProbabilities.addValue(accept ? 1.0 : 0.0);
  }
  
  /**
   * Compute the part of the density that will be affected by 
   * chaning the variable held in this object.
   * 
   * @return
   */
  private double computeLogUnnormalizedPotentials()
  {
    double result = 0.0;
    for (Factor f : connectedFactors)
      result += f.logDensity();
    return result;
  }

  @Override
  public List<?> variablesCovered()
  {
    return Collections.unmodifiableList(variables);
  }

  @Override
  public String toString()
  {
    return "MHMove [proposal=" + proposal + ", variables=" + variables + "]";
  }
}