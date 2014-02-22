package bayonet.mcmc.moves;

import java.util.List;
import java.util.Random;




public interface Move
{
  public void execute(Random rand);
  
  public List<?> variablesCovered();
}
