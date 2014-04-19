package blang.mcmc;

import java.util.List;

import blang.ProbabilityModel;


/**
 * A MoveFactory is responsible for instantiating several 
 * Moves, for example there might be one Move object for
 * each variable in a simple Gibbs situation. More complex 
 * cases include block-Gibbs moves.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface MoveFactory
{
  public List<Move> build(ProbabilityModel model);
}
