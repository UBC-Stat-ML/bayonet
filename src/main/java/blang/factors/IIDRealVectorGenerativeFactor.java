package blang.factors;

import java.util.Random;


import bayonet.distributions.Normal;
import bayonet.distributions.UnivariateRealDistribution;
import bayonet.distributions.Normal.MeanVarianceParameterization;
import bayonet.opt.DoubleArrays;
import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.annotations.Processors;
import blang.processing.NodeProcessor;
import blang.processing.ProcessorContext;
import blang.variables.RealValued;
import blang.variables.RealVectorInterface;



public class IIDRealVectorGenerativeFactor<P> implements GenerativeFactor
{
  private final UnivariateRealDistribution marginalDistribution;
  private final GenerativeFactor generativeFactor;
  
  @FactorComponent
  public final P marginalDistributionParameters;
  
  @FactorArgument(makeStochastic = true)
  public final RealVectorInterface variable;
  
  private IIDRealVectorGenerativeFactor(
      RealVectorInterface variable,
      UnivariateRealDistribution marginalDistribution,
      P marginalDistributionParameters)
  {
    if (!(marginalDistribution instanceof GenerativeFactor))
      throw new RuntimeException();
    this.generativeFactor = (GenerativeFactor) marginalDistribution;
    this.marginalDistribution = marginalDistribution;
    this.variable = variable;
    this.marginalDistributionParameters = marginalDistributionParameters;
  }
  
  public static class VectorNormProcessor implements NodeProcessor<RealVectorInterface>, RealValued
  {
    private RealVectorInterface variable;
    private double currentValue;

    @Override
    public void process(ProcessorContext context)
    {
      double sum = 0.0;
      for (double x : variable.getVector())
        sum += x*x;
      currentValue = Math.sqrt(sum);
    }

    @Override
    public void setReference(RealVectorInterface variable)
    {
      this.variable = variable;
    }

    @Override
    public double getValue()
    {
      return currentValue;
    }
    
  }
  
  public static IIDRealVectorGenerativeFactor<MeanVarianceParameterization> iidNormalOn(RealVectorInterface variable)
  {
    Normal<MeanVarianceParameterization> marginal = Normal.newNormal();
    return new IIDRealVectorGenerativeFactor<Normal.MeanVarianceParameterization>(variable, marginal, marginal.parameters);
  }

  @Override
  public double logDensity()
  {
    double sum = 0.0;
    double [] vector = variable.getVector();
    for (double x : vector)
    {
      marginalDistribution.getRealization().setValue(x);
      sum += marginalDistribution.logDensity();
    }
    return sum;
  }

  @Override
  public void generate(Random random)
  {
    double [] vector = variable.getVector();
    for (int i = 0; i < vector.length; i++)
    {
      generativeFactor.generate(random);
      vector[i] = marginalDistribution.getRealization().getValue();
    }
    variable.setVector(vector);
  }

}
