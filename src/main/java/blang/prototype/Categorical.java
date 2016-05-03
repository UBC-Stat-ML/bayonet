package blang.prototype;

import java.util.Arrays;
import java.util.Collection;

import blang.core.Model;
import blang.core.SupportFactor;
import blang.core.SupportFactor.Support;
import blang.factors.Factor;
import blang.factors.LogScaleFactor;



public class Categorical implements LogScaleFactor, Model
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
    this.supportFactor = new SupportFactor(new CategoricalSupport(realization, params.nStates()));
  }
  
  public final SupportFactor supportFactor;
  
  private static final class CategoricalSupport implements Support
  {
    private final Int realization;
    private final int nStates;
    
    private CategoricalSupport(Int realization, int nStates)
    {
      this.realization = realization;
      this.nStates = nStates;
    }

    @Override
    public boolean isInSupport()
    {
      return realization.get() >= 0 && realization.get() < nStates;
    }
  }

  @Override
  public double logDensity()
  {
    return params.getLogProbability(realization.get());
  }

  @Override
  public Collection<Factor> components()
  {
    return Arrays.asList(supportFactor);
  }
}
