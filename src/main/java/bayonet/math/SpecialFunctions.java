package bayonet.math;

import org.apache.commons.math3.special.Gamma;




public class SpecialFunctions
{
  /**
   * See http://en.wikipedia.org/wiki/Multivariate_gamma_function
   * @author Alexandre Bouchard
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
  
  /**
   * log Gamma function: ln(gamma(alpha)) for alpha>0, accurate to 10 decimal places
   *
   * @author Korbinian Strimmer
   * @param alpha argument
   * @return the log of the gamma function of the given alpha
   */
  public static double lnGamma(double alpha) {
      // Pike MC & Hill ID (1966) Algorithm 291: Logarithm of the gamma function.
      // Communications of the Association for Computing Machinery, 9:684

      double x = alpha, f = 0.0, z;

      if (x < 7) {
          f = 1;
          z = x - 1;
          while (++z < 7) {
              f *= z;
          }
          x = z;
          f = -Math.log(f);
      }
      z = 1 / (x * x);

      return
              f + (x - 0.5) * Math.log(x) - x + 0.918938533204673 +
                      (((-0.000595238095238 * z + 0.000793650793651) *
                              z - 0.002777777777778) * z + 0.083333333333333) / x;
  }

  /**
   * Incomplete Gamma function Q(a,x)
   * (a cleanroom implementation of Numerical Recipes gammq(a,x);
   * in Mathematica this function is called GammaRegularized)
   *
   * @author Korbinian Strimmer
   * @param a parameter
   * @param x argument
   * @return function value
   */
  public static double incompleteGammaQ(double a, double x) {
      return 1.0 - incompleteGamma(x, a, lnGamma(a));
  }

  /**
   * Incomplete Gamma function P(a,x) = 1-Q(a,x)
   * (a cleanroom implementation of Numerical Recipes gammp(a,x);
   * in Mathematica this function is 1-GammaRegularized)
   *
   * @author Korbinian Strimmer
   * @param a parameter
   * @param x argument
   * @return function value
   */
  public static double incompleteGammaP(double a, double x) {
      return incompleteGamma(x, a, lnGamma(a));
  }

  /**
   * Incomplete Gamma function P(a,x) = 1-Q(a,x)
   * (a cleanroom implementation of Numerical Recipes gammp(a,x);
   * in Mathematica this function is 1-GammaRegularized)
   *
   * @author Korbinian Strimmer
   * @param a        parameter
   * @param x        argument
   * @param lnGammaA precomputed lnGamma(a)
   * @return function value
   */
  public static double incompleteGammaP(double a, double x, double lnGammaA) {
      return incompleteGamma(x, a, lnGammaA);
  }


  /**
   * Returns the incomplete gamma ratio I(x,alpha) where x is the upper
   * limit of the integration and alpha is the shape parameter.
   *
   * @author Korbinian Strimmer
   * @param x              upper limit of integration
   * @param alpha          shape parameter
   * @param ln_gamma_alpha the log gamma function for alpha
   * @return the incomplete gamma ratio
   */
  private static double incompleteGamma(double x, double alpha, double ln_gamma_alpha) {
      // (1) series expansion     if (alpha>x || x<=1)
      // (2) continued fraction   otherwise
      // RATNEST FORTRAN by
      // Bhattacharjee GP (1970) The incomplete gamma integral.  Applied Statistics,
      // 19: 285-287 (AS32)

      double accurate = 1e-8, overflow = 1e30;
      double factor, gin, rn, a, b, an, dif, term;
      double pn0, pn1, pn2, pn3, pn4, pn5;

      if (x == 0.0) {
          return 0.0;
      }
      if (x < 0.0 || alpha <= 0.0) {
          throw new IllegalArgumentException("Arguments out of bounds");
      }

      factor = Math.exp(alpha * Math.log(x) - x - ln_gamma_alpha);

      if (x > 1 && x >= alpha) {
          // continued fraction
          a = 1 - alpha;
          b = a + x + 1;
          term = 0;
          pn0 = 1;
          pn1 = x;
          pn2 = x + 1;
          pn3 = x * b;
          gin = pn2 / pn3;

          do {
              a++;
              b += 2;
              term++;
              an = a * term;
              pn4 = b * pn2 - an * pn0;
              pn5 = b * pn3 - an * pn1;

              if (pn5 != 0) {
                  rn = pn4 / pn5;
                  dif = Math.abs(gin - rn);
                  if (dif <= accurate) {
                      if (dif <= accurate * rn) {
                          break;
                      }
                  }

                  gin = rn;
              }
              pn0 = pn2;
              pn1 = pn3;
              pn2 = pn4;
              pn3 = pn5;
              if (Math.abs(pn4) >= overflow) {
                  pn0 /= overflow;
                  pn1 /= overflow;
                  pn2 /= overflow;
                  pn3 /= overflow;
              }
          } while (true);
          gin = 1 - factor * gin;
      } else {
          // series expansion
          gin = 1;
          term = 1;
          rn = alpha;
          do {
              rn++;
              term *= x / rn;
              gin += term;
          }
          while (term > accurate);
          gin *= factor / alpha;
      }
      return gin;
  }
  
  /**
   * error function
   *
   * @param x argument
   * @author Korbinian Strimmer
   * @return function value
   */
  public static double erf(double x)
  {
    if (x > 0.0)
    {
      return SpecialFunctions.incompleteGammaP(0.5, x*x);
    }
    else if (x < 0.0)
    {
      return -SpecialFunctions.incompleteGammaP(0.5, x*x);
    }
    else
    {
      return 0.0;
    }
  }
  
  /**
   * complementary error function = 1-erf(x)
   *
   * @param x argument
   * @author Korbinian Strimmer
   * @return function value
   */
  public static double erfc(double x)
  {
    return 1.0-erf(x);
  }

  
  /**
   * inverse error function
   *
   * @param z argument
   * @author Korbinian Strimmer
   * @return function value
   */
  public static double inverseErf(double z)
  {
    return pointNormal(0.5*z+0.5)/Math.sqrt(2.0);
  }


  // Private

  // Returns z so that Prob{x<z}=prob where x ~ N(0,1) and (1e-12) < prob<1-(1e-12)
  private static double pointNormal(double prob)
  {
    // Odeh RE & Evans JO (1974) The percentage points of the normal distribution.
    // Applied Statistics 22: 96-97 (AS70)
  
    // Newer methods:
    // Wichura MJ (1988) Algorithm AS 241: the percentage points of the
    // normal distribution.  37: 477-484.
    // Beasley JD & Springer SG  (1977).  Algorithm AS 111: the percentage 
    // points of the normal distribution.  26: 118-121.
  
    double a0 = -0.322232431088, a1 = -1, a2 = -0.342242088547, a3 = -0.0204231210245;
    double a4 = -0.453642210148e-4, b0 = 0.0993484626060, b1 = 0.588581570495;
    double b2 = 0.531103462366, b3 = 0.103537752850, b4 = 0.0038560700634;
    double y, z = 0, p = prob, p1;
  
    p1 = (p < 0.5 ? p : 1-p);
    if (p1 < 1e-20)
    {
      new IllegalArgumentException("Argument prob out of range");
    }
  
    y = Math.sqrt(Math.log(1/(p1*p1)));   
    z = y + ((((y*a4+a3)*y+a2)*y+a1)*y+a0)/((((y*b4+b3)*y+b2)*y+b1)*y+b0);
    return (p < 0.5 ? -z : z);
  }
}
