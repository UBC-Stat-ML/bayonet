package blang.validation;

import java.util.List;
import java.util.Random;

import org.junit.Assert;

import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.ProbabilityModel;
import blang.variables.RealValued;


/**
 * Check if cloning the model and running each with an indep MCMC
 * with the same seed leads to the same final state. Important
 * for reproducibility and debugging.
 * 
 * Currently only check equality of the contents of real variables 
 * this test on.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class CheckFixedRandomness
{
  /**
   * 
   */
  private final MCMCFactory mcmcFactory;
  
  /**
   * 
   * @param mcmcFactory
   */
  public CheckFixedRandomness(MCMCFactory mcmcFactory)
  {
    this.mcmcFactory = mcmcFactory;
  }

  /**
   * 
   * @param modelSpecification The object containing the DeclareFactor declarations
   */
  public void check(Object modelSpecification)
  {
    final int nIters = mcmcFactory.mcmcOptions.nMCMCSweeps;
    
    MCMCAlgorithm 
      original = mcmcFactory.build(modelSpecification, false),
      mcmc1 = mcmcFactory.build(modelSpecification, true),
      mcmc2 = mcmcFactory.build(modelSpecification, true);
    
    checkVariables(mcmc1.model, mcmc2.model, true);
    checkVariables(original.model, mcmc1.model, true);
    
    long seed = mcmcFactory.mcmcOptions.random.nextLong();
    Random 
      r1 = new Random(seed),
      r2 = new Random(seed);
    
    for (int i = 0; i < nIters; i++)
    {
      mcmc1.sampler.sweep(r1);
      mcmc2.sampler.sweep(r2);
    }
    
    // check that the sampler actually moved each of the variables
    checkVariables(mcmc1.model, original.model, false); 
    
    // check randomness is fixed
    checkVariables(mcmc1.model, mcmc2.model, true);
  }

  /**
   * Checks that the value of the real valued variables in the two model,
   * make the test fail if the observed equality of the values does not coincide
   * with the argument shouldBeEqual.
   * 
   * At the same time, double check that the two models' variables are distinct
   * in terms of the memory they point to (otherwise the test would be trivial).
   * @param model1
   * @param model2
   * @param shouldBeEqual
   */
  private void checkVariables(ProbabilityModel model1, ProbabilityModel model2, boolean shouldBeEqual)
  {
    List<String> variableNames = model1.getLatentVariableNames();
    if (!variableNames.equals(model2.getLatentVariableNames()))
      throw new RuntimeException();
    boolean atLeastOneCheck = false;
    for (String variableName : variableNames)
      if (model1.getVariableWithName(variableName) instanceof RealValued)
      {
        atLeastOneCheck = true;
        
        RealValued 
          var1 = (RealValued) model1.getVariableWithName(variableName),
          var2 = (RealValued) model2.getVariableWithName(variableName);
        
        if (var1 == var2) 
          Assert.fail("Clone methods seem to not actually clone the variables, in " + variableName);
        
        boolean actual = var1.getValue() == var2.getValue(); //var1.equals(var2);
        if (actual != shouldBeEqual)
          Assert.fail("Expected to be equal? " + shouldBeEqual + " but observed " + actual + " for variable " + variableName);
      }
    if (!atLeastOneCheck)
      Assert.fail("There are not real valued random variables in the model.");
  }
}
