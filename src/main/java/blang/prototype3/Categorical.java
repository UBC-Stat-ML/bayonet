package blang.prototype3;

import java.util.Collection;
import java.util.function.Supplier;

import blang.core.Model;
import blang.core.ModelComponent;



public class Categorical implements Model
{
  
  @SuppressWarnings("unused")
  private final Supplier<Simplex> probabilities;
  
  @SuppressWarnings("unused")
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
