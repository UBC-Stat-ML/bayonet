package blang;


import static blang.variables.RealVariable.real;

import org.junit.Test;

import tutorialj.Tutorial;
import bayonet.distributions.Exponential;
import bayonet.distributions.Exponential.MeanParameterization;
import bayonet.distributions.Uniform;
import bayonet.distributions.Uniform.MinMaxParameterization;
import blang.MCMCAlgorithm;
import blang.MCMCRunner;
import blang.annotations.DefineFactor;
import blang.validation.CheckFixedRandomness;
import blang.validation.CheckStationarity;



/**
 * Testing the BlangSmallExample model.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class TestBlangSmallExample extends MCMCRunner
{
  @DefineFactor
  Uniform<MinMaxParameterization> likelihood = Uniform.on(real());
  
  @DefineFactor 
  Exponential<MeanParameterization> prior = Exponential.on(likelihood.parameters.max).withMean(10.0);

  /**
   * #### Testing probabilistic programs
   * 
   * Writing probabilistic programs is tricky: subtle and not-so-subtle 
   * bugs can easily go unnoticed if one is not careful.
   * 
   * Blang offers some tools to help discover bugs in probabilistic programs.
   * The main one, ``CheckStationarity``, is based on the fact that forward sampling
   * is often  easier to implement than posterior sampling, and more  
   * importantly, is typically based on different ideas than posterior sampling.
   * Both types of sampling therefore share minimal
   * code with the posterior sampling code. So the hope is that they do not share
   * bugs either, so that discrepancy between the two methods can be detected.
   * 
   * Setting up this test is fairly straightforward:
   */
  @Test
  @Tutorial(showSource = true, showSignature = false, showLink = true, linkPrefix = "src/test/java/")
  public void checkStationarity()
  {
    // Creates a model similar to the small example, but with no observations
    // (forward simulation requires that there be no observations).
    TestBlangSmallExample runner = new TestBlangSmallExample();
    
    // Build an MCMC algorithm on this model (this was done implicitly in runner.run() earlier,
    // but this time we will need a bit more control)
    MCMCAlgorithm algo = runner.buildMCMCAlgorithm();
    
    // Print a summary of the model to make sure the model is what we intend to test
    // The syntax ${someString} in this output denotes a random variable named someString.
    System.out.println("Summary of model");
    System.out.println("----------------");
    System.out.println(algo.model);
    
    // The number of MCMC sweeps to perform at each iteration of the test (more info below)
    algo.options.nMCMCSweeps = 10;
    algo.options.CODA = false; 
    
    // Actual code for setting up the test itself
    CheckStationarity check = new CheckStationarity();
    check.setShowSampleSummaryStats(true);
    System.out.println("Summary statistics of the samples");
    System.out.println("---------------------------------");
    
    // Here: 1000 is the number of test iterations (different than MCMC sweeps, see below)
    //       0.05 is a p-value threshold
    check.check(algo, 10000, 0.05);
  }
  
  /**
   * Optional question: look at the information in ``CheckStationary.java``, and 
   * explain how it works and the theory behind it.
   * 
   * Note: this test can be quite expensive for large problems. Fortunately, most 
   * bugs can be detected on fairly small artificial datasets. So you should make your 
   * model/data you test against small enough so that you can frequently and quickly 
   * run test, but large enough so that you have *code coverage*, i.e. all parts of 
   * your code are needed to cover the examples being tested.
   * 
   * An important related principle 
   * is that you should strive to test against *all* small problem instances if possible.
   * When it is not possible, do as many as possible, focusing on corner and degenerate 
   * cases.
   * 
   * Also, make sure you test your test: commit your code, introduce one small bug, 
   * and see if your test can detect it.
   */
  @Tutorial(showSource = false)
  static void moreInfo() {}
  
  /**
   * To conclude this section, a second test, which makes sure that the randomness is
   * fixed, meaning that if you run two times the code with the same seed (as 
   * encoded in the ``Random`` object), then the exact same result should be obtained.
   * 
   * Very important for reproducibility and debugging.
   */
  @Test
  @Tutorial(showSource = true, showSignature = false, showLink = true, linkPrefix = "src/test/java/")
  public void checkFixedRandomness()
  {
    TestBlangSmallExample runner = new TestBlangSmallExample();
    CheckFixedRandomness checkRand = new CheckFixedRandomness(runner.factory);
    checkRand.check(runner);
  }
}
