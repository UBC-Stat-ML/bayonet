package bayonet.math;


import org.ejml.simple.SimpleMatrix;

import briefj.BriefIO;



public class EJMLUtils
{
  /**
   * TODO: rename to l2NormSquared
   * TODO: check EJML does not already include that
   * @param vector
   * @return
   */
  public static double norm(SimpleMatrix vector)
  {
    if (vector.numCols() > 1) throw new RuntimeException();
    return vector.transpose().mult(vector).get(0,0);
  }
  
  
  public static String toString(SimpleMatrix matrix)
  {
    BriefIO.ensureUSLocale();
    return matrix.toString().replaceFirst("^.*\\n", "");
  }
  
  public static void main(String[] args)
  {
    SimpleMatrix test = new SimpleMatrix(2,2);
    System.out.println(toString(test));
  }
}
