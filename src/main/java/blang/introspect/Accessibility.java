package blang.introspect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;




public class Accessibility
{
  /**
   * A node (factor, variable, or component) in the factor graph.
   * 
   * Each such node is associated to an address in memory.
   * 
   * This association could be done via:
   * 
   * 1. a reference to an object o (with hashCode and equals based on o's identity instead of potential overloaded hashCode and equal)
   * 2. a reference to a container c (e.g., an array, or List) as well as a key k (in this case, hashCode and equal are based on a 
   *    combination of the identity of c, and the standard hashCode and equal of k)
   * 3. Possible future extension: a reference to an object as well as one of the fields of this object
   * 
   * The main use is (1), but (2) will be needed for example to obtain slices of 
   * a matrix. [The rest of this entry is development note, to erase later] This last feature is done via a custom interface of the form:
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
   * 
   * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
   *
   */
  public static interface Node
  {
    /**
     * 
     * 
     * @return Either null, if this node is associated with a primitive, else, a reference to the object associated to this node.
     */
    public Object resolve();
    
    public int hashCode();
    public boolean equals(Object obj); 
  }
  
  public static Node getObjectNode(Object o)
  {
    throw new RuntimeException();
  }
  
  public static <K> Node getConstituentNode(Object container, K key)
  {
    throw new RuntimeException();
  }
  
  public static class Edge
  {
    /**
     * The pointer holding the reference
     */
    private final Node source;
    
    /**
     * 
     */
    private final Node destination;
    
    /**
     * Null if destination is a ConstituentNode, otherwise, the field 
     * in source that refers to destination.
     * 
     * Note: the related information is not needed for ConstituentNode's since it
     * can be accessed via destination's key. On the other hand, the field information is not 
     * stored in the destination Node, as several different nodes can refer to a given object node, 
     * in contrast to constituent nodes, which are only possessed by their constituent.
     */
    private final Field field;
  }
  
  public static interface EdgeProcessor<K>
  {
//    public void processPrimitive(K key);
//    public void processObject(K key, Object child);
  }
  
  public static interface ExplorationRule
  {
    public boolean apply(Object o, EdgeProcessor e);
  }
  
  // TODO: use functional interface instead, static defaults, etc
  
  public static class DefaultExplorationRule implements ExplorationRule
  {

    @Override
    public boolean apply(Object o, EdgeProcessor e)
    {
      // go over all fields (incl super, containing, others?)
      
        // call processor.processChildObjectNode
      
      return true;
    }
    
  }
  
