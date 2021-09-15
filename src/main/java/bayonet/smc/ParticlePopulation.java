package bayonet.smc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;

import bayonet.distributions.ExhaustiveDebugRandom;
import bayonet.distributions.Multinomial;
import bayonet.smc.ResamplingScheme.ResampledContext;



public final class ParticlePopulation<P> implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  public final List<P> particles;
  public final List<Integer> ancestors;
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
      final List<Integer> ancestors,
      final double logScaling)
  {
    if (logWeights.length != particles.size())
      throw new RuntimeException("Dimensionality of weights should match the dim of particles");
    double logWeightsScaling = Multinomial.expNormalize(logWeights);
    return new ParticlePopulation<>(particles, ancestors, logWeights, logScaling + logWeightsScaling);
  }
  
  public static <P> ParticlePopulation<P> buildEquallyWeighted(
      final List<P> particles, 
      final List<Integer> ancestors,
      final double logScaling)
  {
    return new ParticlePopulation<>(particles, ancestors, null, logScaling);
  }
  
  private ParticlePopulation(
      final List<P> particles, 
      final List<Integer> ancestors,
      final double[] normalizedWeights,
      final double logScaling)
  {
    this.ancestors = ancestors;
    this.particles = particles;
    this.normalizedWeights = normalizedWeights;
    this.logScaling = logScaling;
  }

  public double [] getLogWeights()
  {
    double [] result = normalizedWeights.clone();
    int i = 0;
    for (double normalizedWeight : result) {
      result[i] = Math.log(normalizedWeight) + logScaling;
      i++;
    }
    return result;
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
    if (random instanceof ExhaustiveDebugRandom)
    {
      // If we're using an ExhaustiveDebugRandom, use only discrete random generation
      ExhaustiveDebugRandom debugRandom = (ExhaustiveDebugRandom) random;
      double [] prs = new double[nParticles()];
      for (int i = 0; i < nParticles(); i++)
        prs[i] = getNormalizedWeight(i);
      List<P> resampled = new ArrayList<>();
      List<Integer> ancestors = new ArrayList<>();
      for (int i = 0; i < nParticles(); i++) {
        int particleIdx = debugRandom.nextCategorical(prs);
        ancestors.add(particleIdx);
        resampled.add(particles.get(particleIdx));
      }
      return ParticlePopulation.buildEquallyWeighted(resampled, ancestors, logScaling);
    }
    else
    {
      ResampledContext<P> resampledContext = resamplingScheme.resample(random, normalizedWeights, particles);
      final List<P> resampled = resampledContext.particles;
      final List<Integer> ancestors = resampledContext.ancestors;
      return ParticlePopulation.buildEquallyWeighted(resampled, ancestors, logScaling);
    }
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
