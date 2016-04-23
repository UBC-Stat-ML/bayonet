package blang.mcmc;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import blang.ProbabilityModel;
import blang.annotations.Samplers;
import blang.annotations.util.AnnotationBasedFactory;
import blang.annotations.util.AnnotationBasedFactory.Producer;
import blang.factors.Factor;
import briefj.ReflexionUtils;


/**
 * Use the annotation Samplers to instantiate and match moves to variables
 * in a ProbabilityModel.
 * 
 * In summary, for each variable in a ProbabilityModel, NodeMoveFactory will look 
 * for the arguments specified in the Samplers annotation. Those can be either
 * Moves, or MHProposals (which are turned into Moves via MHMoves).
 * 
 * See Move for details on this matching and instatiation process.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class NodeMoveFactory implements MoveFactory
{
  public final AnnotationBasedFactory<Object, Samplers, Move> annotationBasedFactory;
  
  public NodeMoveFactory() 
  {
    this.annotationBasedFactory = new AnnotationBasedFactory<Object, Samplers, Move>(Samplers.class);
  }
  
  public void setUseAnnotation(boolean use)
  {
    this.annotationBasedFactory.setUseAnnotation(use);
  }
  
  @Override
  public List<Move> build(ProbabilityModel model)
  {
    NodeMoveProducer producer = new NodeMoveProducer(model);
    return annotationBasedFactory.build(model.getLatentVariables(), producer);
  }
  
  private static class NodeMoveProducer implements Producer<Object, Move>
  {
    private final ProbabilityModel model;
    
    public NodeMoveProducer(ProbabilityModel model)
    {
      this.model = model;
    }

    /**
     * 
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Move produce(
        Object variable,
        Class moveType)
    {
      List<Factor> factors = model.neighborFactors(variable);
      
      List<Field> fieldsToPopulate = ReflexionUtils.getAnnotatedDeclaredFields(moveType, ConnectedFactor.class, true);
      if (!NodeMoveUtils.isFactorAssignmentCompatible(factors, fieldsToPopulate))
        return null;
      
      // instantiate via empty constructor
      @SuppressWarnings("unchecked")
      Operator instantiated = (Operator) ReflexionUtils.instantiate(moveType);
      
      // fill the fields via annotations
      NodeMoveUtils.assignFactorConnections(instantiated, factors, fieldsToPopulate);
      
      // fill the variable node too; make sure there is only one such field
      NodeMoveUtils.assignVariable(instantiated, variable);
      
      // check if MHProposal or Move, act accordingly; make sure it is not both
      boolean isMHProposal = instantiated instanceof MHProposalDistribution;
      boolean isSingleNodeMove = instantiated instanceof NodeMove;
      
      if ((isMHProposal && isSingleNodeMove) || (!isMHProposal && !isSingleNodeMove))
        throw new RuntimeException("" + moveType.getSimpleName() + " should be exactly one of " 
            + MHProposalDistribution.class.getSimpleName() + " or " + NodeMove.class.getSimpleName());
      
      if (isMHProposal)
      {
        MHProposalDistribution proposal = (MHProposalDistribution) instantiated;
        return new MHMove(proposal, factors, Collections.singletonList(variable));
      }
      else if (isSingleNodeMove)
      {
        NodeMove move = (NodeMove) instantiated;
        move.setVariable(variable);
        return move;
      }
      else
        throw new RuntimeException();

    }

  }
  
}
