package bayonet.smc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * Walks through a permutation of a given size s, and after providing
 * s permuted indices, reshuffle the permutation and keep walking on 
 * the newly permuted indices, and so on.
 *  
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class PermutationStream
{
  private int nCalls = 0;
  private final List<Integer> indices;
  private final Random random;
  private final int size;
  
  public PermutationStream(int size, Random random)
  {
    this.random = random;
    this.indices = new ArrayList<>();
    this.size = size;
    for (int i = 0; i < size(); i++)
      indices.add(i);
  }
  
  public int popIndex()
  {
    final int index = nCalls++ % size();
    if (index == 0)
      reshuffle();
    return indices.get(index);
  }
  
  private void reshuffle()
  {
    Collections.shuffle(indices, random);
  }

  public final int size()
  {
    return size;
  }

  public int nCalls()
  {
    return nCalls;
  }
}
