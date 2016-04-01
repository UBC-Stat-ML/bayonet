package blang.prototype;

import blang.core.SupportFactor;
import blang.factors.Factor;



public class Categorical implements Factor
{
  public static interface CategoricalParams
  {
    public double getLogProbability(int state);
    public int nStates();
  }
  
  public final CategoricalParams params;
  public final Int realization;
  
  public Categorical(Int realization, CategoricalParams params)
  {
    this.params = params;
    this.realization = realization;
    this.supportFactor = new SupportFactor(() -> realization.get() >= 0 && realization.get() < params.nStates());
  }
  
  public final SupportFactor supportFactor;

  @Override
  public double logDensity()
  {
    return params.getLogProbability(realization.get());
  }
}
