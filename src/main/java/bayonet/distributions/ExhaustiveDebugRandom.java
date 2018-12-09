package bayonet.distributions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * A non standard implementation of a Random generator for debugging fully 
 * discrete probability models. The idea is that the (transition) probabilities of 
 * a sampling or transition process can be constructed explicitly via
 * 
 * ExhaustiveDebugRandom debugRand = new ExhaustiveDebugRandom();
 * while (debugRand.hasNext())
 * {
 *   Type complicatedRandomObject = someComplicatedFunction(debugRand);
 *   // increment the probability of complicatedRandomObject by debugRand.lastProbability()
 * }
 * 
 * Here someComplicatedFunction can have loops with random bounds, conditional, pretty much anything 
 * as long as it uses debugRand only via the discrete probability methods below. Invoking 
 * continuous probability generator will throw an exception.
 * 
 * @author bouchard
 *
 */
public class ExhaustiveDebugRandom extends Random
{
  private static final long serialVersionUID = -6477347343344095215L;
  
  private static final RandomGenerator dummy = null;
  public ExhaustiveDebugRandom()
  {
    super(dummy);
  }
  
  public boolean hasNext()
  {
    boolean oldHasNext = hasNext;
    init();
    return oldHasNext;
  }
  
  private void init() 
  {
    hasNext = false;
    
    oldDecisions = newDecisions;
    newDecisions = new ArrayList<>();
    
    oldDeepestIncrementableBranch = newDeepestIncrementableBranch;
    newDeepestIncrementableBranch = -1;
    
    currentDepth = 0;
    
    pr = 1.0;
  }
  
  public double lastProbability()
  {
    return pr;
  }
  
  public int lastDepth()
  {
    return newDecisions.size();
  }
  
  @Override
  public boolean nextBernoulli(double p)
  {
    if (!(p >= 0.0 && p <= 1.0))
      throw new IllegalArgumentException("Parameter should be a probability: " + p);
    double [] prs = new double[]{1.0 - p, p};
    return nextCategorical(prs) == 1 ? true : false;
  }
  
  @Override
  public int nextCategorical(double [] probabilities)
  {
    List<Integer> translated = effectiveProbabilities(probabilities);
    
    int oldDecision = oldDecisions == null || currentDepth > oldDeepestIncrementableBranch ? 0 : oldDecisions.get(currentDepth);
    int currentDecision;
    if (currentDepth < oldDeepestIncrementableBranch)
      currentDecision = oldDecision;
    else if (currentDepth == oldDeepestIncrementableBranch)
      currentDecision = oldDecision + 1;
    else
      currentDecision = 0;
    
    newDecisions.add(currentDecision);
    if (currentDecision < translated.size() - 1) // more decision are possible at that branch
    {
      hasNext = true;
      newDeepestIncrementableBranch = currentDepth;
    }
    
    currentDepth++;
    
    int sampledIndex = translated.get(currentDecision);
    pr *= probabilities[sampledIndex];
    return sampledIndex;
  }
  
  List<Integer> oldDecisions = null;
  List<Integer> newDecisions = null;
  int oldDeepestIncrementableBranch = -1;
  int newDeepestIncrementableBranch = -1;
  int currentDepth = -1;
  boolean hasNext = true;
  double pr = Double.NaN;

  private List<Integer> effectiveProbabilities(double[] probabilities)
  {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < probabilities.length; i++)
      if (probabilities[i] > 0)
        result.add(i);
    return result;
  }

  @Override
  public boolean nextBoolean()
  {
    return nextBernoulli(0.5);
  }
  
  @Override
  public int nextInt(int n)
  {
    double [] prs = new double[n];
    for (int i = 0; i < n; i++)
      prs[i] = 1.0 / ((double) n);
    return nextCategorical(prs);
  }

  @Override
  protected int next(int bits)
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public IntStream ints(long streamSize)
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public IntStream ints()
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public LongStream longs(long streamSize)
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public LongStream longs()
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound)
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public LongStream longs(long randomNumberOrigin, long randomNumberBound)
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public DoubleStream doubles(long streamSize)
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public DoubleStream doubles()
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound)
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound)
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public void nextBytes(byte[] bytes)
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public double nextDouble()
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public float nextFloat()
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public double nextGaussian()
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public int nextInt()
  {
    throw new UnsupportedOperationException(MESSAGE);
  }

  @Override
  public long nextLong()
  {
    throw new UnsupportedOperationException(MESSAGE);
  }
  
  private static String MESSAGE = "ExhaustiveDebugRandom only works with a subset of the random generation methods, essentially discrete ones having a bounded support.";
}
