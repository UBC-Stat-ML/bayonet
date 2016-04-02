package blang.prototype;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import blang.accessibility.GraphAnalysis;
import blang.accessibility.GraphAnalysis.Inputs;
import blang.core.Sampler;
import blang.core.SamplerBuilder;
import blang.factors.Factor;
import blang.factors.FactorUtils;
import blang.prototype.Categorical.CategoricalParams;
import blang.prototype.Exponential.ExponentialParams;
import static java.lang.Math.*;

public class HMM 
{
  /*
   * Example of what would be generated by the DSL below. 
   * Note: most DSL lines actually generated two blocks.
   * 
   * variables {
   * 
   *   int len = 3;
   *   globalParam = new RealImpl();
   *   hiddenStates = new IntMatrix(len, 1);
   * 
   * }
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
  
  /*
   * Code generated by 
   * 
   * variables {
   * 
   *   int len = 3;
   *   globalParam = new RealImpl();
   *   hiddenStates = new IntMatrix(len, 1);
   * 
   * }
   * 
   */
  
  public final int len = 3;
  public final Real globalParam = new RealImpl();
  public final IntMatrix hiddenStates = new IntMatrix(len, 1);
  
  /*
   * First segment generated by the line 
   * 
   * globalParam ~ Exponential(1.0)
   */
  private static final class ExponentialParams_0 implements ExponentialParams
  {
    private ExponentialParams_0() 
    {
    }

    @Override
    public double getRate()
    {
      return 1.0;
    }
  }
  
  /*
   * First segment generated by the line
   * 
   * hiddenStates.get(0) ~ Categorical {
   *   def int nStates() {2}
   *   def double getLogProbability(int state) {
   *      log(0.5)
   *   }
   * }
   */
  private static final class CategoricalParams_1 implements CategoricalParams
  {
    private CategoricalParams_1()
    {
    }
    
    @Override 
    public int nStates() 
    { 
      return 2; 
    }
    
    @Override 
    public double getLogProbability(int state)
    {
      return log(0.5);
    }
  }
  
  /*
   * First segment generated by the line
   * 
   * hiddenStates.get(i-1) | Int previous = hiddenStates.entry(i - 1), Real globalParam 
   *      ~ Categorical {
   *        def int nStates() {2}
   *        def double getLogProbability(int state) {
   *          val prev = previous.get()
   *          val normalization = 1.0 + globalParam.get()
   *          log(prev == state ? 1.0 : globalParam.get()) - log(normalization)
   *        }
   *      }
   */
  private static final class CategoricalParams_2 implements CategoricalParams
  {
    private final Int previous;
    private final Real globalParam;
    
    private CategoricalParams_2(Int previous, Real globalParam)
    {
      this.previous = previous;
      this.globalParam = globalParam;
    }

    @Override public int nStates() 
    { 
      return 2; 
    }
    
    @Override public double getLogProbability(int state)
    {
      int prev = previous.get();
      double normalization = 1.0 + globalParam.get();
      return Math.log(prev == state ? 1.0 : globalParam.get()) - Math.log(normalization);
    }
  }
  
  /*
   * Header always generated for the second segment
   */
  private Collection<Factor> factors()
  {
    List<Factor> result = new ArrayList<>();
    /*
     * End of header
     */
    
    /*
     * Second segment generated by:
     * 
     * globalParam ~ Exponential(1.0) 
     */
    { // note: these brackets should be included, to enforce local scope
      ExponentialParams params = new ExponentialParams_0();
      Exponential factor = new Exponential(globalParam, params);
      FactorUtils.addFactorsRecursively(factor, result);
    }
    
    /*
     * Second segment generated by:
     * 
     * hiddenStates.get(0) ~ Categorical {
     *   def int nStates() {2}
     *   def double getLogProbability(int state) {
     *      log(0.5)
     *   }
     * }
     * 
     */
    {
      CategoricalParams params = new CategoricalParams_1();
      Categorical factor = new Categorical(hiddenStates.entry(0), params);
      FactorUtils.addFactorsRecursively(factor, result);
    }
    
    /*
     * Code generated by:
     * 
     * for (int i = 1; i < len; i++)
     */
    for (int i = 1; i < len; i++) // (identical)
      
      /*
       * Second segment generated by:
       */
      {
        CategoricalParams params = new CategoricalParams_2(hiddenStates.entry(i - 1), globalParam);
        Categorical factor = new Categorical(hiddenStates.entry(i), params);
        FactorUtils.addFactorsRecursively(factor, result);
      }

    /*
     * Footer (always generated)
     */
    return result;
  }
  /*
   * End of footer
   */
  
  // Note: the rest does not need to be generated
  
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
    
    System.out.println(graphAnalysis.toStringSummary());
    
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
