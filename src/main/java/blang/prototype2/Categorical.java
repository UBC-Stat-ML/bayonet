package blang.prototype2;

import java.util.Collection;
import java.util.function.Supplier;

import blang.core.Model;
import blang.core.ModelComponent;
import blang.prototype2.Scratch.HMM.Simplex;



public class Categorical implements Model
{
  
  private final Supplier<Simplex> probabilities;
  
  private final Int realization;
  
  public Categorical(Int realization, Supplier<Simplex> probabilities)
  {
    this.probabilities = probabilities;
    this.realization = realization;
  }

  @Override
  public Collection<? extends ModelComponent> components()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
