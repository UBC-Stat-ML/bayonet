package blang;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import blang.moves.Move;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;




public class Sampler
{
  private final List<Move> moves;
  private final ProbabilityModel model;
  
  
  
  public Sampler(ProbabilityModel model, MoveFactory ...factories)
  {
    this.model = model;
    this.moves = init(Arrays.asList(factories));
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<Move> init(List<MoveFactory> factories)
  {
    List<Move> result = Lists.newArrayList();
    Set coveredNode = Sets.newIdentityHashSet();
    
    for (MoveFactory factory : factories)
    {
      List<Move> moves = factory.build(model);
      for (Move move : moves)
        coveredNode.addAll(move.variablesCovered());
      result.addAll(moves);
    }
    
    if (!coveredNode.containsAll(model.getLatentVariables()))
    {
      Set difference = Sets.newIdentityHashSet();
      difference.addAll(model.getLatentVariables());
      difference.removeAll(coveredNode);
      throw new RuntimeException("Not all variables have a sampler. Those not covered are: " + Joiner.on(",").join(difference));
    }
    
    return result;
  }
  
  public void sweep(Random rand)
  {
    Collections.shuffle(moves, rand);
    for (Move move : moves)
      move.execute(rand);
    
    // TODO: some sort of logging/checkpointing
  }
}
