package blang.introspect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;

import bayonet.graphs.GraphUtils;
import briefj.ReflexionUtils;



/**
 * An accessibility provides the low level data structure used to construct the factor graph. It is 
 * a directed graph that keeps track of accessibility relationships between java objects. This is used to answer 
 * questions such as: does factor f1 and f2 both have access to an object with mutable fields which 
 * is unobserved? Note that accessibility via global (static) fields is ignored.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class Accessibility
{
  /**
   * A node (factor, variable, or component) in the accessibility graph.
   * 
   * Each such node is associated to an address in memory.
   * 
   * This association could be done in one of two ways:
   * 
   * 1. a reference to an object o (with hashCode and equals based on o's identity instead of potential overloaded hashCode and equal)
   * 2. a reference to a container c (e.g., an array, or List) as well as a key k (in this case, hashCode and equal are based on a 
   *    combination of the identity of c, and the standard hashCode and equal of k)
   *    
   * Case (1) is called an object node, and case (2) is called a constituent node. 
   * 
   * An important special case of a constituent node: the container c being any class, and the key k being a Field
   * 
   * Constituent nodes are needed for example to obtain slices of 
   * a matrix, partially observed arrays, etc. 
   * 
   * Note that in the graph, object nodes points to constituent nodes only; and constituent nodes points to object nodes only. 
   * Also, constituent nodes have exactly one incoming edge, while object nodes can have zero (for the root), one, or more. 
   * Constituent nodes have at most one outgoing edge (zero in the case of primitives), and object nodes can have zero, one, or 
   * more constituents. TODO: use these properties to create test cases.
   * 
   * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
   *
   */
  public static interface Node
  {
  }
  
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
  
  public static class ObjectNode implements Node
  {
    private final Object object;
    
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
    public boolean equals(Object obj)
    {
      if (this == obj)
        return true;
      if (!(obj instanceof ObjectNode))
        return false;
      return ((ObjectNode) obj).object == this.object;
    }
  }
  
  public static abstract class ConstituentNode<K> implements Node
  {
    /**
     * 
     * @return null if a primitive, the object referred to otherwise
     */
    public abstract Object resolve();
    
    protected final Object container;
    protected final K key;
    
    public ConstituentNode(Object container, K key)
    {
      if (container == null)
        throw new RuntimeException();
      this.container = container;
      this.key = key;
    }
    
    @Override
    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((container == null) ? 0 : 
            // IMPORTANT distinction from automatically generated hashCode():
            // use identity hash code for the container (but not the key),
            // as e.g. large integer keys will not point to the same address
            System.identityHashCode(container));
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      return result;
    }
    @Override
    public boolean equals(Object obj)
    {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      @SuppressWarnings("rawtypes")
      ConstituentNode other = (ConstituentNode) obj;
      if (container == null)
      {
        if (other.container != null)
          return false;
      } else if (
          // IMPORTANT: see similar comment in hashCode()
          container != other.container)
          //!container.equals(other.container))
        return false;
      if (key == null)
      {
        if (other.key != null)
          return false;
      } else if (!key.equals(other.key))
        return false;
      return true;
    }
  }
  
  public static class FieldConstituentNode extends ConstituentNode<Field> 
  {
    public FieldConstituentNode(Object container, Field key)
    {
      super(container, key);
    }

    @Override
    public Object resolve()
    {
      if (key.getType().isPrimitive())
        return null;
      return ReflexionUtils.getFieldValue(key, container);
    }
  }
  
  public static class ArrayConstituentNode extends ConstituentNode<Integer>
  {
    public ArrayConstituentNode(Object container, Integer key)
    {
      super(container, key);
    }

    @Override
    public Object resolve()
    {
      if (container.getClass().getComponentType().isPrimitive())
        return null;
      Object [] array = (Object[]) container;
      return array[key];
    }
    
  }
  
  public static interface ExplorationRule
  {
    /**
     * return null if the rule does not apply to this object, else, a list of constituents to recurse to 
     */
    public List<? extends ConstituentNode<?>> explore(Object object);
  }
  
  public final List<ExplorationRule> defaultExplorationRules = Arrays.asList(Accessibility::arrayExplorationRule);
  
  public static List<? extends ConstituentNode<?>> arrayExplorationRule(Object object)
  {
    Class<? extends Object> c = object.getClass();
    if (!c.isArray())
      return null;
    ArrayList<ArrayConstituentNode> result = new ArrayList<>();
    Object [] array = (Object []) object;
    for (int i = 0; i < array.length; i++)
      result.add(new ArrayConstituentNode(object, i));
    return result;
  }
  
  // TODO: defaultExplorationRule, which looks into all fields
  
  public static DirectedGraph<Node, Pair<Node, Node>> buildAccessibilityGraph(Object root, List<ExplorationRule> rules)
  {
    // vertex set doubles as a set of explored nodes
    DirectedGraph<Node, Pair<Node, Node>> result = GraphUtils.newDirectedGraph();
    
    final LinkedList<? super Object> toExploreQueue = new LinkedList<>();
    toExploreQueue.add(root);
    
    while (!toExploreQueue.isEmpty())
    {
      ObjectNode current = new ObjectNode(toExploreQueue.poll());
      result.addVertex(current);
      
      List<? extends ConstituentNode<?>> constituents = null;
      ruleApplication : for (ExplorationRule rule : rules)
      {
        constituents = rule.explore(current.object);
        if (constituents != null)
          break ruleApplication;
      }
      if (constituents == null)
        throw new RuntimeException("No rule found to apply to object " + current.object);
      
      for (ConstituentNode<?> constituent : constituents)
      {
        result.addVertex(constituent);
        result.addEdge(current, constituent);
        Object nextObject = constituent.resolve();
        if (nextObject != null) // i.e., if this constituent does not refer to a primitive or to null
        {
          ObjectNode next = new ObjectNode(nextObject);
          if (!result.vertexSet().contains(next))
            toExploreQueue.add(nextObject);
        }
      }
    }
    
    return result;
  }
}

