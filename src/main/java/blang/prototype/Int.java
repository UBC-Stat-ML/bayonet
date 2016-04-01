package blang.prototype;

import blang.annotations.Samplers;
import blang.mcmc.IntNaiveMHSampler;


@Samplers(IntNaiveMHSampler.class)
public interface Int
{
  public int get();
  public void set(int value);
}
