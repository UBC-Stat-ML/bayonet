package bayonet.math;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;



public class JBlasUtils
{

  public static DoubleMatrix inversePositiveMatrix(DoubleMatrix covar)
  {
    return Solve.solvePositive(covar, DoubleMatrix.eye(covar.rows));
  }

  public static double [][] asDoubleArray(DoubleMatrix matrix)
  {
    double [][] result = new double[matrix.rows][matrix.columns];
    for (int r = 0; r < matrix.rows; r++)
      for (int c = 0; c < matrix.columns; c++)
        result[r][c] = matrix.get(r,c);
    return result;
  }
}
