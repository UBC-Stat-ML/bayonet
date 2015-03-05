package bayonet.distributions;

import java.util.Random;


import bayonet.math.SpecialFunctions;
import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.RealVariable;


/**
 * Normal densities.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Normal<P extends Normal.Parameters> implements GenerativeFactor, UnivariateRealDistribution
{
  
  @FactorArgument(makeStochastic=true)
  public final RealVariable realization;
  
  @FactorComponent
  public final P parameters;
  
  public static interface Parameters
  {
    public double getMean();
    public double getVariance();
  }
  
  public Normal(RealVariable realization, P parameters)
  {
    this.realization = realization;
    this.parameters = parameters;
  }
  
  
  public final static double LOG_INV_SQRT_2_PI = -Math.log(Math.sqrt(2*Math.PI));
  
  /**
   * The product of  normal densities for n iid observations x_i
   * @param mean
   * @param var
   * @param sum \sum_i x 
   * @param sumSq \sum_i x^2
   * @param n
   * @return
   */
  public static double logProb(double mean, double var, double sum, double sumSq, double n) 
  {
    return -0.5*(sumSq - 2*mean*sum + mean*mean)/var + n*(LOG_INV_SQRT_2_PI - 0.5*Math.log(var));
  }
  
  public static double logDensity(double x, double mean, double var)
  {
    return logProb(mean, var, x, x*x, 1);
  }
  
  /**
   * Box-Muller Transformation
   * 
   * @param random
   * @return N(0,1) sample
   */
  public static double generateStandardNormal(Random random) 
  {
    double x1 = random.nextDouble(), x2 = random.nextDouble();
    double z = Math.sqrt(-2*Math.log(x1))*Math.cos(2*Math.PI*x2);
    return z;
  }
  
  public static double generate(Random random, double mean, double var) 
  {
    return generateStandardNormal(random) * Math.sqrt(var) + mean;
  }
  
  /**
   * quantiles (=inverse cumulative density function)
   *
   * @param z  argument
   * @param m  mean
   * @param sd standard deviation
   * @return icdf at z
   */
  public static double quantile(double z, double m, double sd) 
  {
      return m + Math.sqrt(2.0) * sd * SpecialFunctions.inverseErf(2.0 * z - 1.0);
  }

  @Override
  public double logDensity()
  {
    return logDensity(realization.getValue(), parameters.getMean(), parameters.getVariance());
  }

  @Override
  public RealVariable getRealization()
  {
    return realization;
  }

  @Override
  public void generate(Random random)
  {
    realization.setValue(generate(random, parameters.getMean(), parameters.getVariance()));
  }
  
  public static class MeanVarianceParameterization implements Parameters
  {
    @FactorArgument
    public final RealVariable mean;
    
    @FactorArgument
    public final RealVariable variance;
    
    public MeanVarianceParameterization(RealVariable mean, RealVariable var)
    {
      this.mean = mean;
      this.variance = var;
    }

    @Override
    public double getMean()
    {
      return mean.getValue();
    }

    @Override
    public double getVariance()
    {
      return variance.getValue();
    }
  }
  
  public static class MeanPrecisionParameterization implements Parameters
  {
    @FactorArgument
    public final RealVariable mean; 
    
    @FactorArgument
    public final RealVariable precision;
      
    public MeanPrecisionParameterization(RealVariable mean, RealVariable precision)
    {
        this.mean = mean; 
        this.precision = precision;
    }
    
    @Override
    public double getMean()
    {
        return mean.getValue();
    }

    @Override
    public double getVariance()
    {
        return (1 / precision.getValue());
    }
      
  }
  
  /* Syntactic sugar/method chaining */
  
  public static Normal<MeanVarianceParameterization> on(RealVariable realization)
  {
    return new Normal<MeanVarianceParameterization>(realization, new MeanVarianceParameterization(RealVariable.real(0.0), RealVariable.real(1.0)));
  }
  
  public static Normal<MeanVarianceParameterization> newNormal()
  {
    return Normal.on(RealVariable.real(0.0));
  }
    
  public Normal<MeanPrecisionParameterization> withMeanPrecision(RealVariable mean, RealVariable precision)
  {
      return new Normal<MeanPrecisionParameterization>(realization, new MeanPrecisionParameterization(mean, precision)); 
  }
    
 
}
