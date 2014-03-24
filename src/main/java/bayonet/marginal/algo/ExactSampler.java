package bayonet.marginal.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bayonet.marginal.BinaryFactor;
import bayonet.marginal.FactorGraph;
import bayonet.marginal.Sampler;
import bayonet.marginal.UnaryFactor;


/**
 * Performs forward and posterior sampling.
 * 
 * TODO: currently runs in time quadratic of the number of states at a single node. Could be
 * done in time linear, but in the posterior case there will be a quadratic cost anyways for
 * computing the forward pass, so only a constant factor slower this way. 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class ExactSampler<V>
{
  private final FactorGraph<V> factorGraph;
  private final SumProduct<V> posterior;
  private final Sampler<V> sampler;
  private final boolean isPosterior;
  
  /**
   * Creates a new posterior sampler from a posterior computed using the sum product algorithm.
   * 
   * @param <V>
   * @param posterior
   * @param sampler
   * @return
   */
  public static <V> ExactSampler<V> posteriorSampler(SumProduct<V> posterior, Sampler<V> sampler)
  {
    return new ExactSampler<V>(posterior.getFactorGraph(), posterior, sampler, true);
  }
  
  /**
   * Creates a new prior sampler on the provided factor graph view of a directed graphical model.
   * 
   * @param <V>
   * @param factorGraph
   * @param sampler
   * @return
   */
  public static <V> ExactSampler<V> priorSampler(FactorGraph<V> factorGraph, Sampler<V> sampler)
  {
    return new ExactSampler<V>(factorGraph, null, sampler, false);
  }

  /**
   * Compute a sample for the connected component rooted at the given root.
   * 
   * @param rand
   * @param root
   * @return A map mapping each node to a UnaryFactor encoding a dirac delta on the extracted sample.
   */
  public Map<V,UnaryFactor<V>> sample(Random rand, V root)
  {
    EdgeSorter<V> sorter = EdgeSorter.newEdgeSorter(factorGraph.getTopology(), root);
    ArrayList<Pair<V,V>> allMessagesToCompute = sorter.backwardMessages();
    Map<V,UnaryFactor<V>> result = Maps.newHashMap();
    
    // init at the root
    UnaryFactor<V> rootFactor = isPosterior ? 
        posterior.computeMarginal(root) : 
        factorGraph.getUnary(root);
    if (rootFactor == null)
      throw new RuntimeException("The root node (" + root + ") should have a factor in order to have a sampling process to start there.");
    result.put(root, sampler.sample(rand, sampler.sample(rand, rootFactor)));
    
    for (Pair<V,V> messageToCompute : allMessagesToCompute)
    {
      V source = messageToCompute.getLeft(),
        destination = messageToCompute.getRight();
      
      // standard marginalization using unary from map
      UnaryFactor<V> dirac = result.get(source);
      BinaryFactor<V> binaryFactor = factorGraph.getBinary(source, destination);
      List<UnaryFactor<V>> diracSingleton = Collections.singletonList(dirac);
      UnaryFactor<V> marginalized = factorGraph.factorOperations().marginalize(binaryFactor, diracSingleton);
      
      // pointwise product if posterior mode
      if (isPosterior)
      {
        UnaryFactor<V> subtreeMarginal = posterior.computeSubtreeMarginal(destination, source);
        @SuppressWarnings("unchecked")
        List<UnaryFactor<V>> toMultiply = Lists.newArrayList(subtreeMarginal, marginalized);
        marginalized = factorGraph.factorOperations().pointwiseProduct(toMultiply);
      }
      else
      {
        if (factorGraph.getUnary(destination) != null)
          throw new RuntimeException("In order to do prior sampling, there should be factors only at the roots.");
      }
      
      UnaryFactor<V> newDirac = sampler.sample(rand, marginalized);
      result.put(destination, newDirac);
    }
    
    return result;
  }
  
  private ExactSampler(FactorGraph<V> factorGraph, SumProduct<V> posterior,
      Sampler<V> sampler, boolean isPosterior)
  {
    this.factorGraph = factorGraph;
    this.posterior = posterior;
    this.sampler = sampler;
    this.isPosterior = isPosterior;
  }
  
}
