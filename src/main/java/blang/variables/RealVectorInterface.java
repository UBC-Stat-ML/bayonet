package blang.variables;


/**
 * Interface for vectors of real numbers.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface RealVectorInterface
{
  /**
   * Get the array of values of the vector. This array should not 
   * be modified, UNLESS the array is passed to setVector()
   * right after (to signal certain implementations that changes
   * were applied).
   * 
   * @return The values of the vector. Note that the user
   *         should call setVector after modifying the values, 
   *         otherwise the behavior is not well defined.
   */
  public double [] getVector();
  
  /**
   * Change in place the values.
   * 
   * @param values
   */
  public void setVector(double [] values);
  
  /**
   * Get the dimension (length) of the vector
   * @return the length of the vector 
   */
  public int getDim();
  
  /**
   * Evaluate the vector using a test function, g of type TestFunction
   * @return g.eval(this)
   */
  public double evaluateTestFunction();
}
