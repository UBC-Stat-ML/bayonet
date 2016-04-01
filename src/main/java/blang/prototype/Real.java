package blang.prototype;

import blang.annotations.Samplers;
import blang.mcmc.RealNaiveMHSampler;


@Samplers(RealNaiveMHSampler.class)
public interface Real
{
  public double get();
  public void set(double value);
}
