package bayonet.opt;

/**
 * @author Dan Klein
 */
public interface GradientLineSearcher {
  public double[] minimize(DifferentiableFunction function, double[] initial, double[] direction);
}
