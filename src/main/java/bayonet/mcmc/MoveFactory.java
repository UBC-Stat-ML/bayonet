package bayonet.mcmc;

import java.util.List;

import bayonet.mcmc.moves.Move;



public interface MoveFactory
{
  public List<Move> build(ProbabilityModel model);
}
