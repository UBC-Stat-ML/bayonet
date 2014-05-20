package bayonet.distributions;

import java.util.Random;

import org.junit.Test;

import bayonet.distributions.Laplace.LocationScaleParameterization;
import bayonet.distributions.Uniform.MinMaxParameterization;
import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.mcmc.RealVariableMHProposal;
import blang.mcmc.RealVariablePeskunTypeMove;
import blang.validation.CheckStationarity;
import blang.variables.RealVariable;

public class TestLaplace extends MCMCRunner 
{
	public final RealVariable observation = RealVariable.real(1.0);

	@DefineFactor public final Laplace<LocationScaleParameterization> likelihood = Laplace.on(observation).withParameters(0.0, 1.0);
	@DefineFactor public final Uniform<MinMaxParameterization> prior = Uniform.on(likelihood.parameters.location);

	@Override
	protected void setupMCMC(MCMCFactory factor) 
	{
		factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
		factory.addNodeMove(RealVariable.class, RealVariableMHProposal.class);
	}

	@Test
	public void test()
	{
		TestLaplace runner = new TestLaplace();
		MCMCAlgorithm algo = runner.buildMCMCAlgorithm();
		algo.options.random = new Random(200001);
		System.out.println(algo.model);
		System.out.println();
		algo.options.nMCMCSweeps = 10;
		algo.options.CODA = false; 
		CheckStationarity check = new CheckStationarity();
		check.setShowSampleSummaryStats(true);
		check.check(algo, 10000, 0.05);
	}

	public static void main(String [] args)
	{
		new TestLaplace().test();
	}
}
