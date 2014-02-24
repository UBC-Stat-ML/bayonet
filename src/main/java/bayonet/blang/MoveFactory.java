package bayonet.blang;

import java.util.List;

import bayonet.blang.moves.Move;



public interface MoveFactory
{
  public List<Move> build(ProbabilityModel model);
}
