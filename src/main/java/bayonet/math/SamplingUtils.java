package bayonet.math;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;



public class SamplingUtils
{
  /**
   * Pick an element index uniformly at random. Constant time operation for list,
   * expected linear time for other types of collections.
   * 
   * @throws RuntimeException if the collection is a HashSet (because iteration 
   *   order is non-deterministic, making it hard to reproduce experiments)---use
   *   LinkedHashSet instead in these cases.
   * 
   * @param <T>
   * @param collection
   * @param rand
   * @return
   */
  public static <T> T uniformFromCollection(Random rand, Collection<T> collection)
  {
    if (collection.isEmpty()) return null;
    if (collection instanceof List)
    {
      List<T> list = (List<T>) collection;
      return list.get(rand.nextInt(list.size()));
    }
    else
    {
      if (collection instanceof HashSet)
        throw new RuntimeException("Use LinkedHashSet instead of HashSet");
      
      final int sampledIndex = rand.nextInt(collection.size());
      Iterator<T> iter = collection.iterator();
      for (int i = 0 ; i < sampledIndex; i++)
        iter.next();
      return iter.next();
    }
    
  }
}
