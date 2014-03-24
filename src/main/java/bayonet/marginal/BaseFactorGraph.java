package bayonet.marginal;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.UndirectedGraph;

import com.google.common.collect.Maps;


/**
 * 
 * Provides shared datastructure support for factor graphs.
 * 
 * A factor graph where the factors
 * are either unary or binary. In other words, a Markov random field or 
 * undirected graphical model.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <V> A datatype used to label the variables.
 */
public abstract class BaseFactorGraph<V> implements FactorGraph<V>
{
  protected final Map<V, UnaryFactor<V>> unaries = Maps.newHashMap();
  protected final Map<Pair<V,V>, BinaryFactor<V>> binaries = Maps.newHashMap();
  protected final UndirectedGraph<V, ?> topology;
  
  /**
   * 
   * @param topology The undirected graphical model. Note that since
   *   factor are at most binary, we represent the factor graph
   *   with just connection among the variables.
   */
  public BaseFactorGraph(UndirectedGraph<V, ?> topology)
  {
    this.topology = topology;
  }

  /**
   * @return The undirected graphical model. Note that since
   *   factor are at most binary, we represent the factor graph
   *   with just connection among the variables.
   */
  @Override
  public UndirectedGraph<V, ?> getTopology()
  {
    return topology;
  }
  
  /**
   * Get the unary attached to the given node label, or null if none
   * have been defined.
   * 
   * @throws RuntimeException if the node is not defined in the graph.
   * @param node
   * @return The unary at that node
   */
  @Override
  public UnaryFactor<V> getUnary(V node)
  {
    if (!topology.containsVertex(node))
      throw new RuntimeException();
    return unaries.get(node);
  }
  
  /**
   * 
   * @param node
   * @param unary
   */
  public void setUnary(V node, UnaryFactor<V> unary)
  {
    if (unaries.containsKey(node))
      throw new RuntimeException("Overwriting factors is forbidden");
    unaries.put(node, unary);
  }
  
  /**
   * Modifies in place by a unary, pointwise multiplying each entry with the provided values.
   * 
   * If no unary are currently set, set the unary to be the provided one.
   * 
   * @param node The label of the variable for which the unary will be updated in place.
   * @param newOne The unary to multiply/set.
   */
  @SuppressWarnings("unchecked")
  public void unaryTimesEqual(V node, UnaryFactor<V> newOne)
  {
    UnaryFactor<V> oldOne = unaries.get(node);
    if (oldOne == null)
      unaries.put(node, newOne);
    else
      unaries.put(node, factorOperations().pointwiseProduct(Arrays.asList(newOne, oldOne)));
  }
  
  /**
   * Get the binary attached to the given pair of nodes.
   * 
   * Note: even though the graphical model is undirected, this views
   * (marginaledNode, otherNode) as an ordered pair. This is because the
   * interface BinaryFactor is used to perform marginalization in a 
   * certain direction.
   * 
   * Methods in the descendants of this class are responsible for hiding this 
   * complexity to the user.
   */
  @Override
  public BinaryFactor<V> getBinary(V marginalizedNode, V otherNode)
  {
    if (!topology.containsEdge(marginalizedNode, otherNode))
      throw new RuntimeException();
    return binaries.get(Pair.of(marginalizedNode, otherNode));
  }
  
  /**
   * See comments in getBinary()
   * 
   * @param marginalizedNode
   * @param otherNode
   * @param factor
   */
  public void setBinary(V marginalizedNode, V otherNode, BinaryFactor<V> factor)
  {
    Pair<V,V> key = Pair.of(marginalizedNode, otherNode);
    if (binaries.containsKey(key))
      throw new RuntimeException("Overwriting factors is forbidden");
    binaries.put(key, factor);
  }
}
