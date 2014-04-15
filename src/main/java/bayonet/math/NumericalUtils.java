package bayonet.math;

import bayonet.distributions.Multinomial;


/**
 * Utilities for various numerical computations.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class NumericalUtils
{
  /**
   * A default threshold when checking if quantities match up to 
   * numerical error.
   */
  public static double THRESHOLD = 1e-8;

  /**
   * 
   * @param d1
   * @param d2
   * @param threshold
   * @return whether two numbesr are close +/- thereshold
   */
  public static final boolean isClose(double d1, double d2, double threshold)
  {
    return Math.abs(d1 - d2) < threshold;
  }
  
  /**
   * Checks if two numbers are close +/- thereshold, throw a RuntimeException
   * otherwise
   * @param d1
   * @param d2
   * @param threshold
   */
  public static final void checkIsClose(double d1, double d2, double threshold)
  {
    if (!isClose(d1, d2, threshold))
      throw new RuntimeException("The number " + d1 + " was expected to be close to " + d2 + " (+/- " + threshold + ")");
  }
  
  public static void checkIsClose(double d1, double d2)
  {
    checkIsClose(d1, d2, THRESHOLD);
  }
  
  /**
   * Equivalent, but more numerically resilient to underflows and faster than:
   * 
   * Math.log(Math.exp(x) + Math.exp(y));
   * 
   * @param x A number (in log scale)
   * @param y A number (in log scale)
   * @return logAdded value
   */
  public static double logAdd(double x, double y) {
    // make a the max
    if (y > x) {
      double temp = x;
      x = y;
      y = temp;
    }
    // now a is bigger
    if (x == Double.NEGATIVE_INFINITY) {
      return x;
    }
    double negDiff = y - x;
    if (negDiff < -20) {
      return x;
    }
    return x + java.lang.Math.log(1.0 + java.lang.Math.exp(negDiff));
  }
  
  /**
   * Equivalent, but more numerically resilient to underflows and faster than:
   * 
   * Math.log(\sum_i Math.exp(v_i));
   * 
   * @param v an array of numbers (each in log scale)
   * @return logAdded value
   */
  public static double logAdd(double[] v) {
    double max = Double.NEGATIVE_INFINITY;
    double maxIndex = 0;
    for (int i = 0; i < v.length; i++) {
      if (v[i] > max) { 
        max = v[i];
        maxIndex = i;
      }
    }
    if (max == Double.NEGATIVE_INFINITY) return Double.NEGATIVE_INFINITY;
    // compute the negative difference
    double threshold = max - 20;
    double sumNegativeDifferences = 0.0;
    for (int i = 0; i < v.length; i++) {
      if (i != maxIndex && v[i] > threshold) {
        sumNegativeDifferences += Math.exp(v[i] - max);
      }
    }
    if (sumNegativeDifferences > 0.0) {
      return max + Math.log(1.0 + sumNegativeDifferences);
    } else {
      return max;
    }
  }
  
  /**
   * Call checkIsTransitionMatrix(matrix,threshold) with the default threshold
   * Also checks non-negativity.
   * @param matrix
   */
  public static void checkIsTransitionMatrix(double[][] matrix)
  {
    checkIsTransitionMatrix(matrix, THRESHOLD);
  }

  /**
   * Check that the sum of the entries in each row is numerically close to 1.0.
   * Also checks non-negativity.
   * @param matrix
   * @param threshold
   */
  public static void checkIsTransitionMatrix(double[][] matrix, double threshold)
  {
    final int size = matrix.length;
    for (int row = 0; row < size; row++)
    {
      if (matrix[row].length != size)
        throw new RuntimeException();
      double norm = Multinomial.getNormalization(matrix[row]);
      checkIsClose(norm, 1.0, threshold);
    }
  }
  
  /**
   * Check that the sum of the entries is numerically close to 1.0.
   * Also checks non-negativity.
   * @param numbers
   * @param threshold
   */
  public static void checkIsProb(final double [] numbers, double threshold)
  {
    checkIsClose(1.0, Multinomial.getNormalization(numbers), threshold);
  }
  
  /**
   * Call checkIsProb() with the default threshold
   * Also checks non-negativity.
   * @param matrix
   */
  public static void checkIsProb(final double [] numbers)
  {
    checkIsClose(1.0, Multinomial.getNormalization(numbers), THRESHOLD);
  }

  /**
   * Get the normalization of the array.
   * @param data
   * @return
   */
  public static double getNormalization(double[] data)
  {
    double sum = 0;
    for(double x : data) 
      sum += x;
    return sum;
  }
  
}
