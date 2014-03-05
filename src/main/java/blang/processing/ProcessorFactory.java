package blang.processing;

import java.util.List;

import blang.ProbabilityModel;



public interface ProcessorFactory
{
  public List<? extends Processor> build(ProbabilityModel model) ;
}
