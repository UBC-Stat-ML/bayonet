package blang.core;

import blang.factors.Factor;


/**
 * 
 * It is useful to differentiate support factor from standard factor for at least two reasons:
 *  - construct annealed versions of the log density, and, 
 *  - in some cases, speed up likelihood evaluation, by evaluating support constraints first 
 *    as negative infinity is an absorbing state.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public final class SupportFactor implements Factor
{
  private final Support support;
  
  public SupportFactor(Support support)
  {
    this.support = support;
  }
  
  public boolean isInSupport()
  {
    return support.isInSupport();
  }

  @Override
  public double logDensity()
  {
    return isInSupport() ? 0.0 : Double.NEGATIVE_INFINITY;
  }
  
  @FunctionalInterface
  public static interface Support
  {
    public boolean isInSupport();
  }
}
