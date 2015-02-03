package bayonet.smc;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import bayonet.math.NumericalUtils;



public class TestCompactPopulation
{
  private static class NaiveCompactPopulation
  {
    private double sum = 0.0;
    private double sumSqrs = 0.0;
    
    public void insertLogWeight(double logWeight)
    {
      double weight = Math.exp(logWeight);
      sum += weight;
      sumSqrs += weight * weight;
    }
    
    public double ess()
    {
      return (sum*sum/sumSqrs);
    }
  }

  @Test
  public void test()
  {
    CompactPopulation lazy = new CompactPopulation();
    NaiveCompactPopulation naive = new NaiveCompactPopulation();
    
    Random rand = new Random(1);
    
    for (int i = 0; i < 100; i++)
    {
      double artificialLogWeights = rand.nextGaussian();
      lazy.insertLogWeight(artificialLogWeights);
      naive.insertLogWeight(artificialLogWeights);
    }
    
    Assert.assertEquals(lazy.ess(), naive.ess(), NumericalUtils.THRESHOLD);
  }

}
