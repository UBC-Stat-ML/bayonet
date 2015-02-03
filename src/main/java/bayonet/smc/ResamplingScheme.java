package bayonet.smc;

import java.util.Random;

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
  };
  
  /**
   * @param rand
   * @param nDarts
   * @return The location of the n darts, sorted in ascending order.
   */
  public abstract double[] getSortedCumulativeProbabilities(Random rand, int nDarts);
}