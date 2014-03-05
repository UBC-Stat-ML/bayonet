package bayonet.marginal;

import org.jgrapht.UndirectedGraph;

/**
 * Contains unary and binary factors, as well as algorithms to perform
 * marginalization and pointwise products, the two operations required 
 * by the sum product algorithm.
 * 
 * See http://www.stat.ubc.ca/~bouchard/courses/stat547-sp2013-14/lecture/2014/01/29/lecture8.html
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <V>
 */
public interface FactorGraph<V>
{
  /**
   * The undirected graphical model. Note that since
   *   factor are at most binary, we represent the factor graph
   *   with just connection among the variables.
   */
  public UndirectedGraph<V,?> getTopology();
  
  /**
   * 
   * @return The algorithms that do marginalization and pointwise products.
   */
  public FactorOperation<V> marginalizationOperation();
  
  /**
   * @param node
   * @return The unary at that node
   */
  public UnaryFactor<V> getUnary(V node);
  
  /**
   * 
   * @param marginalizedNode
   * @param otherNode
   * @return
   */
  public BinaryFactor<V> getBinary(V marginalizedNode, V otherNode);
}
