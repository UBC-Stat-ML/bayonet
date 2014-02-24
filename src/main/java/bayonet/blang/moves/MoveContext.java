package bayonet.blang.moves;

import bayonet.blang.ProbabilityModel;



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
