package bayonet.smc;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import bayonet.smc.StreamingPropagator.PropagatorOptions;
import bayonet.smc.StreamingPropagator.PropagationResult;


/**
 * Implementation of:
 * Seong-Hwan Jun and Alexandre Bouchard-Côté. (2014) Memory (and Time) Efficient Sequential Monte Carlo. International Conference on Machine Learning (ICML).
 * 
 * See TestStreamingBootstrap for an example of usage.
 * 
 * This implementation will be mostly useful in a 'likelihood-free' scenario
 * where the ess plummets at every iteration. 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <L> type for latent states
 * @param <E> type for observations
 */
public class StreamingBootstrapFilter<L,E>
{
  private final List<E> emissions;
  private final LatentSimulator<L> transitionDensity;
  private final ObservationDensity<L,E> observationDensity;
  private final Random mainRandom = new Random(1);
  
  public StreamingBootstrapFilter(
      LatentSimulator<L> transitionDensity,
      ObservationDensity<L, E> observationDensity,
      List<E> emissions)
  {
    this.emissions = emissions;
    this.transitionDensity = transitionDensity;
    this.observationDensity = observationDensity;
  }

  public PropagatorOptions options = new PropagatorOptions();
  
  public static interface LatentSimulator<L> 
  {
    public L sampleInitial(Random random);
    public L sampleForwardTransition(Random random, L state);
  }
  public static interface ObservationDensity<L,E>
  {
    public double logDensity(L latent, E emission);
  }
  
  /**
   * @return The estimate for log(Z)
   */
  public double sample()
  {
    // initial distribution
    StreamingBootstrapProposal proposal = getInitialDistributionProposal();
    StreamingPropagator<L> propagator = new StreamingPropagator<L>(proposal, options);
    PropagationResult<L> propResults = propagator.execute();
    double logZ = propResults.population.logZEstimate();
    
    // recursion
    for (int i = 1; i < emissions.size(); i++)
    {
      proposal = new StreamingBootstrapProposal(mainRandom.nextLong(), emissions.get(i), propResults.samples);
      propagator = new StreamingPropagator<>(proposal, options);
      propResults = propagator.execute();
      logZ += propResults.population.logZEstimate();
    }
    return logZ;
  }
  
  private StreamingBootstrapProposal getInitialDistributionProposal()
  {
    return new StreamingBootstrapProposal(mainRandom.nextLong(), emissions.get(0), null);
  }

  /**
   * Adapt the more abstract machinery of the lazy proposal/propagator to the
   *  simpler bootstrap filter case.
   *  
   * This type is used both for the initialization (in which case oldLatents is null), and
   * for the recursion steps. One instance is created at each SMC generation.
   *  
   * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
   *
   */
  private class StreamingBootstrapProposal implements ProposalWithRestart<L>
  {
    private final long seed;
    private final PermutationStream permutationStream;
    private final Random random;
    
    private final E curEmission;
    private final List<L> oldLatents;
    private int nCalls = 0;

    private StreamingBootstrapProposal(long seed, E curEmission, List<L> oldLatents)
    {
      this.seed = seed;
      this.curEmission = curEmission;
      this.oldLatents = oldLatents;
      this.random = new Random(seed * 171);
      this.permutationStream = oldLatents == null ? null : new PermutationStream(oldLatents.size(), random);
    }

    @Override
    public Pair<Double, L> nextLogWeightSamplePair()
    {
      // terminology: old means the SMC generation before current (null if we are doing initial)
      //              cur means the current SMC generation
      L curLatent = isInitial() 
          ? transitionDensity.sampleInitial(random)
          : transitionDensity.sampleForwardTransition(random, sampleOldLatent());
      double logWeight = observationDensity.logDensity(curLatent, curEmission);
      nCalls++;
      return Pair.of(logWeight, curLatent);
    }
    
    private boolean isInitial() 
    { 
      return oldLatents == null; 
    }

    private L sampleOldLatent()
    {
      return oldLatents.get(permutationStream.popIndex());
    }

    @Override
    public int numberOfCalls()
    {
      return nCalls;
    }

    @Override
    public ProposalWithRestart<L> restart()
    {
      return new StreamingBootstrapProposal(seed, curEmission, oldLatents);
    }
  }
}
