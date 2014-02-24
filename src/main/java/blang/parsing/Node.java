package blang.parsing;

import blang.factors.Factor;

/**
 * 
 * Contains either a factor or a variable. The main purpose of this union-like 
 * datastructure is to bypass the variable's .equal and .hashCode: we do not
 * want to consider two distinct variables to be the same even if they initially
 * have the same value, because after resampling they might take on different 
 * values.
 * 
 * At the same time, makes encapsulates the type heterogeity of the factor graph's
 * vertices.
 * 
 * Note that in principle this could have been done using identity hash maps, except that this
 * might have required modifying JGraphT's internals.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Node
{
  private final Object payload;
  private final boolean isFactor;
  
  public static Node factorNode(Factor f) 
  {
    return new Node(f, true);
  }
  
  public static Node variableNode(Object variable)
  {
    return new Node(variable, false);
  }
  
  private Node(Object payload, boolean isFactor)
  {
    this.payload = payload;
    this.isFactor = isFactor;
  }

  public Factor getAsFactor()
  {
    if (!isFactor)
      throw new RuntimeException();
    return (Factor) getPayload();
  }
  
  public Object getAsVariable()
  {
    if (isFactor)
      throw new RuntimeException();
    return getPayload();
  }
  
  /**
   * Note: it is critical to keep equality/hashCode by
   * identity here
   */
  @Override
  public boolean equals(Object _other)
  {
    Node other = (Node) _other;
    if (other.isFactor != this.isFactor)
      return false;
    return getPayload() == other.getPayload();
  }
  
  /**
   * Note: it is critical to keep equality/hashCode by
   * identity here
   */
  @Override
  public int hashCode()
  {
    return System.identityHashCode(getPayload());
  }

  public boolean isFactor()
  {
    return isFactor;
  }

  public Object getPayload()
  {
    return payload;
  }
}