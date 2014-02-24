package blang;

import java.util.List;

import blang.moves.Move;



public interface MoveFactory
{
  public List<Move> build(ProbabilityModel model);
}
