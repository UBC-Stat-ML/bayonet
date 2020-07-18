package bayonet.math;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.collect.Lists;

public class AutoCorrTime {
  
  public static double ess(List<Double> values)
  {
    return ((double) values.size())/estimateAutoCorrTime(values);
  }
  
  public static double estimateAutoCorrTime(List<Double> values)
  {
    double sum = 0.0;
    List<Double> autocorrelationFunction = estimateAutocorrelationFunction(values);
    for (int i = 1; i < autocorrelationFunction.size(); i++)
      sum += 2 * autocorrelationFunction.get(i);
    return 1.0 + sum;
  }

  /**
   * Given a real time series, estimate the autocorrelation function.
   * 
   * Uses the initial positive sequence heuristic (http://arxiv.org/pdf/1011.0175)
   * to adaptively truncate the series and avoid infinite variance.
   * 
   * Warning: this heuristic only works for reversible processes.
   * 
   * Running time will be [length of non-truncated AFC] x [series.size],
   * so could be series.size^2 in worse case but when non-truncated ACF is 
   * short should be competitive with FFT based methods.
   * 
   * @param series
   * @return Autocorrelation function, where entry i is the i-th order
   */
  public static List<Double> estimateAutocorrelationFunction(List<Double> series)
  {
    SummaryStatistics ss = new SummaryStatistics();
    for (double v : series) ss.addValue(v);
    final double sampleVar = ss.getVariance();
    final double mean = ss.getMean();
    double [] data = new double[series.size()];
    for (int i =0 ; i < series.size(); i++)
      data[i] = series.get(i);
    List<Double> result = Lists.newArrayList();
    List<Double> autocovar = Lists.newArrayList();
    final double n = series.size();
    double factor = 1.0 / sampleVar;
    result.add(1.0);
    autocovar.add(sampleVar);
    loop:for (int i = 1; i < n - 1; i++)
    {
      autocovar.add(autocovariance(data, i, mean));
      if (i > 1 && autocovar.get(i) + autocovar.get(i - 1) < 0.0) 
        break loop;
      result.add( factor * autocovar.get(i));
    }
    return result;
  }
  
  static double autocovariance(double[] x, int shift, double mean) {
    if (shift >= x.length - 1) throw new RuntimeException();
    double sum = 0.0;
    double factor = 1.0 / (x.length - shift - 1);
    for (int i = 0; i < x.length - shift; i++) {
      sum += factor * (x[i]-mean) * (x[i + shift]-mean);
    }
    return sum;
  }
}
