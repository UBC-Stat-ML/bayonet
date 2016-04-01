package blang.prototype;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import blang.accessibility.GraphAnalysis;
import blang.accessibility.GraphAnalysis.Inputs;
import blang.core.Sampler;
import blang.core.SamplerBuilder;
import blang.factors.Factor;
import blang.prototype.Categorical.CategoricalParams;



public class HMM
{
  public final int len = 3;
  public final Real globalParam = new RealImpl();
  public final IntMatrix hiddenStates = new IntMatrix(len, 1);
  
  
  /*
   * variables {
   * 
   *   int len = 3;
   *   globalParam = new RealImpl();
   *   hiddenStates = new IntMatrix(len, 1);
   * 
   * }
   * 
   * 
   * globalParam ~ Exponential(1.0)
   * 
   * hiddenStates.get(0) ~ Categorical {
   *   def int nStates() {2}
   *   def double getLogProbability(int state) {
   *      log(0.5)
   *   }
   * }
   * 
   * for (int i = 1; i < len; i++)
   *    hiddenStates.get(i-1) | Int previous = hiddenStates.entry(i - 1), Real globalParam 
   *      ~ Categorical {
   *        def int nStates() {2}
   *        def double getLogProbability(int state) {
   *          val prev = previous.get()
   *          val normalization = 1.0 + globalParam.get()
   *          log(prev == state ? 1.0 : globalParam.get()) - log(normalization)
   *        }
   *      }
   * 
   */
  private List<Factor> factors()
  {
    List<Factor> result = new ArrayList<>();
    
    Exponential exponential = new Exponential(globalParam, () -> 1.0);
    result.add(exponential);
    result.add(exponential.support);
    
    Categorical init = new Categorical(hiddenStates.entry(0), new CategoricalParams() {
      @Override public int nStates() { return 2; }
      @Override public double getLogProbability(int state)
      {
        return Math.log(0.5);
      }
    });
    result.add(init);
    result.add(init.supportFactor);
    
    for (int i = 1; i < len; i++)
    {
      Int previous = hiddenStates.entry(i - 1);
      
      CategoricalParams p = new CategoricalParams() {
        @Override public int nStates() { return 2; }
        @Override public double getLogProbability(int state)
        {
          // find previous sate
          int prev = previous.get();
          double normalization = 1.0 + globalParam.get();
          return Math.log(prev == state ? 1.0 : globalParam.get()) - Math.log(normalization);
        }
      };
      Categorical catDist = new Categorical(hiddenStates.entry(i), p);
      result.add(catDist);
      result.add(catDist.supportFactor);
    }
    
    return result;
  }
  
  private List<Sampler> samplers()
  {
    Inputs inputs = new Inputs();
    
    // register the factors
    for (Factor f : factors())
      inputs.addFactor(f);
    
    // register the variables
    inputs.addVariable(globalParam);
    inputs.addVariable(hiddenStates);
    
    // analyze the object graph
    GraphAnalysis graphAnalysis = GraphAnalysis.create(inputs);
    
    // output visualization of the graph
    graphAnalysis.accessibilityGraph.toDotExporter().export(new File("doc/hmm-accessibility.dot"));
    graphAnalysis.factorGraphVisualization().export(new File("doc/hmm-factor-graph.dot"));
    
    // create the samplers
    return SamplerBuilder.instantiateSamplers(graphAnalysis );
  }
  
  public void sample(Random rand, int nIterations)
  {
    List<Sampler> samplers = samplers();
    for (int i = 0; i < nIterations; i++)
    {
      Collections.shuffle(samplers, rand);
      for (Sampler s : samplers)
        s.execute(rand);
      if (i % 1000 == 0)
        System.out.println(Arrays.toString(hiddenStates.data));
    }
  }
  
  public static void main(String [] args)
  {
    Random rand = new Random(1);
    new HMM().sample(rand, 100_000);
  }
}
