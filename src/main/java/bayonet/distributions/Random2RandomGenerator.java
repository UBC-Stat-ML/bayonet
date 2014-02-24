package bayonet.distributions;

import java.util.Random;

import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;



public class Random2RandomGenerator extends AbstractRandomGenerator implements RandomGenerator
{
  private final Random rand;
  
  public Random2RandomGenerator(Random rand) { this.rand = rand; }

  @Override
  public void setSeed(long seed)
  {
    rand.setSeed(seed);
  }

  @Override
  public double nextDouble()
  {
    return rand.nextDouble();
  }
}