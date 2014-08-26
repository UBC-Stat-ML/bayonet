package bayonet.distributions;

import static org.apache.commons.math3.util.ArithmeticUtils.factorialLog;
import static org.apache.commons.math3.special.Gamma.logGamma;

import java.util.Random;

import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.IntegerVariable;
import blang.variables.RealVariable;
import static blang.variables.RealVariable.real;

/** Negative binomial densities
 * We use the following parameterization
 * 
 * Gamma(realization + n) prob^n * (1 - prob) ^ realization / ( Gamma(n) * realization !) 
 * 
 * The interpretation is that if n is an integer, this is the probability that of realization failures before n successes 
 * in a sequence of Bernoulli trial with probability of success prob. 
 * 
 * P is the type of parameterization
 * 
 * @author Sean Jewell (jewellsean@gmail.com)
 *
 */
public class NegativeBinomial<P extends NegativeBinomial.Parameters> implements GenerativeFactor, UnivariateIntegerDistribution
{
  /**
   * The variable on which this density is defined on.
   */
  @FactorArgument(makeStochastic=true)
  public final IntegerVariable realization;

  /**
   * The parameter of this exponential density.
   */
  @FactorComponent 
  public final P parameters;


  public static interface Parameters
  {
    public double getProb();
    public double getN();
  }

  public static class ProbSuccessParameterization implements Parameters
  {
    @FactorArgument
    public final RealVariable prob;

    @FactorArgument
    public final RealVariable n;


    public ProbSuccessParameterization(RealVariable prob, RealVariable n)
    {
      this.prob = prob;
      this.n = n;
    }

    @Override
    public double getProb() 
    {
      return prob.getValue();
    }

    @Override
    public double getN()
    {
      return n.getValue();
    }

  }
  
  public static class ProbFailParameterization implements Parameters
  {
    @FactorArgument
    public final RealVariable prob;

    @FactorArgument
    public final RealVariable nFail;


    public ProbFailParameterization(RealVariable prob, RealVariable nFail)
    {
      this.prob = prob;
      this.nFail = nFail;
    }

    @Override
    public double getProb() 
    {
      return 1 - prob.getValue();
    }

    @Override
    public double getN()
    {
      return nFail.getValue();
    }

  }
  
 public NegativeBinomial(IntegerVariable realization, P parameters)
  {
    this.realization = realization; 
    this.parameters = parameters;
  }


 @Override
  public double logDensity() 
  {
    return logDensity(realization.getIntegerValue(), parameters.getN(), parameters.getProb());
  }

  public static double logDensity(int N, double r, double p)
  {
    if (r < 0)
      return Double.NEGATIVE_INFINITY;
    if (p < 0 || p > 1)
      return Double.NEGATIVE_INFINITY;
    if (N < 0)
      return Double.NEGATIVE_INFINITY;
        
    return logGamma(N + r) + r * Math.log(p) + N * Math.log(1 - p) - logGamma(r) - factorialLog(N);
  }


  @Override
  public IntegerVariable getRealization() 
  {
    return realization;
  }

  @Override
  public void generate(Random random) 
  {  
    realization.setValue(inverseF(parameters.getN(), parameters.getProb(), random.nextDouble()));
  }

  
  /**
   * Computes the inverse function without precomputing tables.
   * From umontreal.iro.lecuyer.probdist 
   * TODO: properly cite this code and attribute to Pierre L'Ecuyer.
   */
  public static int inverseF (double n, double p, double u) {
    double EPSILON = 10e-16;
    if (u < 0.0 || u > 1.0)
        throw new IllegalArgumentException ("u is not in [0,1]");
     if (p < 0.0 || p > 1.0)
        throw new IllegalArgumentException ("p not in [0, 1]");
     if (n <= 0.0)
        throw new IllegalArgumentException ("n <= 0");
     if (p >= 1.0)                  // In fact, p == 1
        return 0;
     if (p <= 0.0)                  // In fact, p == 0
        return 0;
     if (u <= prob (n, p, 0))
        return 0;
     if (u >= 1.0)
        return Integer.MAX_VALUE;

     double sum, term, termmode;
     final double q = 1.0 - p;

     // Compute the maximum term
     int mode = 1 + (int) Math.floor ((n * q - 1.0) / p);
     if (mode < 0)
        mode = 0;
     int i = mode;
     term = prob (n, p, i);
     while ((term >= u) && (term > Double.MIN_NORMAL)) {
        i /= 2;
        term = prob (n, p, i);
     }

     if (term <= Double.MIN_NORMAL) {
        i *= 2;
        term = prob (n, p, i);
        while (term >= u && (term > Double.MIN_NORMAL)) {
           term *= i / (q * (n + i - 1.0));
           i--;
        }
     }

     mode = i;
     sum = termmode = prob (n, p, i);

     for (i = mode; i > 0; i--) {
        term *= i / (q * (n + i - 1.0));
        if (term < EPSILON)
           break;
        sum += term;
     }

     term = termmode;
     i = mode;
     double prev = -1;
     if (sum < u) {
        // The CDF at the mode is less than u, so we add term to get >= u.
        while ((sum < u) && (sum > prev)){
           term *= q * (n + i) / (i + 1);
           prev = sum;
           sum += term;
           i++;
        }
     } else {
        // The computed CDF is too big so we subtract from it.
        sum -= term;
        while (sum >= u) {
           term *= i / (q * (n + i - 1.0));
           i--;
           sum -= term;
        }
     }
     return i;
  }
  
  
  private static double prob(double n, double p, int x)
  {
    return Math.exp(logDensity(x, n , p));
  }
  
  
  /* Syntactic sugar/method chaining */
  /**
   * 
   * @param realization
   * @return default values are p = 0.5 and n = 1
   */
  public static NegativeBinomial<ProbSuccessParameterization> on(IntegerVariable realization)
  {
    return new NegativeBinomial<ProbSuccessParameterization>(realization, new ProbSuccessParameterization(real(0.5), real(1.0)));
  }
  
  public NegativeBinomial<ProbSuccessParameterization> withProbN(double prob, double N)
  {
    return new NegativeBinomial<ProbSuccessParameterization>(realization, new ProbSuccessParameterization(real(prob), real(N)));
  }

  public NegativeBinomial<ProbSuccessParameterization> withProbN(RealVariable prob, RealVariable N)
  {
    return new NegativeBinomial<ProbSuccessParameterization>(realization, new ProbSuccessParameterization(prob, N));
  }
  
  
}
