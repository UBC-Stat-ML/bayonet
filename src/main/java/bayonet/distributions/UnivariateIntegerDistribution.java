package bayonet.distributions;

import blang.factors.LogScaleFactor;
import blang.variables.IntegerVariable;

public interface UnivariateIntegerDistribution extends LogScaleFactor
{
  public IntegerVariable getRealization();
}