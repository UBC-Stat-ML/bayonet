package bayonet.distributions;

import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import blang.RealVariable;
import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.factors.StandardFactor;

import static blang.RealVariable.real;


/**
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Exponential<P extends Exponential.Parameters> implements StandardFactor, GenerativeFactor
{
  public static double logDensity(double point, double rate)
  {
    if (point < 0.0)
      return Double.NEGATIVE_INFINITY;
    return -rate * point;
  }
  
  public static double generate(Random random, double rate)
  {
    final double mean = 1.0 / rate;
    ExponentialDistribution ed = new ExponentialDistribution(new Random2RandomGenerator(random), mean, ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    return ed.sample();
  }
  
  @FactorArgument(makeStochastic=true)
  public final RealVariable realization;
  
  @FactorComponent 
  public final P parameters;
  
  public static interface Parameters
  {
    public double getRate();
  }
  
  private Exponential(RealVariable realization, P parameters)
  {
    this.realization = realization;
    this.parameters = parameters;
  }
  
  public static Exponential<RateParameterization> on(RealVariable realization)
  {
    return new Exponential<RateParameterization>(realization, new RateParameterization(real(1.0)));
  }
  
  public Exponential<RateParameterization> withRate(double rate)
  {
    return new Exponential<RateParameterization>(realization, new RateParameterization(real(rate)));
  }
  
  public Exponential<MeanParameterization> withMean(double mean)
  {
    return new Exponential<MeanParameterization>(realization, new MeanParameterization(real(mean)));
  }

  @Override
  public double logDensity()
  {
    return logDensity(realization.getValue(), parameters.getRate());
  }

  @Override
  public void generate(Random random)
  {
    realization.setValue(generate(random, parameters.getRate()));
  }
  

  
  private static class RateParameterization implements Parameters
  {
    @FactorArgument public final RealVariable rate;
    
    private RateParameterization(RealVariable rate)
    {
      this.rate = rate;
    }

    @Override
    public double getRate()
    {
      return rate.getValue();
    }
  }
  
  private static class MeanParameterization implements Parameters
  {
    @FactorArgument public final RealVariable mean;
    
    private MeanParameterization(RealVariable mean)
    {
      this.mean = mean;
    }

    @Override
    public double getRate()
    {
      return 1.0/mean.getValue();
    }
  }
}