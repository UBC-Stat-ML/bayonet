package bayonet.math;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.collect.Lists;

/**
 * Computes the ESS based on sqrt(n) size batch estimators. See James Flegal, 2008. 
 * Generally more stable than the 
 * ACT-based methods (e.g. in AutoCorrTime.java) or those based on fitting an AR 
 * model (e.g. in EffectiveSize.java)
 */
public class EffectiveSampleSize
{
  
  public static double ess(List<Double> samples)
  {
    SummaryStatistics globalStats = summaryStatistics(samples.stream());
    double mean = globalStats.getMean();
    double sd = globalStats.getStandardDeviation();
    return ess(samples, mean, sd);
  }
  
  /**
   * Compute an ESS for a random variable with a known mean and sd.
   * E.g. this could be on toy data, or when a much longer run is available. 
   */
  public static double ess(List<Double> samples, double mean, double sd)
  {
    int nBlocks = 1 + (int) Math.sqrt(samples.size());
    int partitionSize = 1 + samples.size() / nBlocks;
    List<List<Double>> split = Lists.partition(samples, partitionSize);
    
    SummaryStatistics blockStats = new SummaryStatistics();
    for (List<Double> block : split)
      blockStats.addValue(Math.pow(average(block.stream().map(x -> (x - mean)/sd)), 2.0)); 
    
    return ess(split.size(), 1.0, blockStats.getMean());
  }
  
  public static double ess(List<Double> samples, Function<Double, Double> transformation)
  {
    return ess(samples.stream().map(transformation).collect(Collectors.toList()));
  }
  
  /**
   * Correct but numerically inferior to the above.
   */
  @Deprecated
  public static double ess(double squareIntegral, SummaryStatistics blockStats) 
  {
    double mean = blockStats.getMean();
    double sampleVariance = squareIntegral - mean * mean;
    return ess((int) blockStats.getN(), sampleVariance, blockStats.getVariance());
  }
  
  private static double ess(int numberOfBlocks, double sampleVariance, double blockVariance)
  {
    return numberOfBlocks * sampleVariance / blockVariance;
  }

  private static double average(Stream<Double> stream)
  {
    return summaryStatistics(stream).getMean();
  }
  
  private static SummaryStatistics summaryStatistics(Stream<Double> stream) 
  {
    SummaryStatistics result = new SummaryStatistics();
    stream.forEachOrdered(x -> result.addValue(x));
    return result;
  }
}
