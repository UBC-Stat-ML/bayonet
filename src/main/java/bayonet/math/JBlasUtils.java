package bayonet.math;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;



public class JBlasUtils
{

  public static DoubleMatrix inversePositiveMatrix(DoubleMatrix covar)
  {
    return Solve.solvePositive(covar, DoubleMatrix.eye(covar.rows));
  }

}
