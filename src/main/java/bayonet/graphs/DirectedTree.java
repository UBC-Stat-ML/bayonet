package bayonet.graphs;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * A discrete directed tree. 
 * The leaves of each node are viewed as unordered sets, for the 
 * purpose of equals and hashcode (but the
 * order is still deterministic since we are using LinkedHashSets).
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <N> Label for the nodes
 */
public final class DirectedTree<N> implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  private final Map<N, N> parentPointers = new LinkedHashMap<>();
  private final Map<N, Set<N>> children = new LinkedHashMap<>();
  private final N root;
  
  public DirectedTree(N root)
  {
    this.root = root;
    this.parentPointers.put(root, null);
    this.children.put(root, new LinkedHashSet<>());
  }
  
  /**
   * 
   * @param node
   * @return Children, or empty set for leaves, or throws if not a node
   */
  public Set<N> getChildren(N node)
  {
    checkNodeExists(node);
    return Collections.unmodifiableSet(children.get(node));
  }
  
  public boolean isLeaf(N node)
  {
    return children.get(node).isEmpty();
  }
  
  /**
   * 
   * @param node
   * @return Parent, or null for the root, or throws if not a node
   */
  public N getParent(N node)
  {
    checkNodeExists(node);
    return parentPointers.get(node);
  }
  
  public N getRoot()
  {
    return root;
  }
  
  public Set<N> getNodes()
  {
    return Collections.unmodifiableSet(children.keySet());
  }
  
  /**
   * Add a child to an existing node.
   * 
   * @param existingNode
   * @param newChild
   */
  public void addChild(N existingNode, N newChild)
  {
    checkNodeExists(existingNode);
    children.get(existingNode).add(newChild);
    children.put(newChild, new LinkedHashSet<>());
    parentPointers.put(newChild, existingNode);
  }

  private void checkNodeExists(N node)
  {
    if (!children.keySet().contains(node))
      throw new RuntimeException("Node not in tree:" + node);
  }

  @Override
  public String toString()
  {
    return "DirectedTree [children=" + children + ", root=" + root + "]";
  }

  /**
   * Note: enough to check the parent pointers.
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((parentPointers == null) ? 0 : parentPointers.hashCode());
    return result;
  }

  /**
   * Note: enough to check the parent pointers.
   */
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
    DirectedTree other = (DirectedTree) obj;
    if (parentPointers == null)
    {
      if (other.parentPointers != null)
        return false;
    } else if (!parentPointers.equals(other.parentPointers))
      return false;
    return true;
  }
}
