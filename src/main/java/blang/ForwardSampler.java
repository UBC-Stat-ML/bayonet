package blang;

import java.util.List;
import java.util.Random;

import blang.factors.Factor;
import blang.factors.GenerativeFactor;




public class ForwardSampler
{
  private final ProbabilityModel model;
  
  public ForwardSampler(Object modelSpecification)
  {
    this.model = new ProbabilityModel(modelSpecification);
  }
  
  public ForwardSampler(ProbabilityModel model)
  {
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
        		"implement " + GenerativeFactor.class.getName() + ". This is not the case for " + f.getClass());
      ((GenerativeFactor) f).generate(rand);
    }
    
  }
}
