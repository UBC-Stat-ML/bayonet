package blang.processing;

import blang.ProbabilityModel;


/**
 * Information on a snapshot of the MCMC chain used by the processors.
 * 
 * For example: 
 * 
 * getModel().getName(variable)  will give a unique name to variables in the model
 * getModel().logDensity() will give the current logDensity
 * 
 * etc.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class ProcessorContext
{
  private final int mcmcIteration;
  private final ProbabilityModel model;
  public ProcessorContext(int mcmcIteration, ProbabilityModel model)
  {
    this.mcmcIteration = mcmcIteration;
    this.model = model;
  }
  
  /**
   * The index of the MCMC sweep (a sweep is defined as a pass over all the samplers
   * in the collection of samplers defined in an MCMCAlgorithm).
   * 
   * @return
   */
  public int getMcmcIteration()
  {
    return mcmcIteration;
  }
  
  public ProbabilityModel getModel()
  {
    return model;
  }
  
}
