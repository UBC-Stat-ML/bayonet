package blang.mcmc;

import java.util.Random;

import blang.core.MHSampler;
import blang.prototype.Int;



public class IntNaiveMHSampler extends MHSampler<Int>
{
  @Override
  public void propose(Random random, Callback callback)
  {
    final int oldValue = variable.get();
    callback.setProposalLogRatio(0.0);
    variable.set(oldValue + (random.nextBoolean() ? 1 : -1));
    if (!callback.sampleAcceptance())
      variable.set(oldValue);
  }
}
