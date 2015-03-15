package bayonet.distributions;

import java.util.Random;

import org.junit.Test;

import bayonet.distributions.Uniform.MinMaxParameterization;
import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.mcmc.RealVariablePeskunTypeMove;
import blang.validation.CheckStationarity;
import blang.variables.IntegerVariable;

public class TestBinomial extends MCMCRunner
{

  public final IntegerVariable observation = IntegerVariable.intVar(50);

  @DefineFactor public final Binomial<bayonet.distributions.Binomial.ProbSuccessParameterization> likelihood = Binomial.on(observation).withProbN(0.5, 100);
  @DefineFactor public final Uniform<MinMaxParameterization> priorProb = Uniform.on(likelihood.parameters.prob).withBounds(0.4, 0.6);
  
  
  @Override
  protected void setupMCMC(MCMCFactory factory)
  {
    factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
 }
  
  @Test
  public void test()
  {
    TestBinomial runner = new TestBinomial();
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
    new TestBinomial().test();
  }
  
}
