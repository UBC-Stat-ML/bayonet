package bayonet.math;


import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.EigenDecomposition;
import org.ejml.ops.EigenOps;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.simple.SimpleMatrix;

import briefj.BriefIO;
import briefj.BriefStrings;


/**
 * 
 * Utilities around functionalities in the Efficient Java Matrix Library.
 * 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class EJMLUtils
{
  /**
   * Is the max point-wise difference of the entries in the two matrices 
   * below the provided threshold?
   * 
   * @param m1
   * @param m2
   * @param threshold
   * @return false if one of the entry deviates by more than threshold, true otherwise
   */
  public static boolean isClose(SimpleMatrix m1, SimpleMatrix m2, double threshold)
  {
    int numCols = m1.numCols();
    int numRows = m1.numRows();
    if (m2.numCols() != numCols || m2.numRows() != numRows)
      throw new RuntimeException();
    for (int r = 0; r < numRows; r++)
      for (int c = 0; c < numCols; c++)
        if (!NumericalUtils.isClose(m1.get(r, c), m2.get(r, c), threshold))
          return false;
    return true;
  }
  
  /**
   * 
   * @throws RuntimeException If the two matrices do not satisfy isClose()
   * @param m1
   * @param m2
   * @param threshold
   */
  public static void checkIsClose(SimpleMatrix m1, SimpleMatrix m2, double threshold)
  {
    if (!isClose(m1, m2, threshold))
      throw new RuntimeException("One of the entries of these two matrices differs by " +
      		"more than " + threshold + "\nMatrix1:\n" + toString(m1) + "\nMatrix2:\n" + toString(m2));
  }
  
  /**
   * Uses the default threshold in NumericalUtils
   * @param m1
   * @param m2
   */
  public static void checkIsClose(SimpleMatrix m1, SimpleMatrix m2)
  {
    checkIsClose(m1, m2, NumericalUtils.THRESHOLD);
  }
  
  /**
   * A convenient access to EJML's eigen-decomposition functionality.
   * Note that using the lower-level functions directly is more efficient if many eigen-decompositions
   * need to be performed.
   * 
   * Also checks that V D V^{-1} is indeed equal to the original matrix up
   * to numerical error (1e-8).
   * 
   * @param matrix
   * @return
   */
  public static SimpleEigenDecomposition simpleEigenDecomposition(SimpleMatrix matrix)
  {
    EigenDecomposition<DenseMatrix64F> eigenDecomp = DecompositionFactory.eig(matrix.numCols(), true);
    if (!eigenDecomp.decompose(matrix.getMatrix()))
      throw new RuntimeException();
    SimpleMatrix D = new SimpleMatrix(EigenOps.createMatrixD(eigenDecomp));
    SimpleMatrix V = new SimpleMatrix(EigenOps.createMatrixV(eigenDecomp));
    SimpleMatrix Vinverse = V.invert();
    test(matrix,V,D,Vinverse, NumericalUtils.THRESHOLD);
    return new SimpleEigenDecomposition(V, D, Vinverse);
  }
  
  /**
   * If Q is the matrix represented in the given decomposition, returns exp(scalar * Q),
   * where exp(.) is the matrix exponential.
   * @param decomposition
   * @param scalar
   * @return
   */
  public static SimpleMatrix matrixExponential(SimpleEigenDecomposition decomposition, double scalar)
  {
    final int size = decomposition.getMatrixSize();
    SimpleMatrix expD = new SimpleMatrix(size, size);
    for (int i = 0; i < size; i++)
      expD.set(i, i, Math.exp(scalar * decomposition.getD().get(i,i)));
    return multiply(decomposition.getV(), expD, decomposition.getVinverse());
  }
  
  private static void test(SimpleMatrix matrix, SimpleMatrix v, SimpleMatrix d,
      SimpleMatrix vinverse, double threshold)
  {
    EjmlUnitTests.assertEquals(matrix.getMatrix(), multiply(v,d,vinverse).getMatrix(), threshold);
  }

  /**
   * Returns the product of the three matrices.
   * 
   * TODO: in the future, this could be made more efficient by a factor of 2. 
   * Currently use the naive method.
   * 
   * @param dense1 A dense matrix
   * @param diag A diagonal matrix
   * @param dense2 Another dense matrix
   * @return
   */
  public static SimpleMatrix multiply(SimpleMatrix dense1, SimpleMatrix diag, SimpleMatrix dense2)
  {
    return dense1.mult(diag).mult(dense2);
  }
  
  /**
   * Holds the result of a real valued eigen-decomposition,
   * 
   * M = V * D * Vinverse
   * 
   * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
   *
   */
  public static class SimpleEigenDecomposition
  {
    private final SimpleMatrix V, D, Vinverse;

    private SimpleEigenDecomposition(SimpleMatrix v, SimpleMatrix d,
        SimpleMatrix vinverse)
    {
      V = v;
      D = d;
      Vinverse = vinverse;
    }

    public int getMatrixSize()
    {
      return D.numCols();
    }

    public SimpleMatrix getV()
    {
      return V;
    }

    public SimpleMatrix getD()
    {
      return D;
    }

    public SimpleMatrix getVinverse()
    {
      return Vinverse;
    }

    @Override
    public String toString()
    {
      return "SimpleEigenDecomposition:\n" +
        BriefStrings.indent(
          "V:\n" + BriefStrings.indent(EJMLUtils.toString(V)) +
          "\nD:\n" + BriefStrings.indent(EJMLUtils.toString(D)) +
          "\nVinverse:\n" + BriefStrings.indent(EJMLUtils.toString(Vinverse)));
    }
  }
  
  /**
   * Unpacks the provided matrix into an array of arrays.
   * 
   * Creates a fresh copy.
   * @param matrix
   * @return
   */
  public static double[][] copyMatrixToArray(SimpleMatrix matrix)
  {
    double [][] result = new double[matrix.numRows()][matrix.numCols()];
    for (int row = 0; row < matrix.numRows(); row++)
      for (int col = 0; col < matrix.numCols(); col++)
        result[row][col] = matrix.get(row, col);
    return result;
  }
  
  /**
   * @param vector
   * @return
   */
  public static double vectorL2NormSquared(SimpleMatrix vector)
  {
    if (vector.numCols() > 1) throw new RuntimeException();
    return vector.transpose().mult(vector).get(0,0);
  }
  
  /**
   * Format a matrix, making sure the locale used is US.
   * @param matrix
   * @return
   */
  public static String toString(SimpleMatrix matrix)
  {
    BriefIO.ensureUSLocale();
    return matrix.toString().replaceFirst("^.*\\n", "").replaceAll("\\n$", "");
  }
}
