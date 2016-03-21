package blang.accessibility;

import blang.accessibility.ExplorationRules.ArrayView;

import com.google.common.collect.ImmutableList;



public final class DoubleArrayView extends ArrayView
{
  @ViewedArray
  private final double[] viewedArray;
  
  public DoubleArrayView(ImmutableList<Integer> viewedIndices, double[] viewedArray)
  {
    super(viewedIndices);
    this.viewedArray = viewedArray;
  }

  public double get(int indexIndex)
  {
    return viewedArray[viewedIndices.get(indexIndex)];
  }
  
  public void set(int indexIndex, double object)
  {
    viewedArray[viewedIndices.get(indexIndex)] = object;
  }
}