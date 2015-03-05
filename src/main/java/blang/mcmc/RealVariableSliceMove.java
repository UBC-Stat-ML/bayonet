package blang.mcmc;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.util.Pair;

import blang.factors.Factor;
import blang.variables.RealVariable;

/**
 * Slice Sampling implemented from 
 * Neal, Radford M. "Slice sampling." Annals of Statistics (2003): 705-741.
 * 
 * Slice sampling boils down to essentially 3 steps.
 * 
 * 1) Identify slice: To avoid underflow, log-values for unnormalizedPotentials is used to determine
 * the "current point". That is, take g(x) = log(f(x)) where f(x) is the unnormalized 
 * potentials. Aux. variable of interest is then: 
 * 
 * z = log(y) = g(x0) - e, e ~ exp(1), 
 * 
 * with slice defined as S = {x : z \leq g(x)}. See p712 for additional discussion. 
 * 
 * 2) Identify interval: Doubling procedure is used to locate an interval around the current point 
 * (faster than stepping-out when w parameter unknown). See Fig.4 p715 for alg.
 * 
 * 3) Draw new point x1 from interval: Sampling from the interval found in 2) via shrinking procedure 
 * for the interval (Fig.5 p716) with an additional check since the doubling procedure is used (Fig.6 p717). 
 * 
 * 
 * WARNING: Require re-evaluation of the likelihood for all connected factors many times! 
 * 
 * @author Sean Jewell (jewellsean@gmail.com)
 *
 * Created on Mar 4, 2015
 */

public class RealVariableSliceMove extends NodeMove
{

    /**
     * The real variable being resampled.
     * Automatically filled in via reflection.
     */
    @SampledVariable RealVariable variable;
    
    /**
     * The factors connected to this variable.
     * Automatically filled in via reflection.
     */
    @ConnectedFactor List<Factor> connectedFactors;
    
    public final double SLICE_SIZE = 2;
    public final int MAX_SLICE_SIZE = 4; // max size of slice is 2^(MAX_SLICE_SIZE) * SLICE_SIZE
    
    @Override
    public void execute(Random rand)
    {
        briefj.BriefLog.warnOnce("Basic slice method not yet fully vetted! In particular, need to tune slice parameter.");
        double originalValue = variable.getValue(); 
        double originalLogUnnormalizedPotential = computeLogUnnormalizedPotentials(originalValue);
        double auxVariable = originalLogUnnormalizedPotential + Math.log(rand.nextDouble());
        Pair<Double, Double> interval = doubleInterval(rand, auxVariable, originalValue);
        double newValue = shrinkingSampling(rand, auxVariable, originalValue, interval);
        variable.setValue(newValue);
    }

    private Pair<Double, Double> doubleInterval(Random rand, double auxVariable, double originalValue)
    {
        double L = originalValue - SLICE_SIZE * rand.nextDouble();
        double R = L + SLICE_SIZE; 
        int K = MAX_SLICE_SIZE;
        
        double leftLogUnnormalizedPotential = computeLogUnnormalizedPotentials(L);
        double rightLogUnnormalizedPotential = computeLogUnnormalizedPotentials(R);
        
        while (K > 0 && (auxVariable < leftLogUnnormalizedPotential || auxVariable < rightLogUnnormalizedPotential))
        {
            if(rand.nextDouble() < 0.5) 
            {
                L = L - (R - L);
                leftLogUnnormalizedPotential = computeLogUnnormalizedPotentials(L);
            }
            else
            {
                R = R + (R - L);
                rightLogUnnormalizedPotential = computeLogUnnormalizedPotentials(R);
            }
            
            K -= 1;
        }
        
        return new Pair<Double, Double>(L, R);
    }
    
    @SuppressWarnings("unused")
    private double shrinkingSampling(Random rand, double auxVariable, double originalValue, Pair<Double, Double> interval)
    {
        double L = interval.getFirst();
        double R = interval.getSecond();
        
        while(true)
        {
            double proposed = L + rand.nextDouble() * (R - L);
            if (auxVariable < computeLogUnnormalizedPotentials(proposed) && acceptableValue(auxVariable, originalValue, interval, proposed))
            {
//                System.out.println("---------------------------------------------");
//                System.out.println("x0 <- " + originalValue);
//                System.out.println("aux <- exp(" + auxVariable + ")");
//                System.out.println("L <- " + interval.getFirst());
//                System.out.println("R <- " + interval.getSecond());
//                System.out.println("proposed <- " + proposed);
                return proposed;
            }
            
            if (proposed < originalValue)
            {
                L = proposed; 
            }
            else
            {
                R = proposed; 
            }
        }
    }
    
    private boolean acceptableValue(double auxVariable, double originalValue, Pair<Double, Double> interval, double proposed)
    {
        double L = interval.getFirst();
        double R = interval.getSecond();
        boolean differ = false; 
        double leftLogUnnormalizedPotential = computeLogUnnormalizedPotentials(L);
        double rightLogUnnormalizedPotential = computeLogUnnormalizedPotentials(R);
        
        while( (R - L) > 1.1 * SLICE_SIZE)
        {
            double M = (L + R) / 2; 
            if ((originalValue < M && proposed >= M) || (originalValue >= M && proposed < M))
            {
                differ = true;
            }
        
            if (proposed < M)
            {
                R = M;
                rightLogUnnormalizedPotential = computeLogUnnormalizedPotentials(R);
            }
            else
            {
                L = M;
                leftLogUnnormalizedPotential = computeLogUnnormalizedPotentials(L); 
            }
            
            if (differ && proposed >= leftLogUnnormalizedPotential && proposed >= rightLogUnnormalizedPotential)
            {
                return false;
            }           
        }
        return true;
    }
    
    /**
     * Sets the variable to the given value, then compute the unnormalized
     * density of the relevant factors.
     * WARNING: should be used with care as it does not set back the original 
     * value.
     * @param value
     * @return
     */
    private double computeLogUnnormalizedPotentials(double value)
    {
      variable.setValue(value);
      double result = 0.0;
      for (Factor f : connectedFactors)
        result += f.logDensity();
      return result;
    }

    @Override
    public String toString()
    {
      return "RealVariableSliceMove [variable=" + variable + "]";
    }
    
    
    
}
