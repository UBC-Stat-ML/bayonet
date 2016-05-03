package blang.prototype2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import blang.core.Model;
import blang.core.ModelComponent;
import blang.factors.Factor;
import blang.factors.LogScaleFactor;
import blang.prototype2.Scratch.HMM.Simplex;



public class Scratch
{
  public static class HMM implements Model
  {
    private int length;
    private Vec<Int> intVector;
    private TransitionCalculator calc;
    
    public static interface Vec<T>
    {
      public T get(int i); 
      public int size();
    }
    
    public static interface TransitionCalculator
    {
      public TransitionProbability transitionsProbabilities(int index);
    }
    
    public static interface TransitionProbability
    {
      public Simplex probabilitiesFrom(int state);
    }
    
    public static interface Simplex extends Vec<Real>
    {
    }
    
    public HMM HetHMM(Vec<Int> intVec, int length, TransitionCalculator calculator)
    {
      return null;
    }
    
    public HMM HomHMM(Vec<Int> intVec, int length, TransitionProbability prob)
    {
      return null;
    }
    

    // return: stuff in each logDensity { .. } plus stuff in bugs { ... }
    @Override
    public Collection<Factor> components()
    {
      return null;
//      List<Factor> result = new ArrayList<>();
//      for (int t = 0; t < length - 1; t++)
//        result.add(new Categorical(intVector.get(t+1), new Simplex_0()
//      return result;
    }
    
    private static class Simplex_0 implements Simplex
    {
      Int prev;
      TransitionProbability prs;
      int stateSize;

      @Override
      public Real get(int i)
      {
        return prs.probabilitiesFrom(prev.intValue()).get(i);
      }

      @Override
      public int size()
      {
        return stateSize;
      }
      
    }
  }
  
  public static class Categorical implements LogScaleFactor
  {
    
    public Simplex probabilities;
    
    public Int realization;
    
    Categorical(Int realiz, Simplex prs)
    {
      
    }

    @Override
    public double logDensity()
    {
      return Math.log(probabilities.get(realization.intValue()).doubleValue());
    }
    
    
  }
}
