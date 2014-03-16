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
   * Math.log(Math.exp(logX) + Math.exp(logY));
   * 
   * @param logX
   * @param logY
   * @return logAdded value
   */
  public static double logAdd(double logX, double logY) {
    // make a the max
    if (logY > logX) {
      double temp = logX;
      logX = logY;
      logY = temp;
    }
    // now a is bigger
    if (logX == Double.NEGATIVE_INFINITY) {
      return logX;
    }
    double negDiff = logY - logX;
    if (negDiff < -20) {
      return logX;
    }
    return logX + java.lang.Math.log(1.0 + java.lang.Math.exp(negDiff));
  }
  
  /**
   * Call checkIsTransitionMatrix(matrix,threshold) with the default threshold
   * @param matrix
   */
  public static void checkIsTransitionMatrix(double[][] matrix)
  {
    checkIsTransitionMatrix(matrix, THRESHOLD);
  }

  /**
   * Check that the sum of the entries in each row is numerically close to 1.0.
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


  
}
