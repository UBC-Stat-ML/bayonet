package blang.processing;


/**
 * A processor is used to do useful stuff on MCMC samples.
 * 
 * Examples of applications: storing the MCMC samples, computing
 * summary statistics, creating plots, doing monitoring.
 * 
 * This is also useful to add deterministic functions of a 
 * probability model, and to monitor the distribution of values
 * of this deterministic function when averaging over the 
 * probability model's posterior.
 * 
 * Processors are also useful for testing. For example, 
 * consider a case where you have a node in a model that is 
 * not real-valued, but you would like to run tests such as
 * checkStationarity which rely on real valued statistics. 
 * Then having a processor implement RealValued will allow you
 * to adapt the test to the non-real variable of the model.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface Processor
{
  /**
   * This function is called periodically by MCMCAlgorithms.
   * The processor can access various information on the current state
   * of the MCMC chain via the provided context.
   * 
   * @param context
   */
  public void process(ProcessorContext context);
}
