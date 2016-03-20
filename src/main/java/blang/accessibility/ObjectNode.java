package blang.accessibility;

import blang.accessibility.AccessibilityGraph.Node;




/**
 * TODO: This comment block is development note, to be erased later.
 * Example of how to create a slice:
 * 
 * class MyArrayView implements ArrayView
 * {
 *   @PartiallyAccessedArray
 *   private double [] theArray;
 *   
 *   @Override
 *   public Collection<Integer> getAccessibleIndices()
 *   {
 *     ...
 *   }
 * }
 * 
 * with a custom rule that by passes the array reference.
 * 
 * Same for sublist, etc.
 */

public class ObjectNode implements Node
{
  final Object object;
  
  public ObjectNode(Object object)
  {
    if (object == null)
      throw new RuntimeException();
    this.object = object;
  }
  
  @Override
  public int hashCode()
  {
    return System.identityHashCode(object);
  }
  
  @Override
  public String toString()
  {
    return "ObjectNode[class=" + object.getClass().getName() + ",objectId=" + System.identityHashCode(object) + "]";
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (!(obj instanceof ObjectNode))
      return false;
    return ((ObjectNode) obj).object == this.object;
  }
}