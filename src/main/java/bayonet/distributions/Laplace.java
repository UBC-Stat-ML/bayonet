package bayonet.distributions;

import static blang.variables.RealVariable.real;

import java.util.Random;


import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.GenerativeFactor;
import blang.variables.RealVariable;

/**
 * Continius Laplace priors with mean Location and Scale parameter Scale.
 * 
 *  P is the type of parametrization.
 *  
 * @author Sohrab Salehi (sohrab.salehi@gmail.com)
 *
 */
public class Laplace<P extends Laplace.Parameters> implements GenerativeFactor, UnivariateRealDistribution
{

	/**
	 * The variable on which this density is defined on.
	 */
	@FactorArgument(makeStochastic = true)
	public final RealVariable realization;


	/**
	 * The parameter of this Laplace density.
	 */
	@FactorComponent
	public final P parameters;


	/**
	 * The parameters are the location parameter and the scale.
	 */
	public static interface Parameters
	{
		/**
		 * @return Mean (location) of the distribution.
		 */
		public double getLocation();

		/**
		 * @return Scale (diversity) of the distribution
		 */
		public double getScale();
	}

	public Laplace(RealVariable variable, P param)
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
		// TODO consider all the impossible/unallowed values
		// the support is (-inf, +inf) therefore there's nothing impossible here
		double x = realization.getValue();
		double mean = parameters.getLocation();
		double beta = parameters.getScale();
		double finalValue = .5 + .5 * Math.signum(mean-beta)*(1-Math.exp(-1/beta * Math.abs(x-mean))); 
		return Math.log(finalValue);
	}

	/**
	 * Uses the current assignment of the parameters to sample a new 
	 * random variable disributed according to Laplace distribution. Write this value in 
	 * the field variable.
	 * 
	 * @param random The source of pseudo-randomness.
	 */
	@Override
	public void generate(Random random) {
		realization.setValue(generate(random, parameters.getLocation(), parameters.getScale()));		
	}

	/**
	 * The identity parameterization.
	 */
	public static class LocationScaleParameterization implements Parameters
	{
		/**
		 * The location of the distribution.
		 */
		@FactorArgument
		public final RealVariable location;

		/**
		 * The scale of the dstribution.
		 */
		@FactorArgument
		public final RealVariable scale;

		/**
		 * 
		 * @param location
		 * @param scale
		 */
		public LocationScaleParameterization(RealVariable location, RealVariable scale)
		{
			this.location = location;
			this.scale= scale;
		}

		/**
		 * @return The location (mean).
		 */
		@Override
		public double getLocation()
		{
			return location.getValue();
		}

		/**
		 * @return The scale.
		 */
		@Override
		public double getScale()
		{
			return scale.getValue();
		}
	}

	@Override
	public String toString()
	{
		return "Laplace";
	}
	
	/* Static versions of the functionalities of this class */
	
	/**
	 * Simulate a laplace random variable with given location and scale.
	 * @param rand The source of pseudo-randomness
	 * @param location The mean (location) of the distribution
	 * @param scale The beta (scale, diversity) of the distribution
	 * @return
	 */
	public static double generate(Random rand, double location, double scale)
	{
		double u = Uniform.generate(rand, -.5, .5);
		if (scale < 0)
			throw new RuntimeException();
		return location - scale * Math.signum(u) * Math.log(1-2 * Math.abs(u));
	}
	
	/* Syntactic sugar/method chaining */

	public static Laplace<LocationScaleParameterization> on(RealVariable variable)
	{
		return new Laplace<LocationScaleParameterization>(variable, new LocationScaleParameterization(real(0), real(1)));
	}

	public Laplace<LocationScaleParameterization> withParameters(double location, double scale) 
	{
		return new Laplace<LocationScaleParameterization>(realization, new LocationScaleParameterization(real(location), real(scale)));
	}

	public Laplace<LocationScaleParameterization> withParameters(RealVariable location, RealVariable scale) 
	{
		return new Laplace<LocationScaleParameterization>(realization, new LocationScaleParameterization((location), (scale)));
	}

	@Override
	public RealVariable getRealization() {
		return realization;
	}
}
