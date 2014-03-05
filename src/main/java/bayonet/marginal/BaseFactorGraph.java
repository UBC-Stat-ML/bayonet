package bayonet.marginal;

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
   * Get the unary attached to the given node label
   * 
   * @throws RuntimeException if there are no unaries at that node.
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
