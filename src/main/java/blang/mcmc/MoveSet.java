package blang.mcmc;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import blang.ProbabilityModel;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;




public class MoveSet
{
  private final List<Move> moves;
  private final ProbabilityModel model;
  
  public MoveSet(ProbabilityModel model, List<MoveFactory> factories, boolean check)
  {
    this.model = model;
    this.moves = init(factories, check);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<Move> init(List<MoveFactory> factories, boolean check)
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
    
    if (check)
      if (!coveredNode.containsAll(model.getLatentVariables()))
      {
        Set<String> missingNodeNames = Sets.newHashSet();
        for (Object variable : model.getLatentVariables())
          if (!coveredNode.contains(variable))
            missingNodeNames.add(model.getName(variable));
        throw new RuntimeException("Not all variables have a sampler. Those not covered are: " + Joiner.on(", ").join(missingNodeNames));
      }
    
    return result;
  }
  
  public void sweep(Random rand)
  {
    Collections.shuffle(moves, rand);
    for (Move move : moves)
      move.execute(rand);
  }
  
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    for (Move move : moves)
      result.append(move + "\n");
    return result.toString();
  }
}
