package blang.validation;

import java.util.List;
import java.util.Map;
import java.util.Random;

import bayonet.math.NumericalUtils;
import blang.ForwardSampler;
import blang.MCMCAlgorithm;
import blang.ProbabilityModel;
import blang.factors.Factor;
import briefj.BriefCollections;
import briefj.BriefIO;
import briefj.BriefLog;
import briefj.collections.Counter;

import com.google.common.collect.Maps;

import static java.lang.System.out;

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
