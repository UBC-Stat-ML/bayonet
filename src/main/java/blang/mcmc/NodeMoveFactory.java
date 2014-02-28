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



public class NodeMoveFactory implements MoveFactory
{
  public final AnnotationBasedFactory<Object, Samplers, Move> annotationBasedFactory;
  
  public NodeMoveFactory(boolean useAnnotations) 
  {
    this.annotationBasedFactory = new AnnotationBasedFactory<Object, Samplers, Move>(useAnnotations, Samplers.class);
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

    @Override
    public Move produce(
        Object variable,
        Class<? extends Move> moveType)
    {
      List<Factor> factors = model.neighborFactors(variable);
      
      List<Field> fieldsToPopulate = ReflexionUtils.getAnnotatedDeclaredFields(moveType, ConnectedFactor.class, true);
      if (!Utils.isFactorAssignmentCompatible(factors, fieldsToPopulate))
        return null;
      
      // instantiate via empty constructor
      Operator instantiated = ReflexionUtils.instantiate(moveType);
      
      // fill the fields via annotations
      Utils.assignFactorConnections(instantiated, factors, fieldsToPopulate);
      
      // fill the variable node too; make sure there is only one such field
      Utils.assignVariable(instantiated, variable);
      
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
  
//  private final boolean useAnnotations;
//  
//  /**
//   * 
//   * @param useAnnotations Whether to use @Samplers annotations in addition to the one
//   *  included manually via include()
//   */

//  
//  private final Set<Class<? extends Operator>> 
//    exclusions = Sets.newHashSet();
//  
//  private final Map<Class<?>, Set<Class<? extends Operator>>>
//    inclusions = Maps.newHashMap();
//  
//  public void exclude(Class<? extends Operator> opType)
//  {
//    exclusions.add(opType);
//  }
//  
//  
//  public void addOperator(Class<?> variableType, Class<? extends Operator> opType)
//  {
//    BriefMaps.getOrPutSet(inclusions, variableType).add(opType);
//  }
//  
//
//  @Override
//  public List<Move> build(ProbabilityModel model)
//  {
//    List<Move> result = Lists.newArrayList();
//
//    loop:for (Object variable : model.getLatentVariables())
//    {
//      Samplers annotation = variable.getClass().getAnnotation(Samplers.class);
//      
//      Set<Class<? extends Operator>> moveTypes = inclusions.get(variable.getClass());
//      
//      if (annotation == null && moveTypes == null)
//        continue loop;
//      
//      if (useAnnotations)
//        moveTypes.addAll(Arrays.asList(annotation.value()));
//      
//      List<Factor> factors = model.neighborFactors(variable);
//    
//      for (Class<? extends Operator> moveType : moveTypes)
//        if (!exclusions.contains(moveType))
//        {
//          List<Field> fieldsToPopulate = ReflexionUtils.getAnnotatedDeclaredFields(moveType, ConnectedFactor.class, true);
//          if (Utils.isFactorAssignmentCompatible(factors, fieldsToPopulate))
//          {
//            // instantiate via empty constructor
//            Operator instantiated = ReflexionUtils.instantiate(moveType);
//            
//            // fill the fields via annotations
//            Utils.assignFactorConnections(instantiated, factors, fieldsToPopulate);
//            
//            // fill the variable node too; make sure there is only one such field
//            Utils.assignVariable(instantiated, variable);
//            
//            // check if MHProposal or Move, act accordingly; make sure it is not both
//            boolean isMHProposal = instantiated instanceof MHProposalDistribution;
//            boolean isSingleNodeMove = instantiated instanceof SingleNodeMove;
//            
//            if ((isMHProposal && isSingleNodeMove) || (!isMHProposal && !isSingleNodeMove))
//              throw new RuntimeException("" + moveType.getSimpleName() + " should be exactly one of " 
//                  + MHProposalDistribution.class.getSimpleName() + " or " + SingleNodeMove.class.getSimpleName());
//            
//            if (isMHProposal)
//            {
//              MHProposalDistribution proposal = (MHProposalDistribution) instantiated;
//              result.add(new MHMove(proposal, factors, Collections.singletonList(variable)));
//            }
//            else if (isSingleNodeMove)
//            {
//              SingleNodeMove move = (SingleNodeMove) instantiated;
//              move.setVariable(variable);
//              result.add(move);
//            }
//            else
//              throw new RuntimeException();
//            
//          }
//        }
//    }
//    
//    return result;
//  }
//  
////public static List<Pair<Object,Class<?>>> matchOperatorsFromAnnotation(ProbabilityModel model, List<Object> objects, Class<? extends Annotation> annotationClass)
////{
////  List<Pair<Object,Class<?>>> result = Lists.newArrayList();
////  
////  loop:for (Object variable : objects)
////  {
////    Annotation annotation = variable.getClass().getAnnotation(annotationClass); 
////    
////    if (annotation == null)
////      continue loop;
//////      throw new RuntimeException("All the types of the latent variables in the model should " +
//////          "have a @" + DefaultSamplers.class.getSimpleName() + " annotation. This is missing for " + 
//////          variable.getClass().getName());
////    
////    Class<?> [] moveTypes = (Class<?>[]) ReflexionUtils.callMethod(variable, "value");
////    List<Factor> factors = model.neighborFactors(variable);
////  
////    for (Class<?> moveType : moveTypes)
////    {
////      List<Field> fieldsToPopulate = ReflexionUtils.getAnnotatedDeclaredFields(moveType, ConnectedFactor.class, true);
////      if (Utils.isFactorAssignmentCompatible(factors, fieldsToPopulate))
////      {
////        // instantiate via empty constructor
////        Object instantiated = ReflexionUtils.instantiate(moveType);
////        
////        // fill the fields via annotations
////        Utils.assignFactorConnections(instantiated, factors, fieldsToPopulate);
////        
////        // fill the variable node too; make sure there is only one such field
////        Utils.assignVariable(instantiated, variable);
////        
////        // check if MHProposal or Move, act accordingly; make sure it is not both
////        boolean isMHProposal = instantiated instanceof MHProposalDistribution;
////        boolean isSingleNodeMove = instantiated instanceof SingleNodeMove;
////        
////        if ((isMHProposal && isSingleNodeMove) || (!isMHProposal && !isSingleNodeMove))
////          throw new RuntimeException("" + moveType.getSimpleName() + " should be exactly one of " 
////              + MHProposalDistribution.class.getSimpleName() + " or " + SingleNodeMove.class.getSimpleName());
////        
////        if (isMHProposal)
////        {
////          MHProposalDistribution proposal = (MHProposalDistribution) instantiated;
////          result.add(new MHMove(proposal, factors, Collections.singletonList(variable)));
////        }
////        else if (isSingleNodeMove)
////        {
////          SingleNodeMove move = (SingleNodeMove) instantiated;
////          move.setVariable(variable);
////          result.add(move);
////        }
////        else
////          throw new RuntimeException();
////        
////      }
////    }
////  }
////  
////  
////  return result;
////  
//////  Object test = new Object();
//////  Annotation testA = test.getClass().getAnnotation(annotationClass);
//////  // hack to get around the fact annotations can't inherit
//////  Class<?> annotatedType = 
////}

}
