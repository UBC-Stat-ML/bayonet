package blang.mcmc;

import java.util.Random;

import blang.core.MHSampler;
import blang.prototype.Real;



public class RealNaiveMHSampler extends MHSampler<Real>
{
  @Override
  public void propose(Random random, Callback callback)
  {
    final double oldValue = variable.get();
    callback.setProposalLogRatio(0.0);
    variable.set(oldValue + random.nextGaussian());
    if (!callback.sampleAcceptance())
      variable.set(oldValue);
  }
}
