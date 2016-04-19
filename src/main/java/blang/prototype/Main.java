package blang.prototype;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;

import blang.accessibility.GraphAnalysis;
import blang.accessibility.GraphAnalysis.Inputs;
import blang.core.FactorComposite;
import blang.core.ModelComponent;
import blang.core.Sampler;
import blang.core.SamplerBuilder;
import blang.factors.Factor;
import briefj.BriefCollections;
import briefj.opt.Option;
import briefj.run.Mains;
import briefj.run.Results;

import com.google.common.base.Optional;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;



public class Main implements Runnable
{
  private final FactorComposite model;
  
  @Option
  private Random random = new Random(1);
  
  @Option
  private int nIterations = 1000;
  
  public Main(FactorComposite model)
  {
    this.model = model;
  }

  @Override
  public void run()
  {
    sample(random, nIterations);
  }
  
  private void sample(Random rand, int nIterations)
  {
    List<Sampler> samplers = samplers();
    for (int i = 0; i < nIterations; i++)
    {
      Collections.shuffle(samplers, rand);
      for (Sampler s : samplers)
        s.execute(rand);
      if (i % 1000 == 0)
        System.out.println("Iteration " + i);
    }
  }
  
  private List<Sampler> samplers()
  {
    Inputs inputs = new Inputs();
    Collection<? extends ModelComponent> modelComponents = model.components();
    
    // register the factors
    for (ModelComponent f : modelComponents)
      if (f instanceof Factor)
        inputs.addFactor((Factor) f);
    
    // register the variables
    for (Field f : model.getClass().getFields())
      if (!f.getType().isPrimitive())
        try { inputs.addVariable(f.get(model)); } 
        catch (Exception e) { throw new RuntimeException(e); }
    for (ModelComponent c : modelComponents)
      if (c instanceof FactorComposite)
        inputs.addVariable((FactorComposite) c);
    
    // analyze the object graph
    GraphAnalysis graphAnalysis = GraphAnalysis.create(inputs);
    
    // output visualization of the graph
    graphAnalysis.accessibilityGraph.toDotExporter().export(Results.getFileInResultFolder("accessibility-graph.dot"));
    graphAnalysis.factorGraphVisualization().export(Results.getFileInResultFolder("factor-graph.dot"));
    
    System.out.println(graphAnalysis.toStringSummary());
    
    // create the samplers
    return SamplerBuilder.instantiateSamplers(graphAnalysis);
  }
  
  /* 
   * *********************
   * Low level stuff below
   * *********************
   */
  
  /*
   * TODO: use git style parsing to offer:  [NO? - see below]
   *    - "blang list" to list all models
   *    - "blang sample .."
   *    - "blang visualize .."
   *    - "blang geweke .."
   *    - "blang [other diagnostics, etc] .."
   *    
   * See:
   *    - https://github.com/airlift/airline
   *    - http://rvesse.github.io/airline/guide/
   *    - https://github.com/rvesse/airline
   *    - https://github.com/cbeust/jcommander
   *    - https://github.com/airlift/airline
   *    
   * Actually, might be better to just create several executable
   * unless there is a compelling argument not to, also, might
   * want to write these in some kind of workflow language?
   */
  public static void main(String [] _args) throws InstantiationException, IllegalAccessException 
  {
    List<String> args = Arrays.asList(_args);
    ModelLoader loader = new ModelLoader();
    
    if (args.isEmpty())
      exitOnBadKeyError(loader, "Specify a model from: ");
    
    final String modelName = args.get(0);
    Optional<Class<? extends FactorComposite>> theClass = loader.load(modelName);
    
    if (theClass.isPresent())
      Mains.instrumentedRun(
          args.subList(1, args.size()).toArray(new String[0]), 
          new Main(theClass.get().newInstance()));
    else
      exitOnBadKeyError(loader,String.format("Model '%s' not found", modelName));
  }

  public static void exitOnBadKeyError(ModelLoader loader, String message)
  {
    System.err.println(message);
    for (String validKey : loader.validKeys())
      System.err.println("\t" + validKey);
    System.exit(1);
  }
  
  public static class ModelLoader
  {
    public final Multimap<String, Class<? extends FactorComposite>> simpleNameToClass;
    
    public ModelLoader()
    {
      this.simpleNameToClass = LinkedHashMultimap.create();
      ServiceLoader<FactorComposite> loader = ServiceLoader.load(FactorComposite.class);
      for (FactorComposite fcInstance : loader)
      {
        Class<? extends FactorComposite> theClass = fcInstance.getClass();
        simpleNameToClass.put(theClass.getSimpleName(), theClass);
      }
    }
    
    public LinkedHashSet<String> validKeys()
    {
      LinkedHashSet<String> result = new LinkedHashSet<>();
      
      for (String key : simpleNameToClass.keySet())
      {
        Collection<Class<? extends FactorComposite>> collection = simpleNameToClass.get(key);
        if (collection.size() == 1)
          result.add(BriefCollections.pick(collection).getSimpleName());
        else
          collection.stream().sequential().map(c -> c.getName()).forEachOrdered(result::add);
      }
      
      return result;
    }
    
    public Optional<Class<? extends FactorComposite>> load(String simpleOrQualifiedName)
    {
      return isQualified(simpleOrQualifiedName) ? 
          loadFromQualifiedName(simpleOrQualifiedName) : 
          loadFromSimpleName(simpleOrQualifiedName);
    }

    private Optional<Class<? extends FactorComposite>> loadFromSimpleName(
        String simpleName)
    {
      Collection<Class<? extends FactorComposite>> collection = simpleNameToClass.get(simpleName);
      return collection.size() == 1 ? 
          Optional.of(BriefCollections.pick(collection)) :
          Optional.absent();
    }

    private Optional<Class<? extends FactorComposite>> loadFromQualifiedName(String qualifiedName)
    {
      try
      {
        @SuppressWarnings("unchecked")
        Class<? extends FactorComposite> fc = (Class<? extends FactorComposite>) Class.forName(qualifiedName);
        return Optional.of(fc);
      } catch (ClassNotFoundException e)
      {
        return Optional.absent();
      }
    }

    private boolean isQualified(String simpleOrQualifiedName)
    {
      return simpleOrQualifiedName.contains(".");
    }
  }
}
