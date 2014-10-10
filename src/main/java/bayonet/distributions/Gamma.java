package bayonet.distributions;


import java.util.Random;
import org.apache.commons.math3.distribution.GammaDistribution;

import bayonet.math.SpecialFunctions;
import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.RealVariable;
import static blang.variables.RealVariable.real;



public class Gamma<P extends Gamma.Parameters> implements GenerativeFactor, UnivariateRealDistribution
{
  /**
   * The variable on which this density is defined on.
   */
  @FactorArgument(makeStochastic=true)
  public final RealVariable realization;
  
  /**
   * The parameter of this exponential density.
   */
  @FactorComponent 
  public final P parameters;
  
  public static interface Parameters
  {
    public double getRate();
    
    public double getShape();
  }
  
  public Gamma(RealVariable realization, P parameters)
  {
    this.realization = realization;
    this.parameters = parameters;
  }
  
  /**
   * @return The log of the density for the current
   *         assignment of parameters and realization.
   */
  @Override
  public double logDensity()
  {
    return logDensity(realization.getValue(), parameters.getRate(), parameters.getShape());
  }
  
  @Override
  public void generate(Random random)
  {
    realization.setValue(generate(random, parameters.getRate(), parameters.getShape()));
  }
  
  public static class RateShapeParameterization implements Parameters
  {
    /**
     * 
     */
    @FactorArgument 
    public final RealVariable rate;
    
    /**
     * 
     */
    @FactorArgument 
    public final RealVariable shape;
    
    public RateShapeParameterization(RealVariable rate, RealVariable shape)
    {
      this.rate = rate;
      this.shape = shape;
    }

    @Override
    public double getRate()
    {
      return rate.getValue();
    }

    @Override
    public double getShape()
    {
      return shape.getValue();
    }
  }
  
  
  public static class ScaleShapeParameterization implements Parameters
  {
    /**
     * 
     */
    @FactorArgument 
    public final RealVariable scale;

    /**
     * 
     */
    @FactorArgument 
    public final RealVariable shape;

    public ScaleShapeParameterization(RealVariable scale, RealVariable shape)
    {
      this.scale = scale;
      this.shape = shape;
    }

    @Override
    public double getRate()
    {
      return 1.0 / scale.getValue();
    }

    @Override
    public double getShape()
    {
      return shape.getValue();
    }
  }

