package blang.prototype3;

import java.util.Random;

import blang.annotations.Samplers;
import blang.core.MHSampler;


@Samplers(RealScalar.NaiveMHSampler.class)
public class RealScalar implements Real
{
  private double value = 0.0;

  @Override
  public double doubleValue()
  {
    return value;
  }
  
  public void setValue(double v)
  {
    this.value = v;
  }
  
  private static class NaiveMHSampler extends MHSampler<RealScalar>
  {
    @Override
    public void propose(Random random, Callback callback)
    {
      final double oldValue = variable.doubleValue();
      callback.setProposalLogRatio(0.0);
      variable.setValue(oldValue + random.nextGaussian());
      if (!callback.sampleAcceptance())
        variable.setValue(oldValue);
    }
  }
}
