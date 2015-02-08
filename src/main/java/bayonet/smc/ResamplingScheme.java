package bayonet.smc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.primitives.Doubles;

import bayonet.distributions.Uniform;


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
}