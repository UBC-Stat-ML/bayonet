package blang.processing;

import java.util.List;

import blang.ProbabilityModel;
import blang.annotations.Processors;
import blang.annotations.util.AnnotationBasedFactory;
import blang.annotations.util.AnnotationBasedFactory.Producer;
import briefj.ReflexionUtils;



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
  
//  private final boolean useAnnotations;
//  
//  /**
//   * 
//   * @param useAnnotations Whether to use @Samplers annotations in addition to the one
//   *  included manually via include()
//   */
//  public ProcessorBuilder(boolean useAnnotations) 
//  {
//    this.useAnnotations = useAnnotations;
//  }
//  
//  private final Set<Class<? extends Processor>> 
//    exclusions = Sets.newHashSet();
//  
//  private final Map<Class<?>, Set<Class<? extends Processor>>>
//    inclusions = Maps.newHashMap();
//  
//  public void addProcessor(Class<?> variableType, Class<? extends Processor> opType)
//  {
//    BriefMaps.getOrPutSet(inclusions, variableType).add(opType);
//  }
//  
//  public void exclude(Class<? extends Processor> opType)
//  {
//    exclusions.add(opType);
//  }
//  
//  
//  public List<SingleNodeProcessor<?>> buildProcessors(ProbabilityModel model)
//  {
//    List<SingleNodeProcessor<?>> result = Lists.newArrayList();
//    
//    loop:for (Object variable : model.getLatentVariables())
//    {
//      Processors annotation = variable.getClass().getAnnotation(Processors.class);
//      
//      Set<Class<? extends Processor>> processorTypes = inclusions.get(variable.getClass());
//      
//      if (annotation == null && processorTypes == null)
//        continue loop;
//      
//      if (useAnnotations)
//        processorTypes.addAll(Arrays.asList(annotation.value()));
//      
//      for (Class<? extends Processor> processorType : processorTypes)
//        if (!exclusions.contains(processorType))
//        {
//          SingleNode instantiated 
//        }
//    }
//    
//    return result;
//  }
}
