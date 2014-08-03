package blang;

import java.util.List;

import blang.MCMCFactory.MCMCOptions;
import blang.mcmc.MoveSet;
import blang.processing.Processor;
import blang.processing.ProcessorContext;



public class MCMCAlgorithm
{
  
  public final static long startTime = System.currentTimeMillis();
  
  public final ProbabilityModel model;
  public final MoveSet sampler;
  public final List<Processor> processors;
  public final MCMCOptions options;
  MCMCAlgorithm(
      ProbabilityModel model,
      MoveSet sampler, 
      List<Processor> processors,
      MCMCOptions options)
  {
    this.options = options;
    this.model = model;
    this.sampler = sampler;
    this.processors = processors;
  }
  public void run()
  {
    if (options.thinningPeriod < 1)
      throw new RuntimeException("The thinning period should be greater or equal to 1: "+options.thinningPeriod);
    for (int i = 0; i < options.nMCMCSweeps; i++)
    {
      sampler.sweep(options.random);
      if ((i % options.thinningPeriod == 0 || i == (options.nMCMCSweeps - 1)) &&
          i > options.burnIn)
        MCMCFactory.callProcessors(processors, new ProcessorContext(i, model, options));
    }
  }
  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    result.append("Summary of model\n");
    result.append("----------------\n");
    result.append(model + "\n");
    result.append("Summary of samplers\n");
    result.append("-------------------\n");
    result.append("" + sampler);
    return result.toString();
  }
}