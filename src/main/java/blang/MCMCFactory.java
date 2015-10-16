package blang;


import java.util.Collections;
import java.util.List;
import java.util.Random;

import blang.mcmc.MoveFactory;
import blang.mcmc.MoveSet;
import blang.mcmc.NodeMoveFactory;
import blang.mcmc.Operator;
import blang.processing.NodeProcessor;
import blang.processing.NodeProcessorFactory;
import blang.processing.Processor;
import blang.processing.ProcessorContext;
import blang.processing.ProcessorFactory;
import briefj.opt.Option;
import briefj.opt.OptionSet;

import com.google.common.collect.Lists;




public class MCMCFactory 
{
  public static class MCMCOptions
  {
    @Option public int nMCMCSweeps = 10000;
    @Option(gloss = "Thinning period. Should be greater or equal to 1.") 
    public int thinningPeriod = 10;
    @Option public int burnIn = 0;
    @Option public boolean progressCODA = false;
    @Option public boolean CODA = true;
    @Option public Random random = new Random(1);
  }

  @OptionSet(name = "mcmc") 
  public MCMCOptions mcmcOptions = new MCMCOptions();
  
  private boolean checkAllNodesCoveredByMCMCMoves = true;
  
  public void addMoveFactory(MoveFactory factory)
  {
    moveFactories.factories.add(factory);
  }
  
  public void addProcessorFactory(ProcessorFactory factory)
  {
    processorFactories.factories.add(factory);
  }
  
  public void addProcessor(final Processor processor)
  {
    processorFactories.factories.add(new ProcessorFactory() {
      @Override
      public List<? extends Processor> build(ProbabilityModel model)
      {
        return Collections.singletonList(processor);
      }
    });
  }

  public void addNodeMove(Class<? extends Object> variableType, Class<? extends Operator> moveType)
  {
    getNodeMoveFactory().annotationBasedFactory.add(variableType, moveType);
  }
  
  @SuppressWarnings({ "unchecked"})
  public void excludeNodeMove(Class<? extends Operator> moveType)
  {
    // TODO: more permanent solution needed
    @SuppressWarnings("rawtypes")
    Class _cast = moveType;
    getNodeMoveFactory().annotationBasedFactory.exclude(_cast);
  }
  
  @SuppressWarnings("rawtypes")
  public void addNodeProcessor(Class<? extends Object> variableType, Class<? extends NodeProcessor> processorType)
  {
    getNodeProcessorFactory().annotationBasedFactory.add(variableType, processorType);
  }
  
  @SuppressWarnings("rawtypes")
  public void excludeNodeProcessor(Class<? extends NodeProcessor> processorType)
  {
    getNodeProcessorFactory().annotationBasedFactory.exclude(processorType);
  }
  
  private Factories<MoveFactory,NodeMoveFactory> moveFactories = new Factories<MoveFactory,NodeMoveFactory>(new NodeMoveFactory());
  private Factories<ProcessorFactory,NodeProcessorFactory> processorFactories = new Factories<ProcessorFactory,NodeProcessorFactory>(new NodeProcessorFactory());
  
  public static class Factories<T, S extends T>
  {
    public final List<T> factories = Lists.newArrayList();
    private final S standardFactory;
    
    public Factories(S standardFactory)
    {
      this.standardFactory = standardFactory;
      factories.add(standardFactory);
    }
  }
  
  public void disableNodeMoveFactory()
  {
    moveFactories.factories.remove(moveFactories.standardFactory);
  } 
  
  private NodeMoveFactory getNodeMoveFactory()
  {
    return moveFactories.standardFactory;
  }
  
  private NodeProcessorFactory getNodeProcessorFactory()
  {
    return processorFactories.standardFactory;
  }
  
  public MCMCAlgorithm build(Object modelSpecification)
  {
    return build(ProbabilityModel.parse(modelSpecification, false));
  }
  
  public MCMCAlgorithm build(Object modelSpecification, boolean clone)
  {
    return build(ProbabilityModel.parse(modelSpecification, clone));
  }

  public MCMCAlgorithm build(ProbabilityModel model)
  {
    MoveSet sampler = new MoveSet(model, moveFactories.factories, checkAllNodesCoveredByMCMCMoves);
    
    List<Processor> processors = buildProcessors(model);
    return new MCMCAlgorithm(model, sampler, processors, mcmcOptions);
  }
  
  static void callProcessors(List<Processor> processors, ProcessorContext context)
  {
    for (Processor p : processors)
      p.process(context);
  }

  public List<Processor> buildProcessors(ProbabilityModel model)
  {
    List<Processor> result = Lists.newArrayList();
    for (ProcessorFactory f : processorFactories.factories)
      result.addAll(f.build(model));
    return result;
  }

  
  public void setCheckAllNodesCoveredByMCMCMoves(
      boolean checkAllNodesCoveredByMCMCMoves)
  {
    this.checkAllNodesCoveredByMCMCMoves = checkAllNodesCoveredByMCMCMoves;
  }

}
