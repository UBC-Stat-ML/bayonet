package bayonet.distributions;

import java.util.Random;



public class Bernoulli
{
  public static boolean generate(Random random, final double probabilityToBeTrue)
  {
    return random.nextDouble() < probabilityToBeTrue;
  }
}
