package blang.variables;

import blang.annotations.Processors;
import blang.annotations.Samplers;
import blang.mcmc.IntegerVariableMHProposal;

/**
 * 
 * @author Sean Jewell (jewellsean@gmail.com)
 *
 */
@Samplers({IntegerVariableMHProposal.class})
@Processors({IntegerVariableProcessor.class})
public class IntegerVariable implements IntegerValued, RealValued
{

  
  private int value; 
  
  public IntegerVariable(int value)
  {
    this.value = value;
  }

  /**
   * 
   * @return An integer variable initialized at 0
   */
  public static IntegerVariable intVar()
  {
    return intVar(0);
  }
  
  /**
   * 
   * @param value
   * @return an integer variable initialized at value
   */
  public static IntegerVariable intVar(int value)
  {
    return new IntegerVariable(value);
  }
  
  
  
  
  @Override
  public int getIntegerValue()
  {
    return value;
  }

  /**
   * 
   * @param value
   */
  public void setValue(int value)
  {
    this.value = value;
  }
  
  
  @Override
  public String toString()
  {
    return "int(" + value + ")";
  }

  @Override
  public double getValue()
  {
    return getIntegerValue();
  }
  
}
