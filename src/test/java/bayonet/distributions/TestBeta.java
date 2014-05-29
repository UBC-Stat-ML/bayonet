package bayonet.distributions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;


import bayonet.distributions.Beta.AlphaBetaParameterization;
import bayonet.distributions.Exponential.MeanParameterization;
import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.mcmc.RealVariableMHProposal;
import blang.mcmc.RealVariablePeskunTypeMove;
import blang.validation.CheckStationarity;
import blang.variables.RealVariable;

public class TestBeta extends MCMCRunner 
{

	public final RealVariable observation = RealVariable.real(1.0);
	
	@DefineFactor public final Exponential<MeanParameterization> likelihood = Exponential.on(observation).withMean(1.0); //.withRate(0.001);
	@DefineFactor public final Beta<AlphaBetaParameterization> prior = Beta.on(likelihood.parameters.mean).withParameters(2, 2);

	@Override
	protected void setupMCMC(MCMCFactory factory)
	{
		factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
		factory.addNodeMove(RealVariable.class, RealVariableMHProposal.class);

		//	    factory.excludeNodeMove(RealVariablePeskunTypeMove.class);
		//	    factory.addNodeMove(RealVariable.class, MultiplicativeRealVariableMHProposal.class);
	}

	@Test
	public void test()
	{
		TestBeta runner = new TestBeta();
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

	//@Test
	/**
	 * generate a couple of realizations
	 *
	 */
	//TOOD: later test the empirical mean and variance and see if they're close enough to 
	// the analytic values
	public void densityTest() throws FileNotFoundException 
	{
		//final Beta<AlphaBetaParameterization> beta = Beta.on(likelihood.parameters.mean).withParameters(2, 2);
		PrintWriter pw = new PrintWriter(new FileOutputStream("testbeta.txt"));
	    
		ArrayList<Double> numList = new ArrayList<Double>();
		for (int i = 0; i < 10000; i++) {
			double temp = Beta.generate(new Random(), 2, 2);
			numList.add(temp);
			pw.println(temp);
			//System.out.println(temp);
		}
		
		//for (double d : numList)
	    //    pw.println(d);
	    pw.close();
	}
	
	public static void main(String [] args) throws FileNotFoundException
	{
		//new TestBeta().test();
		new TestBeta().densityTest();
	}

}
