package bayonet.smc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import briefj.opt.Option;


/**
 * Performs one cycle of importance sampling + resampling in a streaming fashion.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <S>
 */
public class StreamingPropagator<S>
{
  public final ProposalWithRestart<S> proposal;
  public final PropagatorOptions options;
  private Consumer<S> processor = null;
  
  public StreamingPropagator(ProposalWithRestart<S> proposal, PropagatorOptions options)
  {
    this.proposal = proposal;
    this.options = options;
  }
  
  public void addProcessor(Consumer<S> newProcessor)
  {
    if (newProcessor == null)
      throw new RuntimeException();
    if (this.processor == null)
      this.processor = newProcessor;
    else
      this.processor = this.processor.andThen(newProcessor);
  }

  public StreamingPropagator(ProposalWithRestart<S> proposal)
  {
    this(proposal, new PropagatorOptions());
  }
  
  public static class PropagationResult<S>
  {
    /**
     * The virtual (implicit) particles.
     */
    public final CompactPopulation population;
    
    /**
     * The concrete samples obtained by resampling the virtual particles.
     */
    public final List<S> samples;
    
    private PropagationResult(CompactPopulation population, List<S> samples)
    {
      this.population = population;
      this.samples = samples;
    }
  }
  
  /**
   * Performs one cycle of importance sampling + resampling.
   * @return
   */
  public PropagationResult<S> execute()
  {
    CompactPopulation population = new CompactPopulation();
    propose(
        population, 
        options.targetedRelativeESS, 
        options.numberOfConcreteParticles, 
        options.maxNumberOfVirtualParticles);
    if (options.verbose)
      System.out.println(
            "nVirtual=" + population.getNumberOfParticles() + ", "
          + "nConcrete=" + options.numberOfConcreteParticles + ", "
          + "relative_ess=" + (population.ess()/options.numberOfConcreteParticles));
    double [] sortedCumulativeProbabilitiesForFinalResampling = 
        options.resamplingScheme.getSortedCumulativeProbabilities(
            options.resamplingRandom, 
            options.numberOfConcreteParticles);
    List<S> samples = resample(
        population, 
        sortedCumulativeProbabilitiesForFinalResampling);
    return new PropagationResult<>(population, samples);
  }
  
  /**
   * Perform resampling by replaying randomness to instantiate
   * concrete version of the particles that survive the resampling step.
   * 
   * @param proposal
   * @param sortedCumulativeProbabilities See ResamplingScheme
   * @return The list of resampled, equi-weighted particles
   */
  private List<S> resample(
      CompactPopulation population,
      double [] sortedCumulativeProbabilities)
  {
    ProposalWithRestart<S> proposal = this.proposal;
    if (proposal.numberOfCalls() != 0)
    {
      proposal = proposal.restart();
      
      if (proposal.numberOfCalls() != 0)
        throw new RuntimeException("restart() incorrectly implemented");
    }
    
    final double logSum = population.getLogSum();
    final int nParticles = population.getNumberOfParticles();
    final int popAfterCollapse = sortedCumulativeProbabilities.length;
    final List<S> result = new ArrayList<>(popAfterCollapse);
    CompactPopulation sanityCheck = new CompactPopulation();
    
    double normalizedPartialSum = 0.0;
    S candidate = null;
    for (int i = 0; i < popAfterCollapse; i++)
    {
      double nextCumulativeProbability = sortedCumulativeProbabilities[i];
      // sum normalized weights until we get to the next resampled cumulative probability
      while (normalizedPartialSum < nextCumulativeProbability) 
      {
        int before = proposal.numberOfCalls();
        Pair<Double, S> nextLogWeightSamplePair = proposal.nextLogWeightSamplePair();
        if (proposal.numberOfCalls() != before + 1)
          throw new RuntimeException("The method numberOfCalls() was incorrectly implemented in the proposal");
        candidate = nextLogWeightSamplePair.getRight();
        final double normalizedWeight = Math.exp(nextLogWeightSamplePair.getLeft() - logSum);
        normalizedPartialSum += normalizedWeight;
        sanityCheck.insertLogWeight(nextLogWeightSamplePair.getLeft());
      }
      // we have found one particle that survived the collapse
      result.add(candidate);
    }
    
    // replay the last few calls of the proposal sequence to make sure things were indeed behaving deterministically
    for (int i = proposal.numberOfCalls(); i < nParticles; i++)
      sanityCheck.insertLogWeight(proposal.nextLogWeight());
    if (sanityCheck.getLogSum() != logSum || sanityCheck.getLogSumOfSquares() != population.getLogSumOfSquares()) 
      throw new RuntimeException("The provided proposal does not behave deterministically: " + sanityCheck.getLogSum() + " vs " + logSum);
    
    return result;
  }
  
  /**
   * Grow this population by using a proposal distribution.
   * 
   * The number of particles proposed is determined as follows:
   * First, the proposal will be called at least minNumberOfParticles times.
   * Then, after minNumberOfParticles have been proposed, the growth will continue 
   * until the first of these two condition is met:
   * - maxNumberOfParticles is exceeded
   * - the relative ESS exceeds the targetedRelativeESS (here relative ESS is defined
   *   as ESS divided by minNumberOfParticles, since minNumberOfParticle will 
   *   correspond to the number of 'concrete' particles)
   * 
   * @param proposal
   * @param targetedRelativeESS
   * @param minNumberOfParticles
   * @param maxNumberOfParticles
   */
  private void  propose( 
    CompactPopulation population,
    double targetedRelativeESS,
    int minNumberOfParticles,
    int maxNumberOfParticles)
  {
    while 
        ( 
          population.getNumberOfParticles() < minNumberOfParticles || 
          (
              population.getNumberOfParticles() < maxNumberOfParticles && 
              population.ess() / minNumberOfParticles < targetedRelativeESS
          )
        )
      population.insertLogWeight(proposal.nextLogWeight());
  }
  
  public static final class PropagatorOptions
  {
    @Option 
    public boolean verbose = true;

    @Option(gloss = "Number of particles stored in memory.")
    public int numberOfConcreteParticles = DEFAULT_N_CONCRETE_PARTICLES;

    @Option(gloss = "Maximum number of implicit particles, represented via random number generation replay. Only costs CPU, not memory.")
    public int maxNumberOfVirtualParticles = 1000000;

    @Option(gloss = "Virtual particles will be used until that relative effective sampling size is reached (or maxNumberOfVirtualParticles is reached)")
    public double targetedRelativeESS = 0.5;
    
    @Option
    public Random resamplingRandom = new Random(1);
    
    @Option
    public ResamplingScheme resamplingScheme = ResamplingScheme.STRATIFIED;
    
    public static final int DEFAULT_N_CONCRETE_PARTICLES = 1000;
  }
}
