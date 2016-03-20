package blang.accessibility;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;

import bayonet.graphs.GraphUtils;
import briefj.ReflexionUtils;



/**
 * An accessibility graph provides the low level data structure used to construct factor graph automatically. 
 * 
 * An accessibility graph is 
 * a directed graph that keeps track of (a superset of) accessibility relationships between java objects. This is used to answer 
 * questions such as: does factor f1 and f2 both have access to an object with a mutable fields which 
 * is unobserved? 
 * 
 * Note: 
 *  - accessibility via global (static) fields is assumed not to be used throughout this inference.
 *  - visibility (public/private/protected) is ignored when establishing accessibility. To understand why, 
 *    say object o1 has access to o2, which has a private field to o3. Even though o1 may not have direct access to o3,
 *    o2 may have a public method giving effective access to it. Hence, we do want to have a path from o1 to o3 (via 
 *    edges (o1, o2), (o2, o3) indicating that there may be accessibility from o1 to o3.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class AccessibilityGraph
{
  /**
   * A node (factor, variable, or component) in the accessibility graph.
   * 
   * Each such node is associated to a unique address in memory.
   * To be more precise, this association is established in one of two ways:
   * 
   * 1. a reference to an object o (with hashCode and equals based on o's identity instead of o's potentially overloaded hashCode and equal)
   * 2. a reference to a container c (e.g., an array, or List) as well as a key k (in this case, hashCode and equal are based on a 
   *    combination of the identity of c, and the standard hashCode and equal of k)
   *    
   * Case (1) is called an object node, and case (2) is called a constituent node. 
   * 
   * An important special case of a constituent node: the container c being a regular object, and the key k being a Field of c's class
   * 
   * Constituent nodes are needed for example to obtain slices of 
   * a matrix, partially observed arrays, etc. 
   * 
   * 
   * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
   *
   */
  public static interface Node
  {
  }
  
  /**
   * There is no edge in the graph between node n1 and n2 if a method having access to only n1 cannot have
   * access to n2 (unless it uses static fields).
   * 
   * Note that the semantics of this relation is described using the negative voice since we can only hope to 
   * get a super-set of accessibility relationships. This is similar to graphical models, where only the 
   * absence of edge is informative strictly speaking.. 
   */
  public final DirectedGraph<Node, Pair<Node, Node>> graph = GraphUtils.newDirectedGraph();
  
  /**
   * Note that in the graph, object nodes points to constituent nodes only; and constituent nodes points to object nodes only. 
   * Also, (under the default rules, defined below) constituent nodes have exactly one incoming edge (but this can change to more than one 
   * with custom rules?), while object nodes can always have zero (for the root), one, or more. 
   * Constituent nodes always have at most one outgoing edge (zero in the case of primitives), and object nodes can have zero, one, or 
   * more constituents. TODO: use these properties to create test cases.
   */
  private <K> void addEdgeAndVertices(ObjectNode objectNode, ConstituentNode<K> constituentNode)
  {
    _addEdgeAndVertices(objectNode, constituentNode);
  }
  
  /**
   * See addEdgeAndVertices(ObjectNode objectNode, ConstituentNode<K> constituentNode)
   */
  private <K> void addEdgeAndVertices(ConstituentNode<K> constituentNode, ObjectNode objectNode)
  {
    _addEdgeAndVertices(constituentNode, objectNode);
  }
  
  private void _addEdgeAndVertices(Node source, Node destination)
  {
    graph.addVertex(source);
    graph.addVertex(destination);
    graph.addEdge(source, destination);
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
  
  public static abstract class ConstituentNode<K> implements Node
  {
    /**
     * 
     * @return null if a primitive, the object referred to otherwise
     */
    public abstract Object resolve();
    
    public boolean resolvesToObject()
    {
      return resolve() != null;
    }
    
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
    public String toString()
    {
      return "ConstituentNode[containerClass=" + container.getClass() + ",containerObjectId=" + System.identityHashCode(container) + ",key=" + key + "]";
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
  
  public static class ListConstituentNode extends ConstituentNode<Integer>
  {
    public ListConstituentNode(Object container, Integer key)
    {
      super(container, key);
    }

    @Override
    public Object resolve()
    {
      @SuppressWarnings("rawtypes")
      List list = (List) container;
      return list.get(key);
    }
  }
  
  public static interface ExplorationRule
  {
    /**
     * return null if the rule does not apply to this object, else, a list of constituents to recurse to 
     */
    public List<? extends ConstituentNode<?>> explore(Object object);
  }
  
  private static List<ExplorationRule> defaultExplorationRules = Arrays.asList(
      AccessibilityGraph::listExplorationRule,
      AccessibilityGraph::arrayExplorationRule,
      AccessibilityGraph::basicImmutableJavaObjectsRule,
      AccessibilityGraph::standardObjectExplorationRule);
  
  
  public static List<ArrayConstituentNode> arrayExplorationRule(Object object)
  {
    Class<? extends Object> c = object.getClass();
    if (!c.isArray())
      return null;
    ArrayList<ArrayConstituentNode> result = new ArrayList<>();
    final int length = Array.getLength(object);
    for (int i = 0; i < length; i++)
      result.add(new ArrayConstituentNode(object, i));
    return result;
  }
  
  public static List<ListConstituentNode> listExplorationRule(Object object)
  {
    if (!(object instanceof List))
      return null;
    @SuppressWarnings({ "unchecked" })
    List<? extends Object> list = (List<? extends Object>) object;
    ArrayList<ListConstituentNode> result = new ArrayList<>();
    for (int i = 0; i < list.size(); i++)
      result.add(new ListConstituentNode(object, i));
    return result;
  }
  
  public static List<? extends ConstituentNode<?>> basicImmutableJavaObjectsRule(Object object)
  {
    if (object instanceof String || object instanceof Number)
      return Collections.emptyList();
    else
      return null;
  }
  
  // standard object as in not an array object
  public static List<FieldConstituentNode> standardObjectExplorationRule(Object object)
  {
    ArrayList<FieldConstituentNode> result = new ArrayList<>();
    
    // process all enclosing classes, if any
    Object outerObject = ReflexionUtils.getOuterClass(object);
    if (outerObject != null)
      result.addAll(standardObjectExplorationRule(outerObject));
  
    // find all fields (including those of super class(es), recursively, if any
    for (Field f : ReflexionUtils.getDeclaredFields(object.getClass(), true))
      result.add(new FieldConstituentNode(object, f));
    
    return result;
  }
  
  // TODO: interface+annotation-based views
  
  public static AccessibilityGraph inferGraph(Object root)
  {
    return inferGraph(root, defaultExplorationRules);
  }
  
  public static AccessibilityGraph inferGraph(Object root, List<ExplorationRule> explorationRules)
  {
    AccessibilityGraph result = new AccessibilityGraph();
    
    final LinkedList<? super Object> toExploreQueue = new LinkedList<>();
    toExploreQueue.add(root);
    
    final HashSet<ObjectNode> explored = new HashSet<>();
    
    while (!toExploreQueue.isEmpty())
    {
      ObjectNode current = new ObjectNode(toExploreQueue.poll());

      explored.add(current);
      result.graph.addVertex(current);
      
      List<? extends ConstituentNode<?>> constituents = null;
      ruleApplication : for (ExplorationRule rule : explorationRules)
      {
        constituents = rule.explore(current.object);
        if (constituents != null)
          break ruleApplication;
      }
      if (constituents == null)
        throw new RuntimeException("No rule found to apply to object " + current.object);
      
      for (ConstituentNode<?> constituent : constituents)
      {
        result.addEdgeAndVertices(current, constituent);
        if (constituent.resolvesToObject()) 
        {
          Object nextObject = constituent.resolve();
          ObjectNode next = new ObjectNode(nextObject);
          result.addEdgeAndVertices(constituent, next);
          if (!explored.contains(next))
            toExploreQueue.add(nextObject);
        }
      } // of constituent loop
    } // of exploration
    
    return result;
  }
  
  public void toDotFile(File f)
  {
    GraphUtils.toDotFile(graph, f);
  }
}

