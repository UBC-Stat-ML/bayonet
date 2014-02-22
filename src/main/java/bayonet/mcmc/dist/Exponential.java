package bayonet.mcmc.dist;

import bayonet.mcmc.Factor;
import bayonet.mcmc.FactorArgument;
import bayonet.mcmc.RealVariable;

import static bayonet.mcmc.RealVariable.real;


/**
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Exponential implements Factor
{
  private Exponential(RealVariable realization, RealVariable rate, RealVariable min)
  {
    this.realization = realization;
    this.min = min;
    this.rate = rate;
  }
  
  public static Exponential on(RealVariable realization)
  {
    return new Exponential(realization, real(1), real(0));
  }
  
  public Exponential withRate(double rate)
  {
    return new Exponential(realization, real(rate), min);
  }
  
  public Exponential withMinAt(double value)
  {
    return new Exponential(realization, rate, real(value));
  }

  @FactorArgument(makeStochastic=true)
  public final RealVariable realization;
  
  /**
   * The exponential can be translated; corresponding to
   * min being different than 0. For example, min=1 means
   * that X-1 is exponential.
   */
  @FactorArgument
  public final RealVariable min;
  
  /**
   * Rate parameter (NOT mean)
   */
  @FactorArgument
  public final RealVariable rate;

  @Override
  public double logDensity()
  {
    double x = realization.getValue() - min.getValue();
    if (x < 0.0)
      return Double.NEGATIVE_INFINITY;
    return -rate.getValue() * x;
  }

  @Override
  public String toString()
  {
    return "Exponential";
  }
}