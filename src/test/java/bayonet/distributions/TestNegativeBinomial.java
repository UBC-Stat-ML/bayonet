package bayonet.distributions;

import java.util.Random;

import org.junit.Test;

import bayonet.distributions.Gamma.RateShapeParameterization;
import bayonet.distributions.NegativeBinomial.ProbSuccessParameterization;
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

public class TestNegativeBinomial extends MCMCRunner{

  public final IntegerVariable observation = IntegerVariable.intVar(1);

  @DefineFactor public final NegativeBinomial<ProbSuccessParameterization> likelihood = NegativeBinomial.on(observation).withProbN(0.5, 10);
  @DefineFactor public final Uniform<MinMaxParameterization> priorProb = Uniform.on(likelihood.parameters.prob).withBounds(0.5, 0.6);
  @DefineFactor public final Gamma<RateShapeParameterization> priorN = Gamma.on(likelihood.parameters.n).withRateShape(20, 200);
  
  @Override
  protected void setupMCMC(MCMCFactory factory)
  {
    factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
    factory.addNodeMove(RealVariable.class, RealVariableMHProposal.class);
 }
  
  @Test
  public void test()
  {
    TestNegativeBinomial runner = new TestNegativeBinomial();
    MCMCAlgorithm algo = runner.buildMCMCAlgorithm();
    algo.options.random = new Random(200001);
    algo.options.CODA = false; // do not produce CODA plots from processor
    System.out.println(algo.model);
    System.out.println();
    algo.options.nMCMCSweeps = 10;
    CheckStationarity check = new CheckStationarity();
    check.setShowSampleSummaryStats(true);
    check.check(algo, 10000, 0.05);
  }
  
  public static void main(String [] args)
  {
    new TestNegativeBinomial().test();
  }
  

}
