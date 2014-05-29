package bayonet.distributions;

import java.util.Random;

import blang.annotations.FactorArgument;
import blang.factors.GenerativeFactor;
import blang.variables.ProbabilitySimplex;
import blang.variables.RealVector;

public class Dirichlet implements GenerativeFactor 
{
	@FactorArgument(makeStochastic=true)
	public final ProbabilitySimplex realization; // NOTE: the realization of a Dirichlet distribution is a Multinomial distribution
	@FactorArgument
	public final RealVector parameters;

	public Dirichlet(ProbabilitySimplex realization, RealVector parameters)
	{
		this.realization = realization;
		this.parameters = parameters;
	}

	@Override
	public double logDensity() 
	{
		return logDensity(realization, parameters);
	}

	@Override
	public void generate(Random random) 
	{
		realization.setVector(generate(random, parameters));
	}

	public static double logDensity(ProbabilitySimplex realization, RealVector parameters)
	{
		double sumOfAlpha = 0.0;
		double logNorm = 0.0;
		double logDensity = 0.0;
		int dim = parameters.getDim();
		double [] probs = realization.getVector();
		for (int d = 0; d < dim; d++)
		{
			sumOfAlpha += parameters.getComponent(d).getValue();
			logNorm += org.apache.commons.math3.special.Gamma.logGamma(parameters.getComponent(d).getValue());
			
			logDensity += (parameters.getComponent(d).getValue() - 1) * Math.log(probs[d]);
		}
		
		// compute log(B(alpha))
		logNorm = logNorm - Math.log(org.apache.commons.math3.special.Gamma.gamma(sumOfAlpha));
		logDensity = logDensity - logNorm;
		return logDensity;
	}
	
	public static double [] generate(Random random, RealVector parameters)
	{
		int dim = parameters.getDim();
		double [] y = new double[dim];
		double sum = 0.0;
		for (int d = 0; d < dim; d++)
		{
			y[d] = Gamma.generate(random, parameters.getComponent(d).getValue(), 1.0);
			sum += y[d];
		}
		
		for (int d = 0; d < dim; d++)
		{
			y[d] = y[d] / sum;
		}
		
		return y;
	}
	
	/**
	 * Return a new Dirichlet object initialized with realization and with default parameter (1.0, ..., 1.0)
	 * @param realization
	 * @return
	 */
	public static Dirichlet on(ProbabilitySimplex realization)
	{
		int K = realization.getDim();
		double [] alpha = new double[K];
		for (int k = 0; k < K; k++)
		{
			alpha[k] = 1.0;
		}
		
		return new Dirichlet(realization, new RealVector(alpha));
	}
	
	public Dirichlet with(RealVector alpha)
	{
		return new Dirichlet(realization, alpha);
	}	

}
