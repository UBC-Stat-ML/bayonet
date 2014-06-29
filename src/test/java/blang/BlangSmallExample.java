package blang;

import java.util.List;
import java.io.File;

import com.google.common.collect.Lists;

import bayonet.distributions.Exponential;
import bayonet.distributions.Exponential.MeanParameterization;
import bayonet.distributions.Uniform;
import bayonet.distributions.Uniform.MinMaxParameterization;
import bayonet.rplot.PlotHistogram;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.processing.ProcessorContext;
import blang.variables.RealVariable;
import briefj.run.Results;

import static  blang.variables.RealVariable.*;

/**
 * A small example to demonstrate the basic features of Blang.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class BlangSmallExample extends MCMCRunner
{


  RealVariable observation = real(0.06);
    
  @DefineFactor(onObservations = true) 
  Uniform<MinMaxParameterization> likelihood = Uniform.on(observation);
         
  @DefineFactor 
  Exponential<MeanParameterization> prior = Exponential.on(likelihood.parameters.max).withMean(10.0);

  public static void main(String [] args)
  {
    BlangSmallExample example = new BlangSmallExample();
    example.factory.mcmcOptions.nMCMCSweeps = 10000;
    example.run();
    
    File histogramFile = Results.getFileInResultFolder("posteriorOnNumberOfFutureHumans.pdf");
    PlotHistogram.from(example.samples).toPDF(histogramFile);
  }
  
 
  @Override
  protected void process(ProcessorContext context)
  {
	  samples.add(likelihood.parameters.max.getValue() - observation.getValue());
  }
  
  List<Double> samples = Lists.newArrayList();
}