  /**
   * 
   * @param random The source of pseudo-randomness
   * @param rate 
   * @return The sample.
   */
  public static double generate(Random random, double rate, double shape)
  {
    final GammaDistribution gd = new GammaDistribution(new Random2RandomGenerator(random), shape, 1.0/rate, GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    return gd.sample();
  }
  
  /**
   * the natural log of the probability density function of the distribution
   *
   * @author Korbinian Strimmer
   * @author Gerton Lunter
   *
   * @param x     argument
   * @param rate rate parameter
   * @param shape shape parameter
   * @return log pdf value
   */
  public static double logDensity(double x, double rate, double shape) {
    
    if (x < 0.0 || rate < 0.0 || shape < 0.0)
      return Double.NEGATIVE_INFINITY;
    
    final double scale = 1.0/rate;
    // double a = Math.pow(scale,-shape) * Math.pow(x, shape-1.0);
    // double b = x/scale + GammaFunction.lnGamma(shape);
    // return Math.log(a) - b;

    // AR - changed this to return -ve inf instead of throwing an
    // exception... This makes things
    // much easier when using this to calculate log likelihoods.
    // if (x < 0) throw new IllegalArgumentException();
    if (x < 0)
        return Double.NEGATIVE_INFINITY;

    if (x == 0) {
        if (shape == 1.0)
            return Math.log(1.0 / scale);
        else
            return Double.NEGATIVE_INFINITY;
    }
    if (shape == 1.0) {
        return (-x / scale) - Math.log(scale);
    }
    if (shape == 0.0)  // uninformative
        return -Math.log(x);

    return ((shape - 1.0) * Math.log(x / scale) - x / scale - SpecialFunctions
            .lnGamma(shape))
            - Math.log(scale);
  }

  /**
   * quantile (inverse cumulative density function) of the Gamma distribution
   *
   * @author Korbinian Strimmer
   * @param y     argument
   * @param shape shape parameter
   * @param scale scale parameter
   * @return icdf value
   */
  public static double quantile(double y, double shape, double scale) {
      return 0.5 * scale * pointChi2(y, 2.0 * shape);
  }
  
  private static double pointChi2(double prob, double v) {
    // Returns z so that Prob{x<z}=prob where x is Chi2 distributed with df
    // = v
    // RATNEST FORTRAN by
    // Best DJ & Roberts DE (1975) The percentage points of the
    // Chi2 distribution. Applied Statistics 24: 385-388. (AS91)

    final double e = 0.5e-6, aa = 0.6931471805, p = prob;
    double ch, a, q, p1, p2, t, x, b, s1, s2, s3, s4, s5, s6;
    double epsi = .01;
    if( p < 0.000002 || p > 1 - 0.000002)  {
        epsi = .000001;
    }
    // if (p < 0.000002 || p > 0.999998 || v <= 0) {
    //      throw new IllegalArgumentException("Arguments out of range p" + p + " v " + v);
    //  }
    double g = SpecialFunctions.lnGamma(v / 2);
    double xx = v / 2;
    double c = xx - 1;
    if (v < -1.24 * Math.log(p)) {
        ch = Math.pow((p * xx * Math.exp(g + xx * aa)), 1 / xx);
        if (ch - e < 0) {
            return ch;
        }
    } else {
        if (v > 0.32) {
            x = Normal.quantile(p, 0, 1);
            p1 = 0.222222 / v;
            ch = v * Math.pow((x * Math.sqrt(p1) + 1 - p1), 3.0);
            if (ch > 2.2 * v + 6) {
                ch = -2 * (Math.log(1 - p) - c * Math.log(.5 * ch) + g);
            }
        } else {
            ch = 0.4;
            a = Math.log(1 - p);


            do {
                q = ch;
                p1 = 1 + ch * (4.67 + ch);
                p2 = ch * (6.73 + ch * (6.66 + ch));
                t = -0.5 + (4.67 + 2 * ch) / p1
                        - (6.73 + ch * (13.32 + 3 * ch)) / p2;
                ch -= (1 - Math.exp(a + g + .5 * ch + c * aa) * p2 / p1)
                        / t;
            } while (Math.abs(q / ch - 1) - epsi > 0);
        }
    }
    do {
        q = ch;
        p1 = 0.5 * ch;
        if ((t = SpecialFunctions.incompleteGammaP(xx, p1, g)) < 0) {
            throw new IllegalArgumentException(
                    "Arguments out of range: t < 0");
        }
        p2 = p - t;
        t = p2 * Math.exp(xx * aa + g + p1 - c * Math.log(ch));
        b = t / ch;
        a = 0.5 * t - b * c;

        s1 = (210 + a * (140 + a * (105 + a * (84 + a * (70 + 60 * a))))) / 420;
        s2 = (420 + a * (735 + a * (966 + a * (1141 + 1278 * a)))) / 2520;
        s3 = (210 + a * (462 + a * (707 + 932 * a))) / 2520;
        s4 = (252 + a * (672 + 1182 * a) + c * (294 + a * (889 + 1740 * a))) / 5040;
        s5 = (84 + 264 * a + c * (175 + 606 * a)) / 2520;
        s6 = (120 + c * (346 + 127 * c)) / 5040;
        ch += t
                * (1 + 0.5 * t * s1 - b
                * c
                * (s1 - b
                * (s2 - b
                * (s3 - b
                * (s4 - b * (s5 - b * s6))))));
    } while (Math.abs(q / ch - 1) > e);

    return (ch);
}

  /* Syntactic sugar/method chaining */
  public static Gamma<RateShapeParameterization> on(RealVariable realization)
  {
    return new Gamma<RateShapeParameterization>(realization, new RateShapeParameterization(RealVariable.real(1.0), RealVariable.real(1.0)));
  }
  
  public static Gamma<RateShapeParameterization> newGamma()
  {
    return Gamma.on(new RealVariable(1.0));
  }  
  
  public Gamma<RateShapeParameterization> withRateShape(double rate, double shape)
  {
    return new Gamma<RateShapeParameterization>(realization, new RateShapeParameterization(real(rate), real(shape)));
  }
  
  public Gamma<RateShapeParameterization> withRateShape(RealVariable rate, RealVariable shape)
  {
    return new Gamma<RateShapeParameterization>(realization, new RateShapeParameterization(rate, shape));
  }
  
  public Gamma<ScaleShapeParameterization> withScaleShape(double scale, double shape)
  {
    return new Gamma<ScaleShapeParameterization>(realization, new ScaleShapeParameterization(real(scale), real(shape)));
  }
  
  public Gamma<ScaleShapeParameterization> withScaleShape(RealVariable scale, RealVariable shape)
  {
    return new Gamma<ScaleShapeParameterization>(realization, new ScaleShapeParameterization(scale, shape));
  }
  
  
  @Override
  public RealVariable getRealization()
  {
    return realization;
  }
}
