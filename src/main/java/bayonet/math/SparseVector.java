package bayonet.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SparseVector
{
  public int [] indices;
  public double [] values;
  /**
   * A zero vector of provided dim
   * @param dim
   */
  public SparseVector(final int dim)
  {
    indices = new int[0];
    values = new double[0];
  }

  public SparseVector(final List<Integer> inIndices, final List<Double> inValues)
  {
    assert inIndices.size() == inValues.size(); 
    int size = inIndices.size();
    this.indices = new int[size];
    this.values = new double[size];
    for (int i = 0; i < size; i++)
    {
      indices[i] = inIndices.get(i);
      values[i] = inValues.get(i);
    }
  }
  public SparseVector(int [] indices, double [] values)
  {
    if (indices.length != values.length)
      throw new RuntimeException();
    this.indices = indices;
    this.values = values;
  }
  
  public SparseVector(final double[] vector)
  {
    List<Integer> indicesList = new ArrayList<Integer>();
    List<Double> valuesList = new ArrayList<Double>();
    for (int i = 0; i < vector.length; i++)
    {
      if (vector[i] != 0.0)
      {
        indicesList.add(i);
        valuesList.add(vector[i]);
      }
    }
    indices = new int[valuesList.size()];
    values = new double[valuesList.size()];
    for (int j = 0; j < valuesList.size(); j++)
    {
      indices[j] = indicesList.get(j);
      values[j] = valuesList.get(j);
    }
  }
  
  public SparseVector(final HashMap<Integer, Double> map, final int dim)
  {
    List<Integer> indicesList = new ArrayList<Integer>();
    List<Double> valuesList = new ArrayList<Double>();

  	for (Integer key : map.keySet()) {
  		Double value = map.get(key);
  		indicesList.add(key);
  		valuesList.add(value);
  	}
    indices = new int[valuesList.size()];
    values = new double[valuesList.size()];
    for (int j = 0; j < valuesList.size(); j++)
    {
      indices[j] = indicesList.get(j);
      values[j] = valuesList.get(j);
    }

  }
  
  /**
   * Mutates argument vector
   * performs (in vector):
   * vector gets vector + coef * this
   * @param coef
   * @param vector
   */
  public void linearIncrement(double coef, double [] vector)
  {
    for (int i = 0; i < nEntries(); i++)
    {
      int index = indices[i];
      double value = values[i];
      vector[index] = vector[index] + coef * value;
    }
  }
  
  /**
   * The dot product of this with the provided vector
   * @param vector
   * @return
   */
  public double dotProduct(double [] vector)
  {
    double result = 0.0;
    for (int i = 0; i < nEntries(); i++)
    {
      int index = indices[i];
      double value = values[i];
      result = result + value * vector[index];
    }
    return result;
  }
  
  public double[] scale(double factor)
  {
	   double [] result = new double[nEntries()];
	   for ( int i = 0; i < nEntries(); i++)
	   {
		   double value = values[i];
		   result[i] = value * factor;
	   }
	return result;
  }
  private int nEntries()
  {
    return indices.length;
  }
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    for (int i = 0; i < nEntries(); i++)
    {
      builder.append(indices[i] + ":" + values[i]);
      if (i != nEntries() - 1) builder.append(" ");
    }
    builder.append("]");
    return builder.toString();
  }
}