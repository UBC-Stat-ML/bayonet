package blang.prototype;

import bayonet.math.CoordinatePacker;
import blang.accessibility.IntArrayView;

import com.google.common.collect.ImmutableList;



public class IntMatrix
{
  public final int[] data;
  private final CoordinatePacker packer; // todo: replace by more efficient stuff
  
  public IntMatrix(int rows, int cols)
  {
    this.data = new int[rows*cols];
    this.packer = new CoordinatePacker(new int[]{rows, cols});
  }
  
  private Int [] cache = null;
  
  public Int entry(final int entryIndex)
  {
    if (cache == null)
      cache = new Int[packer.size()];
    if (cache[entryIndex] != null)
      return cache[entryIndex];
    final IntArrayView view = new IntArrayView(ImmutableList.of(entryIndex), data);
    return cache[entryIndex] = new IntEntry(view);
  }

  public Int entry(int i, int j)
  {
    final int entryIndex = packer.coord2int(i,j);
    return entry(entryIndex);
  }
  
  static class IntEntry implements Int
  {
    final IntArrayView view;
    public IntEntry(IntArrayView view)
    {
      this.view = view;
    }
    @Override
    public int get()
    {
      return view.get(0);
    }
    @Override
    public void set(int value)
    {
      view.set(0, value);
    }
  }
}
