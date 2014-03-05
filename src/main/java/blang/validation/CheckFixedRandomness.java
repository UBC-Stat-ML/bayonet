package blang.validation;

import java.util.List;
import java.util.Random;

import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.ProbabilityModel;



public class CheckFixedRandomness
{
  // input: a pair of instrumented MCMC samplers
  private final MCMCFactory mcmcFactory;
  
  public CheckFixedRandomness(MCMCFactory mcmcFactory)
  {
    this.mcmcFactory = mcmcFactory;
  }

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

  private void checkVariables(ProbabilityModel model1, ProbabilityModel model2, boolean shouldBeEqual)
  {
    List<String> variableNames = model1.getLatentVariableNames();
    if (!variableNames.equals(model2.getLatentVariableNames()))
      throw new RuntimeException();
    for (String variableName : variableNames)
    {
      Object 
        var1 = model1.getVariableWithName(variableName),
        var2 = model2.getVariableWithName(variableName);
      
      if (var1 == var2) 
        throw new RuntimeException("Clone methods seem to not actually clone the variables, in " + variableName);
      
      boolean actual = var1.equals(var2);
      if (actual != shouldBeEqual)
        throw new RuntimeException("Expected to be equal? " + shouldBeEqual + " but observed " + actual + " for variable " + variableName);
    }
  }
}
