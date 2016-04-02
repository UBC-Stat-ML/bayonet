package blang.factors;



public interface LogScaleFactor extends Factor
{
  /**
   * Note: the density should be normalized, and in log scale.
   * 
   * @return The log of the density for the current
   *  assignment of parameters and realization.
   *  
   */
  public double logDensity();
}
