package blang.processing;

import java.util.List;

import blang.ProbabilityModel;



public interface ProcessorFactory
{
  @SuppressWarnings("rawtypes")
  public List<NodeProcessor> build(ProbabilityModel model) ;
  
//  next:
//    1- extract NodeProcessBuilder interface, make  implement this, 
//    2- add stuff in ModelRunner
//    3- make sure it's easy to use
//    4- implement RealVariableProcessor (Histogram, )
//        will need some kind of interface so that can be manipulated by test framework
}
