package bayonet.distributions;

import java.util.Random;

import org.junit.Test;

import bayonet.distributions.Dirichlet.SymmetricParameterization;
import blang.MCMCAlgorithm;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.mcmc.RealVariableMHProposal;
import blang.mcmc.RealVariablePeskunTypeMove;
import blang.validation.CheckStationarity;
import blang.variables.IntegerValuedVector;
import blang.variables.RealVariable;

public class TestDirichletMultinomial extends MCMCRunner
{
	public static final IntegerValuedVector observation = new IntegerValuedVector(new double[]{50, 20, 30});

	@DefineFactor public final Multinomial likelihood = Multinomial.on(observation);
	@DefineFactor public final Dirichlet<SymmetricParameterization> symmetricPrior = Dirichlet.on(likelihood.parameters);

	public TestDirichletMultinomial()
	{
		factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
		factory.addNodeMove(RealVariable.class, RealVariableMHProposal.class);
	}

	@Test
	public void test()
	{
			TestDirichletMultinomial runner = new TestDirichletMultinomial();
			MCMCAlgorithm algo = runner.buildMCMCAlgorithm();
			algo.options.random = new Random(20140520);
	    System.out.println(algo.model);
	    System.out.println();
	    algo.options.nMCMCSweeps = 10;
	    CheckStationarity check = new CheckStationarity();
	    check.setShowSampleSummaryStats(true);
	    check.check(algo, 10000, 0.05);
	}

	public static void main(String [] args)
	{
		new TestDirichletMultinomial().run();
	}
}
