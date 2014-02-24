package bayonet.blang.moves;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import bayonet.blang.factors.Factor;
import bayonet.blang.moves.MHProposalDistribution.Proposal;


/**
 * A simple MH move for real random variables, using a standard 
 * normal to propose.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class MHMove implements Move
{
//  public static final MoveFactory factory = new MoveFactory() {
//    
//    @Override
//    public List<Move> build(ProbabilityModel model)
//    {
//      List<Move> result = Lists.newArrayList();
//      
//      for (MHProposal mv : model.getLatentVariables(MHProposal.class))
//        result.add(new MHMove(mv, model.neighborFactors(mv), new MoveContext(model)));
//      
//      return result;
//    }
//  };

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
    final double propRatio = proposalRealization.logProposalRatio(); //proposal.proposeInPlace(rand);
    final double logDensityAfter = computeLogUnnormalizedPotentials();
    final double ratio = Math.exp(propRatio + logDensityAfter - logDensityBefore);
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
}