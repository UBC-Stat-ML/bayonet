package blang;

import java.util.List;

import blang.MCMCFactory.MCMCOptions;
import blang.mcmc.MoveSet;
import blang.processing.Processor;
import blang.processing.ProcessorContext;



public class MCMCAlgorithm
{
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

    for (int i = 0; i < options.nMCMCSweeps; i++)
    {
      sampler.sweep(options.random);
      if (i % options.thinningPeriod == 0)
        MCMCFactory.callProcessors(processors, new ProcessorContext(i, model));
    }
  }
}