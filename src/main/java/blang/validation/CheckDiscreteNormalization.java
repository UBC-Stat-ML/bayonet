package blang.validation;

import static java.lang.System.out;

import java.util.List;
import java.util.Random;

import bayonet.math.NumericalUtils;
import blang.ForwardSampler;
import blang.ProbabilityModel;
import briefj.BriefCollections;
import briefj.BriefIO;
import briefj.BriefLog;
import briefj.collections.Counter;

/**
 * This can be used to check the correctness of models containing
 * a single discrete-valued latent variable. 
 * 
 * This will attempts to enumerate all possible values by calling 
 * a forward sampler (we therefore assume that the model's factors are all
 * GenerativeFactors), and accumulating the logDensity() of all unique 
 * values. 
 * 
 * The code checks that the normalization is 1 up to numerical errors.
 * 
 * In order to infer distinctness, each realization is stored via its
 * toString() method, which should therefore acts like a .equals().
 * Rationale: easier to inspect results, i.e. to order the values by
 * their decreasing probabilities. 
 * 
 * Could change this to use .equals and .hashCode in the future but seems
 * more work and no real reason for doing this (performance not as critical
 * as ease of use since this is for testing purpose only).
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class CheckDiscreteNormalization
{
  public static void check(ProbabilityModel model, Random rand, int nIterations)
  {
    ForwardSampler forwardSampler = new ForwardSampler(model);
    Counter<String> prs = new Counter<String>();
    List<Object> variables = model.getLatentVariables();
    if (variables.size() != 1)
      throw new RuntimeException("CheckDiscreteNormalization only works " +
      		"with models with one variable (there are " + variables.size() + " of them).");
    Object theVariable = BriefCollections.pick(variables);
    for (int i = 0; i < nIterations * 2; i++)
    {
      forwardSampler.simulate(rand);
      String current = theVariable.toString();
      double currentPr = Math.exp(model.logDensity());
      
      if (i > nIterations && !prs.containsKey(current))
        BriefLog.warnOnce("Warning: the test might be inaccurate (make " +
            "the problem smaller and/or try increasing nIteration until this error message disappears.");
      
      if (prs.containsKey(current))
        NumericalUtils.checkIsClose(currentPr, prs.getCount(current));
      else
        prs.setCount(current, currentPr);
    }
    
    double sum = 0.0;
    BriefIO.ensureUSLocale();
    for (String key : prs)
    {
      final double currentPr = prs.getCount(key);
      sum += currentPr;
      out.printf("%f\t%f\t%s\n", currentPr, sum, key);
    }
    out.println("\nSum: " + sum);
    NumericalUtils.checkIsClose(1.0, sum);
  }
  
  

}
