package bayonet.distributions;

import static blang.variables.RealVariable.real;

import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.RealVariable;


/**
 * Exponential densities.
 * 
 * P is the type of parameterization.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 */
public class Exponential<P extends Exponential.Parameters> implements GenerativeFactor, UnivariateRealDistribution
{
  /**
   * The variable on which this density is defined on.
   */
  @FactorArgument(makeStochastic=true)
  public final RealVariable realization;
  
  /**
   * The parameter of this exponential density.
   */
  @FactorComponent 
  public final P parameters;
  
  /**
   * 
   */
  public static interface Parameters
  {
    /**
     * @return The parameter, transformed back into a rate.
     */
    public double getRate();
  }

  /**
   * 
   * @param realization The variable on which the density is defined on
   * @param parameters
   */
  public Exponential(RealVariable realization, P parameters)
  {
    this.realization = realization;
    this.parameters = parameters;
  }

  /**
   * @return The log of the density for the current
   *  assignment of parameters and realization.
   */
  @Override
  public double logDensity()
  {
    return logDensity(realization.getValue(), parameters.getRate());
  }

  /**
   * Uses the current assignment of the parameters to sample a new 
   * exponentially distributed random variable. Write this value in 
   * the field realization.
   * 
   * @param random The source of pseudo-randomness.
   */
  @Override
  public void generate(Random random)
  {
    realization.setValue(generate(random, parameters.getRate()));
  }
  
  /**
   * The identity parameterization.
   */
  public static class RateParameterization implements Parameters
  {
    /**
     * 
     */
    @FactorArgument 
    public final RealVariable rate;
    
    /**
     * 
     * @param rate
     */
    public RateParameterization(RealVariable rate)
    {
      this.rate = rate;
    }

    /**
     * @return The rate.
     */
    @Override
    public double getRate()
    {
      return rate.getValue();
    }
  }
  
  /**
   * Parameterization in terms of the mean (1/rate) of the exponential.
   */
  public static class MeanParameterization implements Parameters
  {
    /**
     * 
     */
    @FactorArgument 
    public final RealVariable mean;
    
    /**
     * 
     * @param mean
     */
    public MeanParameterization(RealVariable mean)
    {
      this.mean = mean;
    }

    /**
     * @return The mean transformed back into a rate.
     */
    @Override
    public double getRate()
    {
      return 1.0/mean.getValue();
    }
  }
  
  /* Static versions of the functionalities of this class */
  
  /**
   * @param point The point at which the log density is evaluated at.
   * @param rate The rate parameter.
   * @return The log of the density, normalized.
   */
  public static double logDensity(double point, double rate)
  {
    if (point < 0.0 || rate < 0.0)
      return Double.NEGATIVE_INFINITY;
    return Math.log(rate) - rate * point;
  }
  
  /**
   * 
   * @param random The source of pseudo-randomness
   * @param rate 
   * @return The sample.
   */
  public static double generate(Random random, double rate)
  {
    
    final double mean = 1.0 / rate;
    final ExponentialDistribution ed = new ExponentialDistribution(new Random2RandomGenerator(random), mean, ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    return ed.sample();
  }
  
  /* Syntactic sugar/method chaining */
  
  /**
   * Allows writing Exponential.on(x) instead of new Exponential<..>(x, ...)
   * Default rate/mean is set to 1.0.
   * @param realization 
   */
  public static Exponential<RateParameterization> on(RealVariable realization)
  {
    return new Exponential<RateParameterization>(realization, new RateParameterization(real(1.0)));
  }
  
  /**
   * Alows writing Exponential.on(x).withRate(r)
   * @param rate
   * @return
   */
  public Exponential<RateParameterization> withRate(double rate)
  {
    return new Exponential<RateParameterization>(realization, new RateParameterization(real(rate)));
  }
  
  public Exponential<MeanParameterization> withMean(double mean)
  {
    return new Exponential<MeanParameterization>(realization, new MeanParameterization(real(mean)));
  }
  
  public Exponential<RateParameterization> withRate(RealVariable rate)
  {
    return new Exponential<RateParameterization>(realization, new RateParameterization(rate));
  }
  
  public Exponential<MeanParameterization> withMean(RealVariable mean)
  {
    return new Exponential<MeanParameterization>(realization, new MeanParameterization(mean));
  }

  @Override
  public RealVariable getRealization()
  {
    return realization;
  }
}