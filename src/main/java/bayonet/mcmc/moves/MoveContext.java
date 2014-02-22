package bayonet.mcmc.moves;

import bayonet.mcmc.ProbabilityModel;



public class MoveContext
{
  private final ProbabilityModel model;
  
  // TODO: adaptation flags, etc.

  public MoveContext(ProbabilityModel model)
  {
    this.model = model;
  }

  public ProbabilityModel getModel()
  {
    return model;
  }
}
