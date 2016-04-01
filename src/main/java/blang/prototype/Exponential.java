package blang.prototype;

import blang.factors.Factor;



public class Exponential implements Factor
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
  }

  @Override
  public double logDensity()
  {
    double x = realization.get();
    double rate = params.getRate();
    return Math.log(rate) - rate * x;
  }

}
