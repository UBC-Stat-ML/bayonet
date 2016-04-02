package bayonet.distributions;

import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.LogScaleFactor;
import blang.variables.RealVariable;
import blang.variables.ReadRealVariableData;

public class UnivariateRealData<D extends UnivariateRealDistribution> implements LogScaleFactor
{
	@FactorArgument(makeStochastic = true)
	public final ReadRealVariableData data;

	@FactorComponent
	public final D distFamily;


	public UnivariateRealData(ReadRealVariableData data, D distFamily) {
		this.data = data;
		this.distFamily = distFamily;
	}

	@Override
	public double logDensity() {
		double logLikelihood = 0.0; 
		for (RealVariable value : data.getData().values())
		{
			distFamily.getRealization().setValue(value.getValue());
			logLikelihood += distFamily.logDensity();
		}

		return logLikelihood;
	}
}
