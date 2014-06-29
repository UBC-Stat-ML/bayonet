package blang;

import java.util.Collections;
import java.util.List;


import blang.processing.Processor;
import blang.processing.ProcessorContext;
import blang.processing.ProcessorFactory;
import briefj.opt.OptionSet;



public class MCMCRunner implements Runnable
{
  @OptionSet(name = "factory")
  public final MCMCFactory factory = new MCMCFactory();
  

  
  @Override
  public void run()
  {
    MCMCAlgorithm mcmc = buildMCMCAlgorithm();
    mcmc.run();
  }
  
  public MCMCAlgorithm buildMCMCAlgorithm()
  {
    setupMCMC(factory);
    factory.addProcessorFactory(new CustomProcessor());
    return factory.build(this, false);
  }

  protected void setupMCMC(MCMCFactory factory) {}
  protected void process(ProcessorContext context) {}
  
  private class CustomProcessor implements ProcessorFactory
  {

    @Override
    public List<Processor> build(ProbabilityModel model)
    {
      Processor result = new Processor() {
        @Override
        public void process(ProcessorContext context)
        {
          MCMCRunner.this.process(context);
        }
      };
      return Collections.singletonList(result);
    }
    
  }

}
