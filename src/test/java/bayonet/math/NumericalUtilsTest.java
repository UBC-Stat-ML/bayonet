package bayonet.math;

import org.junit.Assert;
import org.junit.Test;




public class NumericalUtilsTest
{
  @Test
  public void testLogAdd()
  {
    for (double logX = -100; logX < 100; logX++)
      for (double logY = -100; logY < 100; logY++)
        Assert.assertEquals(NumericalUtils.logAdd(logX, logY), Math.log(Math.exp(logX) + Math.exp(logY)), 1e-5);
  }
}
