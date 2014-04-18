package blang.processing;

import java.util.List;

import blang.ProbabilityModel;
import blang.annotations.Processors;
import blang.annotations.util.AnnotationBasedFactory;
import blang.annotations.util.AnnotationBasedFactory.Producer;
import briefj.ReflexionUtils;


/**
 * For each variable in a probability model, Processor annotations in the class of this 
 * variable will be used to instantiate NodeProcessors. This class
 * contains the internal mechanisms responsible for implementing this mapping.
 * 
 * Much of the work is done in the AnnotationBasedFactory, which generalizes both
 * this process and the related process in which samplers are created from 
 * Samplers annotations.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class NodeProcessorFactory implements ProcessorFactory
{
  @SuppressWarnings("rawtypes")
  public final AnnotationBasedFactory<Object,Processors,NodeProcessor> annotationBasedFactory;

  @SuppressWarnings("rawtypes")
  public NodeProcessorFactory()
  {
    this.annotationBasedFactory = new AnnotationBasedFactory<Object, Processors, NodeProcessor>(Processors.class);
  }
  
  public void setUseAnnotation(boolean use)
  {
    this.annotationBasedFactory.setUseAnnotation(use);
  }

  @SuppressWarnings({ "rawtypes" })
  @Override
  public List<? extends Processor> build(ProbabilityModel model) 
  {
    ProcessorProducer producer = new ProcessorProducer();
    List<NodeProcessor> result = annotationBasedFactory.build(model.getLatentVariables(), producer);
    return result;
  }
  
  @SuppressWarnings("rawtypes")
  private static class ProcessorProducer implements Producer<Object, NodeProcessor>
  {

    @SuppressWarnings({ "unchecked" })
    @Override
    public NodeProcessor<Object> produce(
        Object initiator,
        Class productType)
    {
      NodeProcessor initiated = (NodeProcessor) ReflexionUtils.instantiate(productType);
      initiated.setReference(initiator);
      return initiated;
    }
    
  }
}
