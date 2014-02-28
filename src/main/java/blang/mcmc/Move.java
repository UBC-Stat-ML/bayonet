package blang.mcmc;

import java.util.List;
import java.util.Random;




public interface Move extends Operator
{
  public void execute(Random rand);
  
  public List<?> variablesCovered();
}
