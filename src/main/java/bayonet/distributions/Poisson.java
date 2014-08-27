package bayonet.distributions;

import java.util.Random;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.util.CombinatoricsUtils;

import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.IntegerVariable;
import blang.variables.RealVariable;
import static blang.variables.RealVariable.real;

/** Poisson density
 * 
 * The following parameterization is used: 
 * 
 *  X ~ Pois(mean) 
 *  
 *  then the pmf of X is given as: 
 *  
 *  mean^k / k! exp(-mean)
 * 
 * 
 * @author Sean Jewell (jewellsean@gmail.com)
 *
 */

public class Poisson<P extends Poisson.Parameters> implements GenerativeFactor, UnivariateIntegerDistribution
{
  /** 
   * The variable on which this density is defined
   */
  @FactorArgument(makeStochastic=true)
  public final IntegerVariable realization; 
  
  /**
   * Parameter of Poisson density
   */
  @FactorComponent
  public final P parameters; 
  
  public static interface Parameters
  {
    public double getMean();
  }
  
  public static class MeanParameterization implements Parameters
  {
    @FactorArgument
    public final RealVariable mean; 
    
    public MeanParameterization(RealVariable mean)
    {
      this.mean = mean;
    }
    
    @Override
    public double getMean()
    {
      return mean.getValue();
    }
  }
  
  public Poisson(IntegerVariable realization, P parameters)
  {
    this.realization = realization;
    this.parameters = parameters; 
  }

  @Override
  public double logDensity()
  {
    return logDensity(realization.getIntegerValue(), parameters.getMean()); 
  }

  @Override
  public IntegerVariable getRealization()
  {
    return realization;
  }

  @Override
  public void generate(Random random)
  {
    realization.setValue(generate(random, parameters.getMean()));
  }
  
  /* Static versions of the functionalities of this class */
  
  public static double logDensity(int point, double mean)
  {
    if (point < 0 || mean <= 0)
      return Double.NEGATIVE_INFINITY;
    return point * Math.log(mean) - CombinatoricsUtils.factorialLog(point) - mean; 
  }
  
  
  public static int generate(Random random, double mean)
  {
    final PoissonDistribution pd = new PoissonDistribution(new Random2RandomGenerator(random), 
                                                           mean, 
                                                           PoissonDistribution.DEFAULT_EPSILON, 
                                                           PoissonDistribution.DEFAULT_MAX_ITERATIONS);
    return pd.sample(); 
    
  }
  
  
  /* Syntactic sugar/method chaining */
  /**
   * Default mean is set to 1. 
   * @param realization
   * @return
   */
  public static Poisson<MeanParameterization> on(IntegerVariable realization)
  {
    return new Poisson<MeanParameterization>(realization, new MeanParameterization(real(1)));
  }
  
  public Poisson<MeanParameterization> withMean(RealVariable mean)
  {
    return new Poisson<MeanParameterization>(realization, new MeanParameterization(mean));
  }
  
  public Poisson<MeanParameterization> withMean(double mean)
  {
    return new Poisson<MeanParameterization>(realization, new MeanParameterization(real(mean)));
  }
  
  
}
