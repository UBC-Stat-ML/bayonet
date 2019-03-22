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
    SummaryStatistics globalStats = new SummaryStatistics();
    for (double sample : samples)
      globalStats.addValue(sample);
    
    int nBlocks = 1 + (int) Math.sqrt(samples.size());
    int partitionSize = 1 + samples.size() / nBlocks;
    List<List<Double>> split = Lists.partition(samples, partitionSize);
    
    SummaryStatistics blockStats = new SummaryStatistics();
    for (List<Double> block : split)
      blockStats.addValue(average(block.stream()));
    
    return ess(split.size(), globalStats.getVariance(), blockStats.getVariance());
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
    return stream.mapToDouble(a -> a).average().orElseGet(() -> Double.NaN);
  }
}
