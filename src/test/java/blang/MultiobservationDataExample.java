package blang;

import java.util.List;
import java.io.File;

import com.google.common.collect.Lists;

import bayonet.distributions.Exponential;
import bayonet.distributions.Exponential.MeanParameterization;
import bayonet.distributions.Uniform;
import bayonet.distributions.Uniform.MinMaxParameterization;
import bayonet.distributions.UnivariateRealData;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.processing.ProcessorContext;
import blang.variables.RealVariable;
import blang.variables.ReadRealVariableData;
import static  blang.variables.RealVariable.*;

/**
 * A small example to demonstrate multiobservational real univariate data in Blang
 * 
 * This example is based off of code written by Alex. See BlangSmallExample to compare with univariate case.
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 * @author Sean Jewell (jewellsean@gmail.com)
 *
 */ 
public class MultiobservationDataExample extends MCMCRunner
{
  /* ******************************************************************* *
     Specification of the probability model used for this small example: 
   * ******************************************************************* */
  
  RealVariable observation = real();
  
  File file = new File("./data/realVectorIID.csv");
  ReadRealVariableData data = new ReadRealVariableData(file);
  
  @DefineFactor(onObservations = true) 
  UnivariateRealData<Uniform<MinMaxParameterization>> likelihood = new UnivariateRealData<Uniform<MinMaxParameterization>>(data, Uniform.on(observation));
       
  @DefineFactor 
  Exponential<MeanParameterization> prior = Exponential.on(likelihood.distFamily.parameters.max).withMean(10.0);

  
  /* ******************************************************************* */ 

  public static void main(String [] args)
  {
    MultiobservationDataExample example = new MultiobservationDataExample();
    example.factory.mcmcOptions.nMCMCSweeps = 10000;
    example.run();
   }
  
 
  @Override
  protected void process(ProcessorContext context){}
 
  List<Double> samples = Lists.newArrayList();
}
