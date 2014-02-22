package bayonet.mcmc.moves;

import java.util.List;
import java.util.Random;

import bayonet.mcmc.Factor;
import bayonet.mcmc.RealVariable;



public class RealVariableMHProposal implements MHProposal
{
  @SampledVariable 
  private RealVariable variable;
  
  @ConnectedFactor
  private List<Factor> connectedFactors;

  // TODO: implement some adaptation/optimization
  
  private double old = Double.NaN;
  
  @Override
  public double proposeInPlace(Random rand)
  {
    if (!Double.isNaN(old))
      throw new RuntimeException();
    old = variable.getValue();
    final double newValue = old + rand.nextGaussian();
    variable.setValue(newValue);
    return 0.0; // proposal log ratio is zero since Gaussian is symmetric
  }
    
  @Override
  public void acceptRejectInPlace(boolean accept)
  {
    if (!accept)
      variable.setValue(old);
    old = Double.NaN;
  }
  
}
