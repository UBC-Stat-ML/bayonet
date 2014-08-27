package blang;

import bayonet.distributions.Exponential;
import bayonet.distributions.Exponential.MeanParameterization;
import bayonet.distributions.Uniform;
import bayonet.distributions.Uniform.MinMaxParameterization;
import blang.annotations.DefineFactor;
import blang.processing.Processor;
import blang.processing.ProcessorContext;
import blang.variables.RealVariable;
import briefj.opt.Option;
import briefj.opt.OptionSet;
import briefj.run.Mains;


/**
 * An example that shows an instrumented example of blang,
 * i.e. using command line options and nicely organized,
 * reproducible output.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class BlangInstrumentedExample implements Runnable, Processor
{
  @Option
  public double observation = 0.5;
   
  @Option
  public double priorInit = 1.0;
  
  @OptionSet(name = "factory")
  public final MCMCFactory factory = new MCMCFactory();
  
  public class Model
  {
    @DefineFactor(onObservations = true)
    public Exponential<MeanParameterization> likelihood = Exponential.on(new RealVariable(observation)).withMean(priorInit);
     
    @DefineFactor
    public Uniform<MinMaxParameterization> prior = Uniform.on(likelihood.parameters.mean);
  }
  
  // Note: only instantiate this in run() to avoid problems with command line argument parsing
  public Model model;

  @Override
  public void run()
  {
    factory.addProcessor(this);
    model = new Model();
    MCMCAlgorithm mcmc = factory.build(model, false);
    mcmc.options.CODA = true;
    System.out.println(mcmc.model);
    mcmc.run();
  }
   
  public static void main(String [] args)
  {
    Mains.instrumentedRun(args, new BlangInstrumentedExample());
  }

  @Override
  public void process(ProcessorContext context)
  {
    
  }
}
