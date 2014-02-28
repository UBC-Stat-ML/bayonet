package blang;


import java.util.List;
import java.util.Random;

import blang.factors.Factor;
import blang.mcmc.Move;
import blang.mcmc.MoveFactory;
import blang.mcmc.NodeMoveFactory;
import blang.processing.NodeProcessor;
import blang.processing.NodeProcessorFactory;
import blang.processing.Processor;
import blang.processing.ProcessorContext;
import blang.processing.ProcessorFactory;

import com.google.common.collect.Lists;



public abstract class ModelRunner implements Runnable
{
  private final ProbabilityModel model = new ProbabilityModel();
  
  private int nMCMCSweeps = 10000;
  private int thinningPeriod = 100;
  
  public Random random = new Random(1);
  
  public void addMoveFactory(MoveFactory factory)
  {
    moveFactories.add(factory);
  }
  
  public void addProcessorFactory(ProcessorFactory factory)
  {
    processorFactories.add(factory);
  }

  public void addNodeMove(Class<? extends Object> variableType, Class<? extends Move> moveType)
  {
    getNodeMoveFactory().annotationBasedFactory.add(variableType, moveType);
  }
  
  public void excludeNodeMove(Class<? extends Move> moveType)
  {
    getNodeMoveFactory().annotationBasedFactory.exclude(moveType);
  }
  
  @SuppressWarnings("rawtypes")
  public void addNodeProcessor(Class<? extends Object> variableType, Class<? extends NodeProcessor> processorType)
  {
    getNodeProcessorFactory().annotationBasedFactory.add(variableType, processorType);
  }
  
  @SuppressWarnings("rawtypes")
  public void exculdeNodeProcessor(Class<? extends NodeProcessor> processorType)
  {
    getNodeProcessorFactory().annotationBasedFactory.exclude(processorType);
  }

  private List<MoveFactory> moveFactories = Lists.newArrayList();
  private boolean buildMovesUsingAnnotations = true;
  private NodeMoveFactory _nodeMoveFactory = null;
  
  private List<ProcessorFactory> processorFactories = Lists.newArrayList();
  private boolean buildProcessorsUsingAnnotation = true;
  private NodeProcessorFactory _nodeProcessorFactory = null;

  
  
  private NodeMoveFactory getNodeMoveFactory()
  {
    if (_nodeMoveFactory == null)
      _nodeMoveFactory = new NodeMoveFactory(buildMovesUsingAnnotations);
    return _nodeMoveFactory;
  }
  
  private NodeProcessorFactory getNodeProcessorFactory()
  {
    if (_nodeProcessorFactory == null)
      _nodeProcessorFactory = new NodeProcessorFactory(buildProcessorsUsingAnnotation);
    return _nodeProcessorFactory;
  }

  // command line arguments for n iteration/time of iteration, etc

  @Override
  public void run()
  {
    // parse this
    model.parse(this);
    
    System.out.println(getModel());
    
    // create sampler
    moveFactories.add(getNodeMoveFactory());
    PosteriorSampler sampler = new PosteriorSampler(model, moveFactories);
    
    // create processors
    List<Processor> processors = buildProcessors();
     
//    call collect: use annotations for stat collection functions
    
    // run sampling!
    for (int i = 0; i < nMCMCSweeps; i++)
    {
      sampler.sweep(random);
      if (i % thinningPeriod == 0)
        callProcessors(processors, new ProcessorContext(i, model));
    }
  }
  
  private void callProcessors(List<Processor> processors, ProcessorContext context)
  {
    for (Processor p : processors)
      p.process(context);
  }

  private List<Processor> buildProcessors()
  {
    processorFactories.add(getNodeProcessorFactory());
    List<Processor> result = Lists.newArrayList();
    for (ProcessorFactory f : processorFactories)
      result.addAll(f.build(model));
    return result;
  }

  protected <T extends Factor> T observed(T factor)
  {
    model.setVariablesInFactorAsObserved(factor);
    return factor;
  }

  public ProbabilityModel getModel()
  {
    return model;
  }

}
