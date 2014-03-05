package bayonet.marginal.algo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graphs;

import bayonet.graphs.GraphUtils;
import bayonet.marginal.BinaryFactor;
import bayonet.marginal.FactorGraph;
import bayonet.marginal.FactorOperation;
import bayonet.marginal.UnaryFactor;
import briefj.BriefCollections;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;



/**
 * A sum-product meta-implementation.
 * 
 * Takes care of the scheduling of the messages along a tree,
 * but leaves the details of how messages are marginalized and 
 * pointwise multiplied to an instance of FactorOperation.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <V>
 */
public class SumProduct<V>
{
  private final FactorGraph<V> factorGraph;
  private final Map<Pair<V, V>, UnaryFactor<V>> cachedMessages = Maps.newHashMap();
  private final FactorOperation<V> factorOperations;
  
  /**
   * @param factorGraph The model on which the sum product algorithm should be ran on.
   */
  public SumProduct(FactorGraph<V> factorGraph)
  {
    this.factorGraph = factorGraph;
    this.factorOperations = factorGraph.marginalizationOperation();
  }
  
  /**
   * 
   * @return The log normalization of the factor graph.
   */
  public double logNormalization()
  {
    double sum = 0.0;
    
    // add the logNormalization of each connected component
    for (Set<V> cc : GraphUtils.connectedComponents(factorGraph.getTopology()))
      sum += computeMarginal(BriefCollections.pick(cc)).logNormalization();
    
    return sum;
  }
  
  /**
   * The node marginal at the queryNode variable.
   * 
   * @param queryNode
   * @return
   */
  public UnaryFactor<V> computeMarginal(V queryNode)
  {
    computeMessages(queryNode, true);
    List<UnaryFactor<V>> queryIncomingMsgs = Lists.newArrayList();
    for (V neighbor : Graphs.neighborListOf(factorGraph.getTopology(), queryNode))
      queryIncomingMsgs.add(getFromCache(Pair.of(neighbor, queryNode), false));
    UnaryFactor<V> modelFactor = factorGraph.getUnary(queryNode);
    if (modelFactor != null)
      queryIncomingMsgs.add(modelFactor);
    return factorOperations.pointwiseProduct(queryIncomingMsgs);
  }
  
  private void computeMessages(V lastNode, boolean isForward)
  {
    if (allMessagesComputed())
      return;
    
    EdgeSorter<V> sorter = EdgeSorter.newEdgeSorter(factorGraph.getTopology(), lastNode);
    ArrayList<Pair<V,V>> allMessagesToCompute = sorter.messages(isForward);
    
    for (Pair<V,V> messageToCompute : allMessagesToCompute)
      if (!cachedMessages.containsKey(messageToCompute))
        cachedMessages.put(messageToCompute, computeMessage(messageToCompute));
  }

  private boolean allMessagesComputed()
  {
    // when everything is computed, there is one message flowing 
    // in both direction for each edge
    return factorGraph.getTopology().edgeSet().size() == 2 * cachedMessages.size();
  }
  
  private UnaryFactor<V> computeMessage(
      Pair<V, V> messageToCompute)
  {
    V source = messageToCompute.getLeft(),
      destination = messageToCompute.getRight();
    
    // gather incoming factors
    List<UnaryFactor<V>> toMultiply = Lists.newArrayList();
    for (Pair<V,V> incomingPreviouslyComputedMessages : GraphUtils.distinctIncoming(factorGraph.getTopology(), messageToCompute))
      toMultiply.add(getFromCache(incomingPreviouslyComputedMessages, false));
    UnaryFactor<V> modelFactor = factorGraph.getUnary(source);
    if (modelFactor != null)
      toMultiply.add(modelFactor);
    
    // marginalize one node
    BinaryFactor<V> binaryFactor = factorGraph.getBinary(source, destination);
    checkIntegrity(messageToCompute, binaryFactor, toMultiply);
    return factorOperations.marginalize(binaryFactor, toMultiply);
  }

  private void checkIntegrity(Pair<V, V> messageToCompute,
      BinaryFactor<V> binaryFactor, List<UnaryFactor<V>> toMultiply)
  {
    V source = messageToCompute.getLeft(),
    destination = messageToCompute.getRight();
    if (!binaryFactor.marginalizedNode().equals(source))
      throw new RuntimeException();
    if (!binaryFactor.otherNode().equals(destination))
      throw new RuntimeException();
    for (UnaryFactor<V> factor : toMultiply)
      if (!factor.connectedVariable().equals(source))
        throw new RuntimeException();
  }

  private UnaryFactor<V> getFromCache(Pair<V,V> key, boolean allowNulls)
  {
    UnaryFactor<V> result = cachedMessages.get(key);
    if (!allowNulls && result == null)
      throw new RuntimeException();
    return result;
  }
  
}
