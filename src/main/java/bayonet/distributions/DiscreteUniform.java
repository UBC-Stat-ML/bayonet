package bayonet.distributions;

import java.util.List;
import java.util.Random;



public class DiscreteUniform
{
  public static <S> S sample(List<S> items, Random rand)
  {
    return items.get(rand.nextInt(items.size()));
  }
}
