package bayonet.math;



public final class CoordinatePacker
{
  private final int sizes[];
  public final int max; // i.e. number of packed integers
  public int size() { return max; }
  public CoordinatePacker(int[] sizes)
  {
    this.sizes = sizes;
    int prod = 1;
    for (int size : sizes)
      prod *= size;
    max = prod;
  }
  public int[] int2coord(int integer) 
  {
    if (integer <0 || integer >= max)
      throw new RuntimeException();
    int [] result = new int[sizes.length];
    for (int d = 0; d < result.length; d++)
    {
      result[d] = integer % sizes[d];
      integer = integer / sizes[d];
    }
    return result;
  }
  public int coord2int(int ... coord)
  { 
    if (coord.length != sizes.length)
      throw new RuntimeException();
    int result = 0;
    int cPow = 1;
    for (int d = 0; d < sizes.length; d++)
    {
      if (coord[d] < 0 || coord[d] >= sizes[d])
        throw new RuntimeException();
      result += cPow * coord[d];
      cPow *= sizes[d];
    }
    return result;
  }
}
