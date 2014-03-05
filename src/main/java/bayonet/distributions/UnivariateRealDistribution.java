package bayonet.distributions;

import blang.factors.Factor;
import blang.factors.GenerativeFactor;
import blang.variables.RealVariable;



public interface UnivariateRealDistribution extends Factor
{
  public RealVariable getRealization();
}