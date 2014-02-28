package blang;

import java.util.List;
import java.util.Random;

import blang.factors.Factor;
import blang.factors.GenerativeFactor;




public class ForwardSampler
{
  private final ProbabilityModel model;
  
  public ForwardSampler(ProbabilityModel model)
  {
    if (model.nObservedNodes() > 0)
      throw new RuntimeException("Forward samplers are only available for models with no observation.");
    this.model = model;
  }
  
  public void simulate(Random rand)
  {
    // make graph directed
    List<Factor> linearizedFactors = model.linearizedFactors();
    
    // check at same time everything is a GenerativeSampler
    for (Factor f : linearizedFactors)
    {
      if (!(f instanceof GenerativeFactor))
        throw new RuntimeException("In order to generate data, all factors need to " +
        		"implement " + GenerativeFactor.class.getName());
      ((GenerativeFactor) f).generate(rand);
    }
    
  }
}
