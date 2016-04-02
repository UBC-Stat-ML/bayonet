package blang.prototype;

import java.util.Arrays;
import java.util.Collection;

import blang.core.HasChildrenFactors;
import blang.core.SupportFactor;
import blang.core.SupportFactor.Support;
import blang.factors.Factor;



public class Exponential implements Factor, HasChildrenFactors
{
  public static interface ExponentialParams
  {
    public double getRate();
  }
  
  public final ExponentialParams params;
  public final Real realization;
  
  public Exponential(Real realization, ExponentialParams params){
    this.params = params;
    this.realization = realization;
    this.supportFactor = new SupportFactor(new ExponentialSupport(realization));
  }
  
  public final SupportFactor supportFactor;
  
  private static class ExponentialSupport implements Support
  {
    private final Real realization;
    
    private ExponentialSupport(Real realization)
    {
      this.realization = realization;
    }

    @Override
    public boolean isInSupport()
    {
      return realization.get() >= 0.0;
    }
  }

  @Override
  public double logDensity()
  {
    double x = realization.get();
    double rate = params.getRate();
    return Math.log(rate) - rate * x;
  }

  @Override
  public Collection<Factor> factors()
  {
    return Arrays.asList(supportFactor);
  }
}
