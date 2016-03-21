package blang.accessibility;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;

import bayonet.graphs.GraphUtils;
import briefj.BriefIO;



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
   * We assume all implementation provide appropriate hashCode and equal, in particular, by-passing custom hashCode and
   * equals of enclosed objects.
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
   * Constituent nodes always have at most one outgoing edge (zero in the case of primitives), and object nodes can have zero, one, or 
   * more constituents. 
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
   *  use AccessibilityGraph.infer(..)
   */
  private AccessibilityGraph()
  {
  }
  
  public static AccessibilityGraph inferGraph(Object root)
  {
    return inferGraph(root, ExplorationRules.defaultExplorationRules);
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
    PrintWriter output = BriefIO.output(f);
    
    final VertexNameProvider<Node> nameProvider = new VertexNameProvider<Node>() {

      @Override
      public String getVertexName(Node vertex)
      {
        if (vertex instanceof ObjectNode)
        {
          Object object = ((ObjectNode) vertex).object;
          return "" + object.getClass().getName() + "@" + System.identityHashCode(object);
        }
        else if (vertex instanceof FieldConstituentNode)
          return "" + ((FieldConstituentNode) vertex).key.getName();
        else if (vertex instanceof ConstituentNode<?>)
          return "" + ((ConstituentNode<?>) vertex).key;
        else
          throw new RuntimeException();
      }
    };
    
    ComponentAttributeProvider<Node> attributeProvider = new ComponentAttributeProvider<Node>() {

      @Override
      public Map<String, String> getComponentAttributes(Node component)
      {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("shape", "box");
        if (component instanceof ConstituentNode)
        { 
          ConstituentNode<?> constituentNode = (ConstituentNode<?>) component;
          result.put("style", "dotted");
          if (constituentNode.isMutable())
          {
            result.put("color", "red");
            result.put("fontcolor", "red");
          }
        }
        return result;
      }
    };
    
    DOTExporter<Node,Pair<Node,Node>> exporter = new DOTExporter<>(
        new IntegerNameProvider<>(),
        nameProvider,
        null,
        attributeProvider,
        null
        );
    exporter.export(output, graph);
    output.close();
  }
}

