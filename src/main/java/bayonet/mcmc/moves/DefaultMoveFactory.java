package bayonet.mcmc.moves;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import bayonet.mcmc.Factor;
import bayonet.mcmc.MoveFactory;
import bayonet.mcmc.ProbabilityModel;
import bayonet.mcmc.RandomVariable;
import briefj.ReflexionUtils;



public class DefaultMoveFactory implements MoveFactory
{

  @Override
  public List<Move> build(ProbabilityModel model)
  {
    List<Move> result = Lists.newArrayList();

    for (Object variable : model.getLatentVariables())
    {
      RandomVariable annotation = variable.getClass().getAnnotation(RandomVariable.class);
      
      if (annotation == null)
        throw new RuntimeException("All the types of the latent variables in the model should " +
        		"have a @" + RandomVariable.class.getSimpleName() + " annotation. This is missing for " + 
        		variable.getClass().getName());
      
      Class<?> [] moveTypes = annotation.mcmcMoves();
      List<Factor> factors = model.neighborFactors(variable);
    
      for (Class<?> moveType : moveTypes)
      {
        List<Field> fieldsToPopulate = ReflexionUtils.getAnnotatedDeclaredFields(moveType, ConnectedFactor.class, true);
        if (Utils.isFactorAssignmentCompatible(factors, fieldsToPopulate))
        {
          // instantiate via empty constructor
          Object instantiated = ReflexionUtils.instantiate(moveType);
          
          // fill the fields via annotations
          Utils.assignFactorConnections(instantiated, factors, fieldsToPopulate);
          
          // fill the variable node too!!!!; make sure there is only one such field
          Utils.assignVariable(instantiated, variable);
          
          // check if MHProposal or Move, act accordingly; make sure it is not both
          boolean isMHProposal = instantiated instanceof MHProposal;
          boolean isSingleNodeMove = instantiated instanceof SingleNodeMove;
          
          if ((isMHProposal && isSingleNodeMove) || (!isMHProposal && !isSingleNodeMove))
            throw new RuntimeException("" + moveType.getSimpleName() + " should be exactly one of " 
                + MHProposal.class.getSimpleName() + " or " + SingleNodeMove.class.getSimpleName());
          
          if (isMHProposal)
          {
            MHProposal proposal = (MHProposal) instantiated;
            result.add(new MHMove(proposal, factors, Collections.singletonList(variable)));
          }
          else if (isSingleNodeMove)
          {
            SingleNodeMove move = (SingleNodeMove) instantiated;
            move.setVariable(variable);
            result.add(move);
          }
          else
            throw new RuntimeException();
          
        }
      }
    }
    
    return result;
  }

}
