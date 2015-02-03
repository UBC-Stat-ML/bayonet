package bayonet.smc;


import bayonet.math.NumericalUtils;


/**
 * Keeps the sufficient statistics of a potentially large collection of 
 * weighted particles.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class CompactPopulation
{
  private int nParticles = 0;
  
  /**
   * Sum of the unnormalized weights (log scale)
   */
  private double logSum = Double.NEGATIVE_INFINITY;
  
  /**
   * Sum of the squares-of-unnormalized-weights (log scale)
   */
  private double logSumOfSquares = Double.NEGATIVE_INFINITY;
  
  public int getNumberOfParticles() 
  { 
    return nParticles; 
  }
  
  public void insertLogWeight(double logWeight)
  {
    nParticles++;
    logSum          = NumericalUtils.logAdd(logSum,              logWeight);
    logSumOfSquares = NumericalUtils.logAdd(logSumOfSquares, 2 * logWeight);
  }
  
  /**
   * @return The effective sampling size (ESS).
   */
  public double ess()
  {
    return Math.exp(2 * logSum - logSumOfSquares);
  }
  
  /**
   * @return Standard importance sampling estimator of normalization (log scale)
   */
  public double logZEstimate()
  {
    return logSum - Math.log(nParticles);
  }

  public double getLogSum()
  {
    return logSum;
  }

  public double getLogSumOfSquares()
  {
    return logSumOfSquares;
  }
}