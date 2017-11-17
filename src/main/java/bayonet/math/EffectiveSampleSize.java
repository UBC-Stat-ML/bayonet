package bayonet.math;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.collect.Lists;

/**
 * Computes the ESS based on batch estimators. Generally more stable than the 
 * ACT-based methods (e.g. in AutoCorrTime.java) or those based on fitting an AR 
 * model (e.g. in EffectiveSize.java)
 */
public class EffectiveSampleSize
{
  public static double ess(List<Double> samples)
  {
    int nBlocks = 1 + (int) Math.sqrt((double) samples.size());
    int partitionSize = 1 + samples.size() / nBlocks;
    List<List<Double>> split = Lists.partition(samples, partitionSize);
    
    SummaryStatistics blockStats = new SummaryStatistics();
    for (List<Double> block : split)
      blockStats.addValue(average(block.stream()));
    
    double squareIntegral = average(samples.stream(), x -> x*x);
    return ess(squareIntegral, blockStats);
  }
  
  public static double ess(List<Double> samples, Function<Double, Double> transformation)
  {
    return ess(samples.stream().map(transformation).collect(Collectors.toList()));
  }
  
  public static double ess(double squareIntegral, SummaryStatistics blockStats) 
  {
    double variance = squareIntegral - Math.pow(blockStats.getMean(), 2);
    return blockStats.getN() * variance / blockStats.getVariance();
  }
  
  private static double average(Stream<Double> stream, Function<Double, Double> f)
  {
    return average(stream.map(f));
  }

  private static double average(Stream<Double> stream)
  {
    return stream.mapToDouble(a -> a).average().orElseGet(() -> Double.NaN);
  }
}
