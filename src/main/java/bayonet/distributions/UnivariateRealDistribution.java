package bayonet.distributions;

import blang.factors.Factor;
import blang.variables.RealVariable;



public interface UnivariateRealDistribution extends Factor
{
  public RealVariable getRealization();
}