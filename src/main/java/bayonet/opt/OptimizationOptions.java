package bayonet.opt;
import briefj.opt.Option;




public class OptimizationOptions
{
  @Option public double regularizationStrength = 1.0;
  @Option public int maxIterations = 100;
  @Option public double tolerance = 1e-8;
}
