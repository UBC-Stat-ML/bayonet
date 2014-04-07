package bayonet.opt;

/**
 */
public interface Function {
  int dimension();
  double valueAt(double[] x);
}
