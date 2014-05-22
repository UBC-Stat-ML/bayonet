package bayonet.distributions;

import java.util.Random;

import org.junit.Test;

import blang.MCMCAlgorithm;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.validation.CheckStationarity;
import blang.variables.IntegerValuedVector;

public class TestDirichletMultinomial extends MCMCRunner
{
	public static final IntegerValuedVector observation = new IntegerValuedVector(new int[]{8, 1, 1});
	
	@DefineFactor public final Multinomial likelihood = Multinomial.on(observation);
	@DefineFactor public final Dirichlet prior = Dirichlet.on(likelihood.parameters);

	public TestDirichletMultinomial()
	{
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
