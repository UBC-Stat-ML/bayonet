package blang.prototype;

import static java.lang.Math.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import blang.core.FactorComposite;
import blang.core.ModelComponent;
import blang.factors.FactorUtils;
import blang.prototype.Exponential.ExponentialParams;
import blang.prototype.HMMLib.HMMLibParams;

import com.google.auto.service.AutoService;

@AutoService(FactorComposite.class) // allow discovery by the main 
public class HMM2 implements FactorComposite
{
  /*
   * Example of what would be generated by the DSL below. 
   * An alternative to HMM that showcases the ability to 
   * create reusable units.
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
   * hiddenStates | Real globalParam = globalParam, int len = len 
   * ~ HMMLib {
   *   def int nStates() {2}
   *   def int length() {len}
   *   def double transitionLogProbability(int from, int to) {
   *     val normalization = 1.0 + globalParam.get()
   *     log(from == to ? 1.0 : globalParam.get()) - log(normalization)
   *   }
   *   def double initialLogProbability(int i) {
   *     log(0.5)
   *   }
   * }
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
   * hiddenStates | Real globalParam = globalParam, int len = len 
   * ~ HMMLib {
   *   def int nStates() {2}
   *   def int length() {len}
   *   def double transitionLogProbability(int from, int to) {
   *     val normalization = 1.0 + globalParam.get()
   *     log(from == to ? 1.0 : globalParam.get()) - log(normalization)
   *   }
   *   def double initialLogProbability(int i) {
   *     log(0.5)
   *   }
   * }
   */
  private static final class HMMLibParams_0 implements HMMLibParams
  {
    private final Real globalParam;
    private final int len;
    
    private HMMLibParams_0(Real globalParam, int len)
    {
      this.globalParam = globalParam;
      this.len = len;
    }

    @Override
    public int length()
    {
      return len;
    }

    @Override
    public int nStates()
    {
      return 2;
    }

    @Override
    public double transitionLogProbability(int from, int to)
    {
      final double normalization = 1.0 + globalParam.get();
      return log(from == to ? 1.0 : globalParam.get()) - log(normalization);
    }

    @Override
    public double initialLogProbability(int i)
    {
      return log(0.5);
    }

    
  }
 
  /*
   * Header always generated for the second segment
   */
  @Override
  public Collection<? extends ModelComponent> components()
  {
    List<ModelComponent> result = new ArrayList<>();
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
      FactorUtils.addModelComponentsRecursively(factor, result);
    }
    
    /*
     * Second segment generated by:
     * 
     *   
     * hiddenStates | Real globalParam = globalParam, int len = len 
     * ~ HMMLib {
     *   def int nStates() {2}
     *   def int length() {len}
     *   def double transitionLogProbability(int from, int to) {
     *     val normalization = 1.0 + globalParam.get()
     *     log(from == to ? 1.0 : globalParam.get()) - log(normalization)
     *   }
     *   def double initialLogProbability(int i) {
     *     log(0.5)
     *   }
     * }
     * 
     */
    {
      HMMLibParams params = new HMMLibParams_0(globalParam, len);
      HMMLib factor = new HMMLib(hiddenStates, params);
      FactorUtils.addModelComponentsRecursively(factor, result);
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
  
//  private List<Sampler> samplers()
//  {
//    Inputs inputs = new Inputs();
//    Collection<? extends ModelComponent> modelComponents = components();
//    
//    // register the factors
//    for (ModelComponent f : modelComponents)
//      if (f instanceof Factor)
//        inputs.addFactor((Factor) f);
//    
//    // register the variables
//    inputs.addVariable(globalParam);
//    inputs.addVariable(hiddenStates);
//    for (ModelComponent c : modelComponents)
//      if (c instanceof FactorComposite)
//        inputs.addVariable((FactorComposite) c);
//    
//    // analyze the object graph
//    GraphAnalysis graphAnalysis = GraphAnalysis.create(inputs);
//    
//    // output visualization of the graph
//    graphAnalysis.accessibilityGraph.toDotExporter().export(new File("doc/hmm-accessibility-2.dot"));
//    graphAnalysis.factorGraphVisualization().export(new File("doc/hmm-factor-graph-2.dot"));
//    
//    System.out.println(graphAnalysis.toStringSummary());
//    
//    // create the samplers
//    return SamplerBuilder.instantiateSamplers(graphAnalysis );
//  }
  
//  public void sample(Random rand, int nIterations)
//  {
//    List<Sampler> samplers = samplers();
//    for (int i = 0; i < nIterations; i++)
//    {
//      Collections.shuffle(samplers, rand);
//      for (Sampler s : samplers)
//        s.execute(rand);
//      if (i % 1000 == 0)
//        System.out.println(Arrays.toString(hiddenStates.data));
//    }
//  }
  
//  public static void main(String [] args)
//  {
//    Random rand = new Random(1);
//    new HMM2().sample(rand, 100_000);
//  }
}