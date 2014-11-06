package bayonet.distributions;

import java.util.Random;

import org.junit.Test;

import bayonet.distributions.BetaBinomial.MeanPrecisionParameterization;
import bayonet.distributions.Exponential.RateParameterization;
import bayonet.distributions.Uniform.MinMaxParameterization;
import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.mcmc.RealVariableMHProposal;
import blang.mcmc.RealVariablePeskunTypeMove;
import blang.validation.CheckStationarity;
import blang.variables.IntegerVariable;
import blang.variables.RealVariable;

public class TestBetaBinomial extends MCMCRunner
{

  public final IntegerVariable observation = IntegerVariable.intVar(10);

//  @DefineFactor public final BetaBinomial<AlphaBetaParameterization> likelihood = BetaBinomial.on(observation).withAlphaBetaN(0.5, 0.5, 20);
//  @DefineFactor public final Exponential<RateParameterization> priorBeta = Exponential.on(likelihood.parameters.beta);
//  @DefineFactor public final Exponential<RateParameterization> priorAlpha = Exponential.on(likelihood.parameters.alpha);
  
//  @DefineFactor public final Uniform<MinMaxParameterization> priorBeta = Uniform.on(likelihood.parameters.beta).withBounds(0, 1);
//  @DefineFactor public final Uniform<MinMaxParameterization> priorAlpha = Uniform.on(likelihood.parameters.alpha).withBounds(0, 1);
  
  @DefineFactor public final BetaBinomial<MeanPrecisionParameterization> likelihood = BetaBinomial.on(observation).withMeanPrecision(0.5, 100, 20);
  @DefineFactor public final Uniform<MinMaxParameterization> priorM = Uniform.on(likelihood.parameters.m).withBounds(0.4, 0.6);
  @DefineFactor public final Exponential<RateParameterization> priorS = Exponential.on(likelihood.parameters.s);
  
  
  @Override
  protected void setupMCMC(MCMCFactory factory)
  {
    factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
    factory.addNodeMove(RealVariable.class, RealVariableMHProposal.class);
 }
  
  @Test
  public void test()
  {
    TestBetaBinomial runner = new TestBetaBinomial();
    MCMCAlgorithm algo = runner.buildMCMCAlgorithm();
    algo.options.random = new Random(200001);
    algo.options.CODA = false; 
    System.out.println(algo.model);
    System.out.println();
    algo.options.nMCMCSweeps = 10;
    CheckStationarity check = new CheckStationarity();
    check.setShowSampleSummaryStats(true);
    check.check(algo, 10000, 0.05);
  }
  
  public static void main(String [] args)
  {
    new TestBetaBinomial().test();
  }
  
}