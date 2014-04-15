package blang.variables;

import blang.annotations.Processors;
import blang.annotations.Samplers;
import blang.mcmc.RealVariablePeskunTypeMove;



/**
 * A simple real variable that can be modified in place. 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 */
@Samplers({RealVariablePeskunTypeMove.class})
@Processors({RealVariableProcessor.class})
public class RealVariable implements RealValued
{
  /**
   * 
   */
  private double value;
  
  /**
   * 
   * @return A real initialized at zero.
   */
  public static RealVariable real()
  {
    return real(0);
  }
  
  /**
   * 
   * @param value
   * @return
   */
  public static RealVariable real(double value) 
  {
    return new RealVariable(value);
  }
  
  /**
   * 
   * @param value
   */
  public RealVariable(double value)
  {
    this.value = value;
  }

  /**
   * 
   * @param newValue
   */
  public void setValue(double newValue)
  {
    this.value = newValue;
  }
  
  /**
   * 
   * @return
   */
  public double getValue()
  {
    return value;
  }

  @Override
  public String toString()
  {
    return "real(" + value + ")";
  }
  
}