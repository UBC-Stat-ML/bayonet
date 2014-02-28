package blang.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates a variable connected to a factor.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FactorArgument 
{
  /**
   * If X | Y ~ f(Y),
   * then both X and Y will be arguments of f, but only
   * X is made stochastic by f (Y could potentially be deterministic/
   * hyperparameter)
   * 
   * @return
   */
  public boolean makeStochastic() default false; 
}
