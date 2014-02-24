package blang.factors;

import java.util.Random;



public interface GenerativeFactor extends Factor
{
  public void generate(Random random);
}
