package bayonet.mcmc;

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
   * Whether this argument is part of the variable being
   * defined.
   * 
   * In other words, if X | Y ~ f(Y),
   * then both X and Y will be arguments of f, but only
   * X is being defined by f
   * 
   * @return
   */
  public boolean makeStochastic() default false; 
  // TODO: add observed static fct/fct in model base class : Factor -> Factor
  // @Option RealVariable observed;
  // Exponential likelihood = observed(Exponential.on(observed));
  // Gamma prior = Gamma.on(likelihood.realization);
}
