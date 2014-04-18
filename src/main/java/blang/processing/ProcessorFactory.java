package blang.processing;

import java.util.List;

import blang.ProbabilityModel;


/**
 * Low level interface for creating processors.
 * 
 * Use for example if you want to create processors that process 
 * more than one node simultaneously. 
 * 
 * In most cases however, one typically want to process a single
 * node at a time, in which case NodeProcessor is easier to 
 * work with.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface ProcessorFactory
{
  /**
   * Instantiate one or several processors for a given probability
   * models.
   * 
   * @param model
   * @return
   */
  public List<? extends Processor> build(ProbabilityModel model) ;
}
