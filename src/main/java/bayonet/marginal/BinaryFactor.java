package bayonet.marginal;


/**
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <V>
 */
public interface BinaryFactor<V>
{
  public V marginalizedNode();
  public V otherNode();

}
