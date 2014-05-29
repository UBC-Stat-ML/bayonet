package bayonet.distributions;

import java.util.Random;

import org.junit.Test;

import bayonet.distributions.Gamma.RateShapeParameterization;
import bayonet.distributions.Gamma.ScaleShapeParameterization;
import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.mcmc.RealVariableMHProposal;
import blang.mcmc.RealVariablePeskunTypeMove;
import blang.validation.CheckStationarity;
import blang.variables.RealVariable;

public class TestGamma extends MCMCRunner{

  public final RealVariable observation = RealVariable.real(1.0);
  
//  @DefineFactor public final Gamma<RateShapeParameterization> likelihood = Gamma.on(observation).withRateShape(2, 1);
//  @DefineFactor public final Gamma<RateShapeParameterization> priorShape = Gamma.on(likelihood.parameters.shape);
//  @DefineFactor public final Gamma<RateShapeParameterization> priorRate = Gamma.on(likelihood.parameters.rate);

  @DefineFactor public final Gamma<ScaleShapeParameterization> likelihood = Gamma.on(observation).withScaleShape(0.5, 1);
  @DefineFactor public final Gamma<RateShapeParameterization> priorShape = Gamma.on(likelihood.parameters.shape);
  @DefineFactor public final Gamma<ScaleShapeParameterization> priorScale = Gamma.on(likelihood.parameters.scale).withScaleShape(2, 1);
  
  
  
  @Override
  protected void setupMCMC(MCMCFactory factory)
  {
    factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
    factory.addNodeMove(RealVariable.class, RealVariableMHProposal.class);
 }
  
  @Test
  public void test()
  {
    TestGamma runner = new TestGamma();
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
    new TestGamma().test();
  }
  

}
