package bayonet.smc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;

import bayonet.distributions.Multinomial;



public final class ParticlePopulation<P> implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  public final List<P> particles;
  private final double [] normalizedWeights;
  
  /**
   * Note: not the same as logNormEstimate() !
   * Useful to get unified weight update, no matter if re-sampling
   * was done last time or not.
   */
  public final double logScaling;
  
  /**
   * Build a new population, where the new weight is given by
   * 
   * w[i] = exp(logWeight[i] + logScaling)
   * 
   * Internally to avoid underflow, the weights are represented as
   * (1) a vector of normalized weights, normalized_w, and
   * (2) a new scaling, represented in log space, newLogScale
   * 
   * w[i] = normalized_w[i] * exp(newLogScale)
   * 
   * Note: the contents of the input logWeights is modified in place 
   * (exponentiated and normalized)
   * 
   * @param logWeights
   * @param particles
   * @param logScaling
   * @return
   */
  public static <P> ParticlePopulation<P> buildDestructivelyFromLogWeights(
      double [] logWeights, 
      final List<P> particles, 
      final double logScaling)
  {
    double logWeightsScaling = Multinomial.expNormalize(logWeights);
    return new ParticlePopulation<>(particles, logWeights, logScaling + logWeightsScaling);
  }
  
  public static <P> ParticlePopulation<P> buildEquallyWeighted(
      final List<P> particles, 
      final double logScaling)
  {
    return new ParticlePopulation<>(particles, null, logScaling);
  }
  
  private ParticlePopulation(
      final List<P> particles, 
      final double[] normalizedWeights,
      final double logScaling)
  {
    this.particles = particles;
    this.normalizedWeights = normalizedWeights;
    this.logScaling = logScaling;
  }

  public double getNormalizedWeight(final int index)
  {
    if (equallyWeighted())
      return 1.0 / nParticles();
    return normalizedWeights[index];
  }
  
  private boolean equallyWeighted()
  {
    return normalizedWeights == null;
  }
  
  public P sample(Random random)
  {
    int index = equallyWeighted()  ?
      random.nextInt(nParticles()) :
      Multinomial.sampleMultinomial(random, normalizedWeights);
    return particles.get(index);
  }
  
  public int nParticles()
  {
    return particles.size();
  }
  
  public double logNormEstimate()
  {
    return logScaling - Math.log(nParticles());
  }
  
  public ParticlePopulation<P> resample(
      final Random random, 
      final ResamplingScheme resamplingScheme)
  {
    final List<P> resampled = resamplingScheme.resample(random, normalizedWeights, particles);
    return ParticlePopulation.buildEquallyWeighted(resampled, logScaling);
  }

  public double getESS()
  {
    if (equallyWeighted())
      return nParticles();
    return 1.0 / DoubleStream.of(normalizedWeights).map(w -> w*w).sum();
  }
  
  public String weightsToString()
  {
    String result = "exp(" + logScaling + ") * ";
    if (equallyWeighted())
      result += "uniform(" + nParticles() + ")";
    result +=  Arrays.toString(normalizedWeights);
    return result;
  }

  public double getRelativeESS()
  {
    return getESS() / nParticles();
  }
}