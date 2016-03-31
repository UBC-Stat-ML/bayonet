package blang.core;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import blang.accessibility.GraphAnalysis;
import blang.accessibility.ObjectNode;
import blang.factors.Factor;
import blang.mcmc.ConnectedFactor;
import blang.mcmc.NodeMoveUtils;
import blang.mcmc.Operator;
import briefj.ReflexionUtils;



public class SamplerBuilder
{
//  private final GraphAnalysis graphAnalysis;
  
//  public static interface OperatorClassProvider
//  {
//    public List<Class<? extends Operator>> get()
//  }
  
  /*
   * TODO: 
   *  - some kind of interface to initialize and finalize samplers
   *  - estimate of the number of FLOPS or likelihood evals needed
   */
  
  public static Operator tryInstantiate(
      Class<? extends Operator> operatorClass, 
      Object variable,
      GraphAnalysis graphAnalysis)
  {
    List<? extends Factor> factors = 
        graphAnalysis.getConnectedFactor(new ObjectNode<>(variable)).stream()
          .map(node -> node.object)
          .collect(Collectors.toList());
    
    List<Field> fieldsToPopulate = ReflexionUtils.getAnnotatedDeclaredFields(operatorClass, ConnectedFactor.class, true);
    
    if (!NodeMoveUtils.isFactorAssignmentCompatible(factors, fieldsToPopulate))
      return null;
    
    // instantiate via empty constructor
    Operator instantiated = ReflexionUtils.instantiate(operatorClass);
    
    // fill the fields via annotations
    NodeMoveUtils.assignFactorConnections(instantiated, factors, fieldsToPopulate);
    
    // fill the variable node too; make sure there is only one such field
    NodeMoveUtils.assignVariable(instantiated, variable);
    
    return instantiated;
  }
  
}
