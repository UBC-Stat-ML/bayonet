package bayonet.distributions;

import java.util.Random;

import bayonet.math.SpecialFunctions;
import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.ProbabilitySimplex;
import blang.variables.RealVariable;
import blang.variables.RealVector;

public class Dirichlet<P extends Dirichlet.Parameters> implements GenerativeFactor 
{
	@FactorArgument(makeStochastic=true)
	public final ProbabilitySimplex realization; // NOTE: the realization of a Dirichlet distribution is a Multinomial distribution
	@FactorComponent
	public final P parameters; 

	public Dirichlet(ProbabilitySimplex realization, P parameters)
	{
		this.realization = realization;
		this.parameters = parameters;
	}
	
	public static interface Parameters
	{
		/**
		 * get alpha_k
		 * @param k
		 * @return
		 */
		public RealVariable getComponent(int k);
		public int getDim();
		public double [] getAlphas();
	}
	
	public static class SymmetricParameterization implements Parameters
	{
		@FactorArgument private final RealVariable alpha;
		private int dim;
		
		public SymmetricParameterization(int dim, RealVariable alpha)
		{
			this.dim = dim;
			this.alpha = alpha;
		}
		
		@Override
		public int getDim()
		{
			return dim;
		}
		
		@Override
		public RealVariable getComponent(int k)
		{
			return alpha;
		}

		@Override
		public double [] getAlphas()
		{
			return RealVector.rep(dim, alpha.getValue()).getVector();
		}
	}

	@Override
	public double logDensity() 
	{
		return logDensity(realization, parameters.getAlphas());
	}

	@Override
	public void generate(Random random) 
	{
		realization.setVector(generate(random, parameters.getAlphas()));
	}

	public static double logDensity(ProbabilitySimplex realization, double [] alphas)
	{
		double sumOfAlpha = 0.0;
		double logNorm = 0.0;
		double logDensity = 0.0;
		int dim = alphas.length;
		double [] probs = realization.getVector();
		for (int d = 0; d < dim; d++)
		{
			sumOfAlpha += alphas[d];
			logNorm += SpecialFunctions.lnGamma(alphas[d]);
			
			logDensity += (alphas[d] - 1) * Math.log(probs[d]);
		}
		
		// compute log(B(alpha))
		logNorm = logNorm - SpecialFunctions.lnGamma(sumOfAlpha);
		logDensity = logDensity - logNorm;
		return logDensity;
	}
	
	public static double [] generate(Random random, double [] alphas)
	{
		int dim = alphas.length;
		double [] y = new double[dim];
		double sum = 0.0;
		for (int d = 0; d < dim; d++)
		{
			y[d] = Gamma.generate(random, alphas[d], 1.0);
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
	public static Dirichlet<SymmetricParameterization> on(ProbabilitySimplex realization)
	{
		SymmetricParameterization parameters = new SymmetricParameterization(realization.getDim(), RealVariable.real(1.0));
		return new Dirichlet<SymmetricParameterization>(realization, parameters);
	}

	public Dirichlet<SymmetricParameterization> with(SymmetricParameterization parameters)
	{
		return new Dirichlet<SymmetricParameterization>(this.realization, parameters);
	}	
	
	public static void main(String [] args)
	{
		// do some simple testing (sanity checks)
		double [] probs = new double[]{0.3, 0.4, 0.3};
		ProbabilitySimplex realization = new ProbabilitySimplex(probs); 
		Dirichlet<SymmetricParameterization> dir = Dirichlet.on(realization);
		double [] alphas = dir.parameters.getAlphas();
		String str = "( ";
		for (double alpha : alphas)
		{
			str += alpha + " ";
		}
		str += ")";
		System.out.println("alpha=" + str); // should be 1.0, 1.0, 1.0
		
		double logDensity = dir.logDensity();
		System.out.println(logDensity); // should be log(2) = 0.693147...
		
		// passes! Now do more formal tests (check stationarity)
	}

}
