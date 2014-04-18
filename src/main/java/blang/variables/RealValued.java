package blang.variables;


/**
 * A read-only view to real variable or real-valued transformations of 
 * variables.
 * 
 * Used for example for the CheckStationarity framework, which assumes
 * real statistics in order to apply classical statistical tests.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface RealValued
{
  public double getValue();
}