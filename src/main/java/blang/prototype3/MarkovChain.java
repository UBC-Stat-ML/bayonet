package blang.prototype3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import blang.core.Model;
import blang.core.ModelComponent;


/**

b3 code shown in this class:

```
model MarkovChain {

  interface TransitionCalculator {
    def List<Simplex> transitionsProbabilities(int index)
  }

  param TransitionCalculator calculator
  
  random Vec<Int> states
  
  laws {
    for (int t = 0; t < states.size(); t++)
      states.get(t + 1) | Int prev = states.get(t), calculator, t
        ~ Categorical(calculator.transitionsProbabilities(t).get(prev))
  }
}
```
 */
public class MarkovChain implements Model
{
  interface TransitionCalculator 
  {
    public List<Simplex> transitionsProbabilities(int index);
  }
  
  private final Supplier<TransitionCalculator> calculator;
  
  private final Vec<Int> states;
  
  public MarkovChain(Supplier<TransitionCalculator> calculator, Vec<Int> states)
  {
    this.calculator = calculator;
    this.states = states;
  }

  @Override
  public Collection<? extends ModelComponent> components()
  {
    ArrayList<ModelComponent> components = new ArrayList<>();
    
    for (int t = 0; t < states.size(); t++)
      components.add(
          new Categorical(states.get(t+1), 
              $generated_setupSubModel0Param0(states.get(t), calculator, t)));
    
    return components;
  }

  private static Supplier<Simplex> $generated_setupSubModel0Param0(Int prev,
      Supplier<TransitionCalculator> $generated_calculator, int t)
  {
    return new Supplier<Simplex>()
    {
      @Override
      public Simplex get()
      {
        TransitionCalculator calculator = $generated_calculator.get();
        
        return calculator.transitionsProbabilities(t).get(prev.intValue());
      }
    };
  }
}

/*
And an example how this would get used in a bigger model:

model HMM {

  ...
  
  laws {
    
    ...
    
    hiddenStates 
      | rateMatrix, TransitionCalculator matrixExponentiator = matrixExponentiator(times)
      ~ MarkovChain(matrixExponentiator.on(rateMatrix))
      
    for (int t = 0; t < chainLength(); t++)
      observation.get(t) | Int latent = hiddenStates.get(t), means, variances
        ~ Normal(means.get(latent), variances.get(latent))
  
  }

}


*/
