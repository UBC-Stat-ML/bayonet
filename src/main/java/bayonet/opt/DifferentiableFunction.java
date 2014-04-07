package bayonet.opt;

/**
 */
public interface DifferentiableFunction extends Function {
  double[] derivativeAt(double[] x);
}
