package blang.prototype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import blang.core.FactorComposite;
import blang.factors.Factor;
import blang.factors.FactorUtils;
import blang.prototype.Categorical.CategoricalParams;



public class HMMLib implements FactorComposite
{
  public static interface HMMLibParams
  {
    public int length();
    public int nStates();
    public double transitionLogProbability(int from, int to);
    public double initialLogProbability(int i);
  }
  
  private final IntMatrix hiddenStates;
  private final HMMLibParams params;
  
  public HMMLib(IntMatrix hiddenStates, HMMLibParams params)
  {
    this.hiddenStates = hiddenStates;
    this.params = params;
  }

  @Override
  public Collection<Factor> componentFactors()
  {
    List<Factor> result = new ArrayList<>();
    
    {
      CategoricalParams param = new CategoricalParams_0(params);
      Categorical factor = new Categorical(hiddenStates.entry(0), param);
      FactorUtils.addFactorsRecursively(factor, result);
    }
    
    for (int t = 1; t < params.length(); t++)
    {
      {
        CategoricalParams param = new CategoricalParams_1(params, hiddenStates.entry(t-1));
        Categorical factor = new Categorical(hiddenStates.entry(t), param);
        FactorUtils.addFactorsRecursively(factor, result);
      }
    }
    
    return result;
  }
  
  private static final class CategoricalParams_0 implements CategoricalParams
  {
    private final HMMLibParams params;
    
    private CategoricalParams_0(HMMLibParams params)
    {
      this.params = params;
    }

    @Override 
    public int nStates() 
    { 
      return params.nStates();
    }
    
    @Override 
    public double getLogProbability(int cur)
    {
      return params.initialLogProbability(cur);
    }
  }
  
  private static final class CategoricalParams_1 implements CategoricalParams
  {
    private final HMMLibParams params;
    private final Int prev;
    
    private CategoricalParams_1(HMMLibParams params, Int prev)
    {
      this.params = params;
      this.prev = prev;
    }
    
    @Override
    public double getLogProbability(int state)
    {
      return params.transitionLogProbability(prev.get(), state);
    }
    
    @Override
    public int nStates()
    {
      return params.nStates();
    }
  }
  
//
//  @Override
//  public Collection<Factor> factors()
//  {
//    
//    return null;
//  }
//  
}