  public static class 
  
  
  public static void buildAccessibilityGraph(Object root, List<? extends ExplorationRule> rules)
  {
    final LinkedList<? super Object> toExploreQueue = new LinkedList<>();
    toExploreQueue.add(root);
    
    EdgeProcessor processor = null;
//        new EdgeProcessor<K>() {
//  
//      @Override
//      public void processPrimitive(blang.introspect.K key)
//      {
//        // TODO Auto-generated method stub
//        
//      }
//  
//      @Override
//      public void processObject(blang.introspect.K key, Object child)
//      {
//        // TODO Auto-generated method stub
//        
//      }
//    };
//    
    Set<? super Object> explored = Collections.newSetFromMap(new IdentityHashMap<>());
    
    while (!toExploreQueue.isEmpty())
    {
      Object current = toExploreQueue.poll();
      
      // add to explored first to handle the case an object has a pointer to itself without causing an infinite loop
      explored.add(current);
      
      ruleApplication : for (ExplorationRule rule : rules)
      {
        boolean done = rule.apply(current, processor);
        if (done)
          break ruleApplication;
      }
    }
  }
  

//  
//  public static class Pointer<K>
//  {
//    does not work: eg if two list point to the same object!
//    
//    // first: the object holding the pointer
//    // second: either a Field, Integer (for arrays), or some other key in fixed collection
//    private final Pair<IdentityHashCodeEqual,K> coordinates;
//    
//    public Pointer(Object referent, K key)
//    {
//      coordinates = Pair.of(new IdentityHashCodeEqual(referent), key);
//    }
//
//    @Override
//    public int hashCode()
//    {
//      return coordinates.hashCode();
//    }
//
//    @Override
//    public boolean equals(Object obj)
//    {
//      if (getClass() != obj.getClass())
//        return false;
//      @SuppressWarnings("unchecked")
//      Pointer<K> other = (Pointer<K> ) obj;
//      return this.coordinates.equals(other.coordinates);
//    }
//    
//    
//  }
//  
//  
//  private static class IdentityHashCodeEqual
//  {
//    private final Object contents;
//    
//    private IdentityHashCodeEqual(Object contents)
//    {
//      this.contents = contents;
//    }
//
//    @Override
//    public int hashCode()
//    {
//      return System.identityHashCode(contents);
//    }
//
//    @Override
//    public boolean equals(Object obj)
//    {
//      if (!(obj instanceof IdentityHashCodeEqual))
//        return false;
//      return ((IdentityHashCodeEqual) obj).contents == this.contents;
//    }
//  }
//  
//  
//////  private final Set<Pointer> accessible
////  
////  private final DirectedGraph<? extends Pointer, AccessEdge> graph;
////  
////  TODO: need to decide if primitives are included in graph? useful for marking stuff as observed? seems true for array, but not for fields??
//// 
//  
//  
//  public static interface ExplorationRule<K>
//  {
//    public boolean apply(Object currentObject, EdgeProcessor<K> processor);
//  }
//  
//  public static interface EdgeProcessor<K>
//  {
//    public void processPrimitive(K key);
//    public void processObject(K key, Object child);
//  }
//
////  public static class EdgeProcessor<K>
////  {
////    public void processPrimitive(K key)
////    {
////      throw new RuntimeException();
////    }
////    
////    public void processObject(K key, Object child)
////    {
////      throw new RuntimeException();
////    }
////  }
//  
//  private final ArrayList<ExplorationRule<?>> rules;
//  
//  private void explore(Object root)
//  {
//    final LinkedList<? super Object> remainingToExplore = new LinkedList<>();
//    remainingToExplore.add(root);
//    
//    String [] test = new double[3];
//    Object o = test;
//    
//    
//    EdgeProcessor processor = new EdgeProcessor<K>() {
//
//      @Override
//      public void processPrimitive(blang.introspect.K key)
//      {
//        // TODO Auto-generated method stub
//        
//      }
//
//      @Override
//      public void processObject(blang.introspect.K key, Object child)
//      {
//        // TODO Auto-generated method stub
//        
//      }
//    };
//    
//    Set<? super Object> explored = Collections.newSetFromMap(new IdentityHashMap<>());
//    
//    while (!remainingToExplore.isEmpty())
//    {
//      Object toExplore = remainingToExplore.poll();
//      
//      for (ExplorationRule<?> rule : rules)
//      {
//        boolean applies = apply(toExplore, )
//      }
//    }
//  }
//  
//  
////  
////  public static class AccessEdge
////  {
////    // TODO: in, out, field?
////  }
//  
////  public static interface Pointer
////  {
////    public int hashCode();
////    public boolean equals(Object obj);
////  }
////  
////  public static class ElementPointer<K> implements Pointer
////  {
////    private final ObjectPointer container;
////    private final K key;
////    
////    public ElementPointer(ObjectPointer container, K key)
////    {
////      if (container == null)
////        throw new RuntimeException();
////      this.container = container;
////      this.key = key;
////    }
////
////    @Override
////    public int hashCode()
////    {
////      int result = container.hashCode();
////      result = 29 * result + (key != null ? key.hashCode() : 0);
////      return result;
////    }
////    
////    @Override
////    public boolean equals(Object obj)
////    {
////      if (this == obj)
////        return true;
////      if (!(obj instanceof ElementPointer))
////        return false;
////
////      @SuppressWarnings("unchecked")
////      final ElementPointer<K> otherPointer = (ElementPointer<K>) obj;
////
////      if (!this.container.equals(otherPointer.container))
////        return false;
////      if (this.key != null ? !this.key.equals(otherPointer.key) : otherPointer.key != null)
////        return false;
////
////      return true;
////    }
////  }
////  
////  public static class ObjectPointer implements Pointer
////  {
////    private final Object contents;
////    
////    public ObjectPointer(Object contents)
////    {
////      this.contents = contents;
////    }
////
////    @Override
////    public int hashCode()
////    {
////      return System.identityHashCode(contents);
////    }
////
////    @Override
////    public boolean equals(Object obj)
////    {
////      if (!(obj instanceof ObjectPointer))
////        return false;
////      return ((ObjectPointer) obj).contents == this.contents;
////    }
////  }
}


