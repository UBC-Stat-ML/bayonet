package bayonet.math;


import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.EigenDecomposition;
import org.ejml.ops.EigenOps;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.MatrixVisualization;
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
   * A convenient access to EJML's eigen decomposition functionality.
   * Note that using the matrix directly is more efficient if many eigendecompositions
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
   * Holds the result of an eigendecomposition,
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
  
  public static void main(String [] args)
  {
    double [][] data = new double[][]{{-1,1},{2,-2}};
    SimpleMatrix test = new SimpleMatrix(data);
    double t = 0.01;
    SimpleEigenDecomposition rateDecomp = simpleEigenDecomposition(test);
    SimpleMatrix p = matrixExponential(rateDecomp,t);
    System.out.println(toString(p));
    System.out.println(simpleEigenDecomposition(p.transpose()));
    System.out.println(toString(matrixExponential(rateDecomp, 100)));
//    MatrixVisualization.show(test.getMatrix(), "test");
  }

}
