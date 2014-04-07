package bayonet.distributions;


import java.util.Random;

import org.junit.Test;

import bayonet.distributions.Exponential.RateParameterization;
import bayonet.distributions.Normal.MeanVarianceParameterization;
import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.mcmc.RealVariableMHProposal;
import blang.mcmc.RealVariablePeskunTypeMove;
import blang.validation.CheckStationarity;
import blang.variables.RealVariable;




public class TestNormalExponential extends MCMCRunner
{
  public final RealVariable observation = RealVariable.real(1.0);
  
  @DefineFactor public final Normal<MeanVarianceParameterization> likelihood = Normal.on(observation); //.withRate(0.001);
  @DefineFactor public final Exponential<RateParameterization> prior = Exponential.on(likelihood.parameters.variance);
  
//  @DefineFactor public final Exponential<RateParameterization> likelihood = Exponential.on(observation); //.withRate(0.001);
//  @DefineFactor public final Exponential<RateParameterization> prior = Exponential.on(likelihood.parameters.rate);
  
//  @DefineFactor public final Uniform<?> prior = Uniform.on(likelihood.parameters.mean);
  
//  @DefineFactor public final Uniform<MinMaxParameterization> tiny = Uniform.on(observation).withBounds(0, 0.1);
//  @DefineFactor public final Uniform<?> simplePrior = Uniform.on(tiny.parameters.max);
  

  @Override
  protected void setupMCMC(MCMCFactory factory)
  {
    factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
    factory.addNodeMove(RealVariable.class, RealVariableMHProposal.class);
    
//    factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
//    factory.addNodeMove(RealVariable.class, MultiplicativeRealVariableMHProposal.class);
  }
  
  @Test
  public void test()
  {
    TestNormalExponential runner = new TestNormalExponential();
    MCMCAlgorithm algo = runner.buildMCMCAlgorithm();
    algo.options.random = new Random(200001);
    System.out.println(algo.model);
    System.out.println();
    algo.options.nMCMCSweeps = 10;
    CheckStationarity check = new CheckStationarity();
    check.setShowSampleSummaryStats(true);
    check.check(algo, 10000, 0.05);
  }
  
  public static void main(String [] args)
  {
    new TestNormalExponential().test();
  }
}
