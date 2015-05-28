package bayonet.opt;


import java.util.Random;

import org.jblas.DoubleMatrix;

import bayonet.math.NumericalUtils;
import bayonet.math.SamplingUtils;




public class GradientValidator
{
  public static boolean isRandomAnalyticDirectionalDerivCloseToNumerical(
      Random random,
      DifferentiableFunction function)
  {
    return isRandomAnalyticDirectionalDerivCloseToNumerical(random, function, SamplingUtils.randomUnitNormVector(random, function.dimension()));
  }
  
  public static boolean isRandomAnalyticDirectionalDerivCloseToNumerical(
      Random random,
      DifferentiableFunction function, 
      double [] point)
  {
    return isAnalyticDirectionalDerivCloseToNumerical(function, point, SamplingUtils.randomUnitNormVector(random, point.length));
  }
  
  public static boolean isAnalyticDirectionalDerivCloseToNumerical(
      DifferentiableFunction function, 
      double [] point, 
      double [] direction)
  {
    return isAnalyticDirectionalDerivCloseToNumerical(function, point, direction, NumericalUtils.THRESHOLD, INITIAL_DELTA, MAX_N_HALVINGS);
  }
  
  public static int MAX_N_HALVINGS = 100;
  public static double INITIAL_DELTA = 2e-5;
  
  public static boolean isAnalyticDirectionalDerivCloseToNumerical(
      DifferentiableFunction function, 
      double [] point, 
      double [] direction,
      double threshold,
      double initialDelta,
      int maxNHalvings)
  {
    double analytic  = analyticDirectionalDerivative(function, point, direction);
    for (int iter = 0; iter < maxNHalvings; iter++)
    {
      double numerical = numericalDirectionalDerivative(function, point, direction, initialDelta);
      if (NumericalUtils.isClose(
          numerical, 
          analytic, threshold))
        return true;
      initialDelta /= 2.0;
    }
    
    return false;
  }
  
  private static double analyticDirectionalDerivative(
      DifferentiableFunction function, double[] point, double[] direction)
  {
    DoubleMatrix 
      gradient = new DoubleMatrix(function.derivativeAt(point)),
      v = new DoubleMatrix(direction);
    return gradient.dot(v);
  }

  public static double numericalDirectionalDerivative(
      Function function, 
      double [] point, 
      double [] direction,
      double delta)
  {
    DoubleMatrix secondPoint = new DoubleMatrix(direction);
    secondPoint = secondPoint.mul(delta).addi(new DoubleMatrix(point));
    double 
      f0 = function.valueAt(point),
      f1 = function.valueAt(secondPoint.data);
    return numericalDerivative(f0, f1, delta);
  }
  
  private static double numericalDerivative(double f0, double f1, double delta)
  {
    if (delta < 0)
      throw new RuntimeException();
    
    return (f1 - f0) / delta;
  }
}
