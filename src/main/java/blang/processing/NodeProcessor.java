package blang.processing;


/**
 * A processor that is responsible of collecting information for
 * a single node in a graph. 
 * 
 * The MCMCAlgorithm will create instances (via reflection)
 * of NodeVariable via the Processors annotation. Each instance of 
 * NodeProcessor is associated with a single node in a probability model.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <T>
 */
public interface NodeProcessor<T> extends Processor
{
  /**
   * MCMCAlgorithm will call setReference once, before starting the
   * sampling process, to provide the nodeprocessor with the instance
   * of the variable that it is in charge of monitoring. The instance
   * is responsible for maintaining this instance.
   * 
   * 
   * @param variable
   */
  public void setReference(T variable);
}
