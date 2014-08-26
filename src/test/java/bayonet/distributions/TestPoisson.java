package bayonet.distributions;

import java.util.Random;

import org.junit.Test;

import bayonet.distributions.Gamma.RateShapeParameterization;
import bayonet.distributions.Poisson.MeanParameterization;
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

public class TestPoisson extends MCMCRunner
{

  public final IntegerVariable observation = IntegerVariable.intVar(1);
  @DefineFactor public final Poisson<MeanParameterization> likelihood = Poisson.on(observation);
  @DefineFactor public final Gamma<RateShapeParameterization> priorMean = Gamma.on(likelihood.parameters.mean);
//  @DefineFactor public final Uniform<MinMaxParameterization> priorMean = Uniform.on(likelihood.parameters.mean).withBounds(0.8, 1.2);
  
  @Override
  protected void setupMCMC(MCMCFactory factory)
  {
    factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
    factory.addNodeMove(RealVariable.class, RealVariableMHProposal.class);
 }
  
  @Test
  public void test()
  {
    TestPoisson runner = new TestPoisson();
    MCMCAlgorithm algo = runner.buildMCMCAlgorithm();
    algo.options.random = new Random(200001);
    algo.options.CODA = false; // do not produce CODA plots from processor
    System.out.println(algo.model);
    System.out.println();
    algo.options.nMCMCSweeps = 10;
    CheckStationarity check = new CheckStationarity();
    System.out.println("List of tests: " + check.getTests().toString());
    check.getTests().remove(2);
    System.out.println("List of tests with rank based test removed: " + check.getTests().toString());
    check.setShowSampleSummaryStats(true);
    check.check(algo, 10000, 0.05);
  }
  
  public static void main(String [] args)
  {
    new TestPoisson().test();
  }
}
