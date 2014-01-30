package bayonet.math;

import org.apache.commons.math3.special.Gamma;



public class SpecialFunctions
{
  /**
   * See http://en.wikipedia.org/wiki/Multivariate_gamma_function
   * @param dim p in the above source
   * @param a a in the above source
   * @return The value of the multivariate gamma IN LOG SCALE
   */
  public static double multivariateLogGamma(int dim, double a)
  {
    if (dim == 1)
      return Gamma.logGamma(a);
    else if (dim == 2)
      return 0.5 * Math.log(Math.PI) + Gamma.logGamma(a) + Gamma.logGamma(a - 0.5);
    else
    {
      double result = ((double) dim)/2.0 * Math.log(Math.PI);
      for (int i = 0; i < dim; i++)
        result += Gamma.logGamma(a - ((double)i)/2.0);
      return result;
    }
  }
}
