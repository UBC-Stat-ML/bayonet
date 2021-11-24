package bayonet.smc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.primitives.Doubles;

import bayonet.distributions.Uniform;
import bayonet.math.NumericalUtils;


/**
 * Resampling here is viewed as a way of throwing n darts on the unit interval in such 
 * a way as to have an expected number of l darts falling on subintervals of length l.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public enum ResamplingScheme
{
  /**
   * See http://biblio.telecom-paristech.fr/cgi-bin/download.cgi?id=5755
   */
  STRATIFIED {
    @Override
    public double [] getSortedCumulativeProbabilities(Random rand, int nDarts)
    {
      final double [] result = new double[nDarts];
      final double spacing = 1.0/((double) nDarts);
      for (int i = 0; i < nDarts; i++)
      {
        final double positionInBin = Uniform.generate(rand, 0.0, spacing);
        result[i] = spacing * i + positionInBin;
      }
      return result;
    }
  },
  MULTINOMIAL {
    @Override
    public double[] getSortedCumulativeProbabilities(Random rand, int nDarts)
    {
      // Note: uses an n log(n) algo. Linear is possible using normalized exponentials,
      // but constant is larger than the sorting-based algorithm.
      List<Double> darts = new ArrayList<>(nDarts);
      for (int i = 0; i < nDarts; i++)
        darts.add(rand.nextDouble());
      Collections.sort(darts);
      return Doubles.toArray(darts);
    }
  };
  
  /**
   * @param rand
   * @param nDarts
   * @return The location of the n darts, sorted in ascending order.
   */
  public abstract double[] getSortedCumulativeProbabilities(Random rand, int nDarts);
  public class ResampledContext<T> {
    List<T> particles;
    List<Integer> ancestors;
  }

  public <T> ResampledContext<T> resample(
      final Random rand, 
      final double [] w, 
      final List<T> particles)
  {
    return resample(rand, w, particles, particles.size());
  }
  
  public <T> ResampledContext<T> resample(
      final Random rand, 
      final double [] w, 
      final List<T> particles, 
      final int nSamples)
  {
    final double [] darts = getSortedCumulativeProbabilities(rand, nSamples); 
    final List<T> result = new ArrayList<>(nSamples);
    final List<Integer> ancestors = new ArrayList<Integer>(nSamples);
    double sum = 0.0;
    int nxtDartIdx = 0;
    for (int i = 0; i < w.length; i++)
    {
      final double curLen = w[i];
      if (curLen < 0 - NumericalUtils.THRESHOLD)
        throw new RuntimeException();
      final double right = sum + curLen;
      
      innerLoop : for (int dartIdx = nxtDartIdx; dartIdx < darts.length; dartIdx++)
        if (darts[dartIdx] < right)
        {
          result.add(particles.get(i)); //result.incrementCount(i, 1.0);
          ancestors.add(i);
          nxtDartIdx++;
        }
        else 
          break innerLoop;
      sum = right;
    }
    if (Double.isNaN(sum))
      throw new RuntimeException();
    NumericalUtils.checkIsClose(1.0, sum);
    if (result.size() != nSamples)
      throw new RuntimeException();
    ResampledContext<T> resampledContext = new ResampledContext<T>();
    resampledContext.particles = result;
    resampledContext.ancestors = ancestors;
    return resampledContext;
  }
}