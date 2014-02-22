package bayonet.mcmc;

import bayonet.mcmc.moves.RealVariableMHProposal;



/**
 * A simple real variable that can be modified in place. 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
@RandomVariable(mcmcMoves = {RealVariableMHProposal.class})
public class RealVariable 
{
  private double value;
  
  public static RealVariable real(double value) 
  {
    return new RealVariable(value);
  }
  
  private RealVariable(double value)
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
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(value);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RealVariable other = (RealVariable) obj;
    if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
      return false;
    return true;
  }

  @Override
  public String toString()
  {
    return super.toString() + "[value=" + value + "]";
  }
  
}