package bayonet.mcmc.moves;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import bayonet.mcmc.Factor;


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
  private final MHProposal proposal;
  private final Collection<Factor> connectedFactors;
  private final List<?> variables;
  
  public MHMove(MHProposal proposal,
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
    final double propRatio = proposal.proposeInPlace(rand);
    final double logDensityAfter = computeLogUnnormalizedPotentials();
    final double ratio = Math.exp(propRatio + logDensityAfter - logDensityBefore);
    final boolean accept = rand.nextDouble() < ratio;
    proposal.acceptRejectInPlace(accept);
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