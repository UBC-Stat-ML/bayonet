package bayonet.distributions;

import blang.FactorArgument;
import blang.RealVariable;
import blang.factors.Factor;

import static blang.RealVariable.real;


/**
 * A uniform prior. For example, UniformPrior.onUnitInteval()
 * return a uniform prior on the unit interval.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Uniform implements Factor
{
  @FactorArgument(makeStochastic = true)
  public final RealVariable variable;
  
  @FactorArgument
  public final RealVariable min;
  
  @FactorArgument
  public final RealVariable max;
  
  /**
   * Put a unit uniform distribution on the given variable
   * @param variable
   * @return
   */
  public static Uniform on(RealVariable variable)
  {
    return new Uniform(variable, real(0), real(1));
  }
  
  private Uniform(RealVariable variable, RealVariable min, RealVariable max)
  {
    this.variable = variable;
    this.min = min;
    this.max = max;
  }

  @Override
  public double logDensity()
  {
    double x = variable.getValue();
    if (x < min.getValue()) return Double.NEGATIVE_INFINITY;
    if (x > max.getValue()) return Double.NEGATIVE_INFINITY;
    return 0.0;
  }
  
  @Override
  public String toString()
  {
    return "Uniform";
  }
}