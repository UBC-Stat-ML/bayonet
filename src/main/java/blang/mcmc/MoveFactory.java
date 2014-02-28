package blang.mcmc;

import java.util.List;

import blang.ProbabilityModel;



public interface MoveFactory
{
  public List<Move> build(ProbabilityModel model);
}
