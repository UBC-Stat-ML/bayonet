package blang.variables;



public interface RealVectorInterface
{
  /**
   * 
   * @return The values of the vector. Note that the user
   *         should call setVector after modifying the values, 
   *         otherwise the behavior is not well defined.
   */
  public double [] getVector();
  public void setVector(double [] values);
}
