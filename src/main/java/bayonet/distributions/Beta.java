package bayonet.distributions;


import static blang.variables.RealVariable.real;

import java.util.Random;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.util.FastMath;

import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.RealVariable;

// TODO: there's a newer version of Apache commons Math3 out there, consider migrating to that


/**
 * Continius Beta priors with mean Alpha and Beta parameter Beta.
 * 
 *  P is the type of parametrization.
 *  
 * @author Sohrab Salehi (sohrab.salehi@gmail.com)
 *
 */
public class Beta<P extends Beta.Parameters> implements GenerativeFactor, UnivariateRealDistribution
{
	/**
	 * The variable on which this density is defined on.
	 */
	@FactorArgument(makeStochastic = true)
	public final RealVariable realization;


	/**
	 * The parameter of this Beta density.
	 */
	@FactorComponent
	public final P parameters;


	/**
	 * The parameters are the two shape parameters.
	 */
	public static interface Parameters
	{
		/**
		 * @return first shape parameter (alpha) of the distribution.
		 */
		public double getAlpha();

		/**
		 * @return second shape parameter (beta) of the distribution.
		 */
		public double getBeta();
	}

	public Beta(RealVariable variable, P param)
	{
		this.realization = variable;
		this.parameters = param;
	}


	@Override
	/**
	 * @return The log of the density for the current
	 *  assignment of parameters and realization.
	 */
	public double logDensity() {
		// TODO consider all the impossible/un-allowed values
		return logDensity(realization.getValue(), parameters.getAlpha(), parameters.getBeta());
	}

	public static double logDensity(double x, double alpha, double beta)
	{
		// TODO: the latest version of Apache commons math 3.3 has a direct implementation of log density
		final BetaDistribution bd = new BetaDistribution(alpha, beta, BetaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		double density = bd.density(x);
		return FastMath.log(density); 
	}
	
	
	/**
	 * Uses the current assignment of the parameters to sample a new 
	 * random variable disributed according to Beta distribution. Write this value in 
	 * the field variable.
	 * 
	 * @param random The source of pseudo-randomness.
	 */
	@Override
	public void generate(Random random) {
		realization.setValue(generate(random, parameters.getAlpha(), parameters.getBeta()));		
	}

	/**
	 * The identity parameterization.
	 */
	public static class AlphaBetaParameterization implements Parameters
	{
		/**
		 * The first shape parameter (alpha) of the distribution.
		 */
		@FactorArgument
		public final RealVariable alpha;

		/**
		 * The second shape parameter (beta) of the distribution.
		 */
		@FactorArgument
		public final RealVariable beta;

		/**
		 * 
		 * @param alpha
		 * @param beta
		 */
		public AlphaBetaParameterization(RealVariable alpha, RealVariable beta)
		{
			this.alpha = alpha;
			this.beta= beta;
		}

		/**
		 * @return The alpha, first shape parameter.
		 */
		@Override
		public double getAlpha()
		{
			return alpha.getValue();
		}

		/**
		 * @return The beta, second shape parameter.
		 */
		@Override
		public double getBeta()
		{
			return beta.getValue();
		}
	}

	@Override
	public String toString()
	{
		return "Beta";
	}

	/* Static versions of the functionalities of this class */

	/**
	 * Simulate a Beta random variable with given alpha and beta.
	 * @param rand The source of pseudo-randomness
	 * @param alpha The first shape parameter of the distribution
	 * @param beta The beta the second shape parameter of the distribution
	 * @return
	 */
	public static double generate(Random rand, double alpha, double beta)
	{
		final BetaDistribution bd = new BetaDistribution(new Random2RandomGenerator(rand), alpha, beta, BetaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		return bd.sample();
	}

	/* Syntactic sugar/method chaining */
	// TODO: what is a good default value for beta distribution
	public static Beta<AlphaBetaParameterization> on(RealVariable variable)
	{
		return new Beta<AlphaBetaParameterization>(variable, new AlphaBetaParameterization(real(2), real(2)));
	}

	public Beta<AlphaBetaParameterization> withParameters(double alpha, double beta) 
	{
		return new Beta<AlphaBetaParameterization>(realization, new AlphaBetaParameterization(real(alpha), real(beta)));
	}

	public Beta<AlphaBetaParameterization> withParameters(RealVariable alpha, RealVariable beta) 
	{
		return new Beta<AlphaBetaParameterization>(realization, new AlphaBetaParameterization((alpha), (beta)));
	}

	@Override
	public RealVariable getRealization() {
		return realization;
	}
}
