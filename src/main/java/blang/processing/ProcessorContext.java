package blang.processing;

import blang.ProbabilityModel;



public class ProcessorContext
{
  private final int mcmcIteration;
  private final ProbabilityModel model;
  public ProcessorContext(int mcmcIteration, ProbabilityModel model)
  {
    this.mcmcIteration = mcmcIteration;
    this.model = model;
  }
  
  
  
}
