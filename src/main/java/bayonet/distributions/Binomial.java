package bayonet.distributions;

import static blang.variables.IntegerVariable.intVar;
import static blang.variables.RealVariable.real;

import java.util.Random;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.util.CombinatoricsUtils;

import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.IntegerVariable;
import blang.variables.RealVariable;

  /** Binomial density
   * @author Sean Jewell (jewellsean@gmail.com)
   *
   */

  public class Binomial<P extends Binomial.Parameters> implements GenerativeFactor, UnivariateIntegerDistribution
  {
    /** 
     * The variable on which this density is defined
     */
    @FactorArgument(makeStochastic=true)
    public final IntegerVariable realization; 
    
    /**
     * Parameter of Binomial density
     */
    @FactorComponent
    public final P parameters; 
    
    public static interface Parameters
    {
      public double getProb();
      public int getTrials();
    }
    
    public static class ProbSuccessParameterization implements Parameters
    {
      @FactorArgument
      public final RealVariable prob; 
      
      @FactorArgument
      public final IntegerVariable trials;
      
      public ProbSuccessParameterization(RealVariable prob, IntegerVariable trials)
      {
        this.prob = prob;
        this.trials = trials;
      }
      
      @Override
      public double getProb()
      {
        return prob.getValue();
      }
      
      @Override
      public int getTrials()
      {
        return trials.getIntegerValue();
      }
      
    }
    
    public Binomial(IntegerVariable realization, P parameters)
    {
      this.realization = realization;
      this.parameters = parameters; 
    }

    @Override
    public double logDensity()
    {
      return logDensity(realization.getIntegerValue(), parameters.getProb(), parameters.getTrials()); 
    }

    @Override
    public IntegerVariable getRealization()
    {
      return realization;
    }

    @Override
    public void generate(Random random)
    {
      realization.setValue(generate(random, parameters.getProb(), parameters.getTrials()));
    }
    
    /* Static versions of the functionalities of this class */
    
    public static double logDensity(int point, double prob, int trials)
    {
       if( point < 0 || point > trials || prob < 0 || prob > 1 || trials <= 0)
         return Double.NEGATIVE_INFINITY;
       return CombinatoricsUtils.binomialCoefficientLog(trials, point) + 
           point * Math.log(prob) + (trials - point) * Math.log(1 - prob);
    }
    
    
    public static int generate(Random random, double prob, int trials)
    {
      final BinomialDistribution bd = new BinomialDistribution(new Random2RandomGenerator(random), trials, prob); 
      return bd.sample(); 
    }
    
    
    /* Syntactic sugar/method chaining */
    /**
     * Default prob is set to 0.5.
     * Default number of trials is 1 (ie. Bernoulli)  
     * @param realization
     * @return
     */
    public static Binomial<ProbSuccessParameterization> on(IntegerVariable realization)
    {
      return new Binomial<ProbSuccessParameterization>(realization, new ProbSuccessParameterization(real(0.5), intVar(1)));
    }
    
    public Binomial<ProbSuccessParameterization> withProbN(RealVariable prob, IntegerVariable trials)
    {
      return new Binomial<ProbSuccessParameterization>(realization, new ProbSuccessParameterization(prob, trials));
    }
    
    public Binomial<ProbSuccessParameterization> withProbN(double prob, int trials)
    {
      return new Binomial<ProbSuccessParameterization>(realization, new ProbSuccessParameterization(real(prob), intVar(trials)));
    }
    

    
  }

