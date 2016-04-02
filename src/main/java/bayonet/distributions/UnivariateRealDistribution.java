package bayonet.distributions;

import blang.factors.LogScaleFactor;
import blang.variables.RealVariable;



public interface UnivariateRealDistribution extends LogScaleFactor
{
  public RealVariable getRealization();
}