package blang.prototype3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import blang.core.Model;
import blang.core.ModelComponent;
import blang.core.SupportFactor;
import blang.core.SupportFactor.Support;
import blang.factors.LogScaleFactor;
import static java.lang.Math.*;


/**
 * b3 code:
 * ```
 * // optional package
 * // imports
 * model {
 * 
 *   // parameters declaration
 *   param Real mean
 *   param Real variance
 * 
 *   // random variable declaration
 *   random Real x
 *  
 *   laws {
 *     // bugs block (see below)
 *   }
 * }
 * ```
 */
public class Normal implements Model
{
  /**
   * ```
   * param Real mean
   * ```
   * Note: a parameter of type T gets translated into Supplier<T> field.
   *       This is used to emulated lazy evaluation behavior on the constructor
   *       call.
   */
  private final Supplier<Real> mean;
  
  /**
   * ```
   * param Real variance
   * ```
   */
  private final Supplier<Real> variance;
  
  /**
   * ```
   * random Real x
   * ```
   */
  private final Real x;
  
  // See comment about constructors in SimpleNormalModel
  public Normal(Real y, Supplier<Real> mean, Supplier<Real> variance)
  {
    this.x = y;
    this.mean = mean;
    this.variance = variance;
  }
  
  final static double LOG2PI = log(2 * PI);

  /**
   * ```
   * final static double LOG2PI = log(2 * PI)
   * 
   * laws {
   * 
   *   logf(variance) = -0.5 * ( log(variance.doubleValue) + LOG2PI )
   *   
   *   logf(x, variance, mean) = {
   *     val centered = x.doubleValue - mean.doubleValue
   *     return -0.5 * centered * centered / variance.doubleValue
   *   }
   *   
   *   indicator(variance) = variance.doubleValue > 0
   * 
   * }
   * ```
   */
  @Override
  public Collection<? extends ModelComponent> components()
  {
    ArrayList<ModelComponent> components = new ArrayList<>();
    
    components.add($generated_setupLogScaleFactor0(variance));
    components.add($generated_setupLogScaleFactor1(x, variance, mean));
    components.add($generated_setupSupportFactor0(variance));
    
    return components;
  }
  
  private static LogScaleFactor $generated_setupLogScaleFactor0(final Supplier<Real> $generated_variance)
  {
    return new LogScaleFactor() 
    {
      @Override
      public double logDensity()
      {
        Real variance = $generated_variance.get();  // Note: This line is generated for each "param" passed in.
                                                    //       This is not needed for "random" arguments (see next example)
        
        return -0.5 * ( log(variance.doubleValue()) + LOG2PI );
      }
    };
  }
  
  private static ModelComponent $generated_setupLogScaleFactor1(
      final Real x,
      final Supplier<Real> $generated_variance, final Supplier<Real> $generated_mean)
  {
    return new LogScaleFactor()
    {
      @Override
      public double logDensity()
      {
        Real variance = $generated_variance.get();
        Real mean = $generated_mean.get();
        
        final double centered = x.doubleValue() - mean.doubleValue();
        return -0.5 * centered * centered / variance.doubleValue();
      }
    };
  }
  
  private static SupportFactor $generated_setupSupportFactor0(final Supplier<Real> $generated_variance)
  {
    return new SupportFactor(new Support() 
    {
      @Override
      public boolean isInSupport()
      {
        Real variance = $generated_variance.get();
        
        return variance.doubleValue() > 0;
      }
    });
  }
}
