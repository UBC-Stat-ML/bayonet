package blang.prototype3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import blang.accessibility.GraphAnalysis;
import blang.accessibility.GraphAnalysis.Inputs;
import blang.core.Model;
import blang.core.ModelComponent;
import blang.core.Sampler;
import blang.core.SamplerBuilder;
import blang.factors.Factor;
import briefj.opt.Option;
import briefj.run.Mains;
import briefj.run.Results;

import com.google.common.base.Optional;



public class Main implements Runnable
{
  private final Model model;
  
  @Option
  private Random random = new Random(1);
  
  @Option
  private int nIterations = 10_000_000;
  
  public Main(Model model)
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
    List<Sampler> samplers = samplers(model);
    for (int i = 0; i < nIterations; i++)
    {
      Collections.shuffle(samplers, rand);
      for (Sampler s : samplers)
        s.execute(rand);
      if ((i+1) % 1_000_000 == 0)
        System.out.println("Iteration " + (i+1));
    }
  }
   
  private static List<Factor> factors(ModelComponent root)
  {
    LinkedList<ModelComponent> queue = new LinkedList<>();
    queue.add(root);
    List<Factor> result = new ArrayList<Factor>();
    Set<ModelComponent> visited = new HashSet<>();
    while (!queue.isEmpty())
    {
      ModelComponent current = queue.poll();
      visited.add(current);
      if (current instanceof Factor)
        result.add((Factor) current);
      if (current instanceof Model)
      {
        Model model = (Model) current; 
        for (ModelComponent child : model.components())
          if (!visited.contains(child))
            queue.add(child);
      }
    }
    return result;
  }
 
  
  private static List<Sampler> samplers(Model model)
  {
    Inputs inputs = new Inputs();
    
    for (Factor f : factors(model))
      inputs.addFactor((Factor) f);
    
    // register the variables
    for (Field f : model.getClass().getFields())
      if (!f.getType().isPrimitive())
        try { inputs.addVariable(f.get(model)); } 
        catch (Exception e) { throw new RuntimeException(e); }
//    for (ModelComponent c : modelComponents)
//      if (c instanceof Model)
//        inputs.addVariable((Model) c);
    
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
  @SuppressWarnings("unchecked")
  public static void main(String [] _args) throws InstantiationException, IllegalAccessException 
  {
    List<String> args = Arrays.asList(_args);
//    ModelLoader loader = new ModelLoader();
    
    Optional<Class<? extends Model>> theClass = Optional.absent();
    if (!args.isEmpty())
      try { theClass = Optional.of((Class<? extends Model>) Class.forName(args.get(0))); }
      catch (ClassNotFoundException e) {}
    
    if (theClass.isPresent())
      Mains.instrumentedRun(
          args.subList(1, args.size()).toArray(new String[0]), 
          new Main(theClass.get().newInstance()));
    else
    {
      System.err.println("The first argument must specify a fully qualified (e.g. package.subpack.ClassName) "
          + "class implementing the Model interface.");
      System.exit(1);
    }
  }
  
  /*
   * strategy below might be fragile (relies on the annotation pipeline which seems
   * to be complicated to setup on eclipse, etc) 
   * 
   * instead, just use the fully classified model name and
   * create script at xtend compile time
   */

//  public static void exitOnBadKeyError(ModelLoader loader, String message)
//  {
//    System.err.println(message);
//    for (String validKey : loader.validKeys())
//      System.err.println("\t" + validKey);
//    System.exit(1);
//  }
//  
//  public static class ModelLoader
//  {
//    public final Multimap<String, Class<? extends Model>> simpleNameToClass;
//    
//    public ModelLoader()
//    {
//      this.simpleNameToClass = LinkedHashMultimap.create();
//      ServiceLoader<Model> loader = ServiceLoader.load(Model.class);
//      for (Model fcInstance : loader)
//      {
//        Class<? extends Model> theClass = fcInstance.getClass();
//        simpleNameToClass.put(theClass.getSimpleName(), theClass);
//      }
//    }
//    
//    public LinkedHashSet<String> validKeys()
//    {
//      LinkedHashSet<String> result = new LinkedHashSet<>();
//      
//      for (String key : simpleNameToClass.keySet())
//      {
//        Collection<Class<? extends Model>> collection = simpleNameToClass.get(key);
//        if (collection.size() == 1)
//          result.add(BriefCollections.pick(collection).getSimpleName());
//        else
//          collection.stream().sequential().map(c -> c.getName()).forEachOrdered(result::add);
//      }
//      
//      return result;
//    }
//    
//    public Optional<Class<? extends Model>> load(String simpleOrQualifiedName)
//    {
//      return isQualified(simpleOrQualifiedName) ? 
//          loadFromQualifiedName(simpleOrQualifiedName) : 
//          loadFromSimpleName(simpleOrQualifiedName);
//    }
//
//    private Optional<Class<? extends Model>> loadFromSimpleName(
//        String simpleName)
//    {
//      Collection<Class<? extends Model>> collection = simpleNameToClass.get(simpleName);
//      return collection.size() == 1 ? 
//          Optional.of(BriefCollections.pick(collection)) :
//          Optional.absent();
//    }
//
//    private Optional<Class<? extends Model>> loadFromQualifiedName(String qualifiedName)
//    {
//      try
//      {
//        @SuppressWarnings("unchecked")
//        Class<? extends Model> fc = (Class<? extends Model>) Class.forName(qualifiedName);
//        return Optional.of(fc);
//      } catch (ClassNotFoundException e)
//      {
//        return Optional.absent();
//      }
//    }
//
//    private boolean isQualified(String simpleOrQualifiedName)
//    {
//      return simpleOrQualifiedName.contains(".");
//    }
//  }
}
