package bayonet.distributions;

import java.util.Random;

import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.RealVariable;

import static blang.variables.RealVariable.real;


/**
 * Continuous uniform priors on an interval [a, b).
 * 
 * P is the type of the parameterization.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Uniform<P extends Uniform.Parameters> implements GenerativeFactor, UnivariateRealDistribution
{
  /**
   * The variable on which this density is defined on.
   */
  @FactorArgument(makeStochastic = true)
  public final RealVariable realization;
  
  /**
   * The parameter of this uniform density (an interval).
   */
  @FactorComponent
  public final P parameters;
  
  /**
   * The parameter is a representation of a single bounded interval on the real line.
   */
  public static interface Parameters
  {
    /**
     * @return Left boundary of the interval
     */
    public double getMin();
    
    /**
     * @return Right boundary of the interval
     */
    public double getMax();
  }
  
  /**
   * 
   * @param variable The variable on which the density is defined on
   * @param param
   */
  public Uniform(RealVariable variable, P param)
  {
    this.realization = variable;
    this.parameters = param;
  }
  
  /**
   * @return The log of the density for the current
   *  assignment of parameters and realization.
   */
  @Override
  public double logDensity()
  {
    double x = realization.getValue();
    if (x < parameters.getMin()) return Double.NEGATIVE_INFINITY;
    if (x >= parameters.getMax()) return Double.NEGATIVE_INFINITY;
    return - Math.log(parameters.getMax() - parameters.getMin());
  }
  
  /**
   * Uses the current assignment of the parameters to sample a new 
   * uniformly distributed random variable. Write this value in 
   * the field variable.
   * 
   * @param random The source of pseudo-randomness.
   */
  @Override
  public void generate(Random random)
  {
    realization.setValue(generate(random, parameters.getMin(), parameters.getMax()));
  }

  /**
   * The identity parameterization.
   */
  public static class MinMaxParameterization implements Parameters
  {
    /**
     * The left boundary of this interval.
     */
    @FactorArgument
    public final RealVariable min;
    
    /**
     * The right boundary of this inverval.
     */
    @FactorArgument
    public final RealVariable max;
    
    /**
     * 
     * @param min
     * @param max
     */
    public MinMaxParameterization(RealVariable min, RealVariable max)
    {
      this.min = min;
      this.max = max;
    }

    /**
     * @return The left boundary.
     */
    @Override
    public double getMin()
    {
      return min.getValue();
    }

    /**
     * @return The right boundary.
     */
    @Override
    public double getMax()
    {
      return max.getValue();
    }
  }

  /**
   * 
   */
  @Override
  public String toString()
  {
    return "Uniform";
  }

  /* Static versions of the functionalities of this class */
  
  /**
   * Simulate uniformly on the interval [min, max).
   * 
   * @param rand The source of pseudo-randomness 
   * @param min The left boundary of the inteval.
   * @param max The right boundary of the inteval.
   */
  public static double generate(Random rand, double min, double max)
  {
    if (min > max)
      throw new RuntimeException();
    return min + (max-min) * rand.nextDouble();
  }
  
  /* Syntactic sugar/method chaining */
  
  /**
   * Allows writing Uniform.on(x) instead of new Uniform(x, ...)
   * Default interval is set to [0, 1).
   * @param variable
   * @return
   */
  public static Uniform<MinMaxParameterization> on(RealVariable variable)
  {
    return new Uniform<MinMaxParameterization>(variable, new MinMaxParameterization(real(0), real(1)));
  }
  
  /**
   * Allows writing Uniform.on(x).withBounds(a, b);
   * @param min
   * @param max
   * @return
   */
  public Uniform<MinMaxParameterization> withBounds(double min, double max) 
  {
    return new Uniform<MinMaxParameterization>(realization, new MinMaxParameterization(real(min), real(max)));
  }
  
  public Uniform<MinMaxParameterization> withBounds(RealVariable min, RealVariable max) 
  {
    return new Uniform<MinMaxParameterization>(realization, new MinMaxParameterization((min), (max)));
  }

  @Override
  public RealVariable getRealization()
  {
    return realization;
  }
}