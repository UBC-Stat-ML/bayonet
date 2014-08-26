package bayonet.distributions;

import blang.factors.Factor;
import blang.variables.IntegerVariable;

public interface UnivariateIntegerDistribution extends Factor
{
  public IntegerVariable getRealization();
}