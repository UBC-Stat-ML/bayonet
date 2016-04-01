package blang.prototype;

import java.util.ArrayList;
import java.util.List;

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
  public List<Factor> factors()
  {
    List<Factor> result = new ArrayList<>();
    
    Exponential exponential = new Exponential(globalParam, () -> 1.0);
    result.add(exponential);
    result.add(exponential.support);
    
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
}
