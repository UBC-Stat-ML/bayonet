package blang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import blang.annotations.DefineFactor;
import blang.annotations.FactorComponent;
import blang.factors.Factor;
import blang.factors.FactorList;
import blang.variables.RealVariable;



public class TestFactorList
{
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void testFactorListArguments()
  {
    List<RealVariable> vars = new ArrayList<RealVariable>();
    final int n = 17;
    for (int i = 0; i < n; i++)
      vars.add(new RealVariable(0.0));
    ModelSpec modelSpec = new ModelSpec(vars);
    ProbabilityModel model = new ProbabilityModel(modelSpec);
    System.out.println(model.getLatentVariables().size());
    Assert.assertTrue(model.getLatentVariables().size() == n);
    Assert.assertTrue(new HashSet(model.getLatentVariables()).equals(new HashSet(vars)));
  }
  
  public static class ModelSpec
  {
    @DefineFactor
    public final MyFactor myFactor;
    
    public ModelSpec(List<RealVariable> vars)
    {
      myFactor = new MyFactor(vars);
    }
  }
  
  public static class MyFactor implements Factor
  {
    @FactorComponent
    public final FactorList<RealVariable> arguments;
    
    public MyFactor(List<RealVariable> list)
    {
      arguments = FactorList.ofArguments(list, true);
    }

    @Override
    public double logDensity()
    {
      throw new RuntimeException();
    }
  }
}
