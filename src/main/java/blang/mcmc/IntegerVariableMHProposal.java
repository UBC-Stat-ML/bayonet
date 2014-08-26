package blang.mcmc;

import java.util.List;
import java.util.Random;

import bayonet.distributions.Multinomial;
import blang.factors.Factor;
import blang.variables.IntegerVariable;


public class IntegerVariableMHProposal implements MHProposalDistribution
{
  @SampledVariable IntegerVariable variable;
  
  @ConnectedFactor List<Factor> connectedFactors;

  // TODO: implement some adaptation/optimization
  // TODO: add double checking for savedValue since NaN method does not work for int
  
  private int savedValue;
  private int NUM_STATES = 3; // possible increments by proposal

  
  @Override
  public Proposal propose(Random rand)
  {
       
    savedValue = variable.getIntegerValue();
    double [] incrementIdx = Multinomial.generate(rand, 1, 
        uniformDiscreteProbs(NUM_STATES));
    double [] stepsVector = steps(NUM_STATES);
    
    int increment = 0; 
    
    for(int i = 0; i < (2 * NUM_STATES); i++)
      increment += (int) incrementIdx[i] * stepsVector[i];
        
    final int newValue = savedValue + increment;
    variable.setValue(newValue);
    return new ProposalRealization();
  }
  
  private class ProposalRealization implements Proposal
  {

    @Override
    public double logProposalRatio() { return 0.0; } // proposal log ratio is zero due to symmetry

    @Override
    public void acceptReject(boolean accept)
    {
      if (!accept)
        variable.setValue(savedValue);
      
    }
  }
  
  private double [] steps(int numStates)
  {
    double [] steps = new double[numStates * 2];
    for (int i = 0; i < 2 * numStates; i++)
    {
      if(i < numStates)
        steps[i] = -1 * numStates + i; 
      if(i >= numStates)
        steps[i] = -1 * numStates + i + 1; // do not include zero 
    }
    return steps;
  }
  
  
  /**
   * Create a probability vector for multinomial distribution
   * creates a symmetry around zero by construction
   * 
   * @param numStates number of states with support on each side of zero
   * @return probability vector of size 2 * numStates
   */
  private double [] uniformDiscreteProbs(int numStates) 
  {
    double [] uniformProbs = new double[2 * numStates];
    for(int i = 0; i < 2 * numStates; i++)
      uniformProbs[i] = (1.0 / (2 * numStates));
    return uniformProbs;
  }
  
  
}
