package bayonet.math;


/**
 * Utilities for various numerical computations.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class NumericalUtils
{
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
      throw new RuntimeException("The numer " + d1 + " was expected to be close to " + d2 + " (+/- " + threshold + ")");
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
  
}
