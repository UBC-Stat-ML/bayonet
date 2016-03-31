package blang.core;

import java.util.Collection;
import java.util.Random;



public interface Sampler
{
  /*
   * Todo: need facilities for
   *  - command line options
   *  - logging 
   *      - summary stats such as acceptance rate
   *      - samples
   *  - adaptation
   *  - bailing out
   *  - annealing
   *      - remove -Inf in support
   *      - exponents for AIS/parallel tempering/simulated annealing
   *  - return an order of magnitude of the number of FLOPS required
   */
  
  public void execute(Random rand);
  
  public static @interface SetFromFactorGraph {}
  
  public static abstract class MHSampler implements Sampler
  {
//    @SetFromFactorGraph
//    private FactorProduct neighbors;
    
    public void execute(Random random)
    {
//      // record likelihood before
//      final double logBefore = neighbors.logLikelihood();
//      Callback callback = new Callback()
//      {
//        private Double proposalLogRatio = null;
//        @Override
//        public void setProposalLogRatio(double logRatio)
//        {
//          this.proposalLogRatio = logRatio;
//        }
//        @Override
//        public boolean sampleAcceptance()
//        {
//          final double logAfter = neighbors.logLikelihood();
//          final double ratio = Math.exp(proposalLogRatio + logAfter - logBefore);
//          return random.nextDouble() < ratio;
//        }
//      };
//      propose(random, callback);
    }
    
    public abstract void propose(Random random, Callback callback);
    
    public static interface Callback
    {
      public void setProposalLogRatio(double logRatio);
      public boolean sampleAcceptance();
    }
    
//    public static class FactorProduct
//    {
//      private final Collection<? extends LogScaleFactor> numericFactors;
//      private final Collection<? extends SupportFactor> supportFactors;
//      public FactorProduct(
//          Collection<? extends LogScaleFactor> numericFactors,
//          Collection<? extends SupportFactor> supportFactors)
//      {
//        this.numericFactors = numericFactors;
//        this.supportFactors = supportFactors;
//      }
//      public double logLikelihood()
//      {
//        for (SupportFactor support : supportFactors)
//          if (!support.isOne())
//            return Double.NEGATIVE_INFINITY;
//        
//        double sum = 0.0;
//        for (LogScaleFactor numericFactor : numericFactors)
//          sum += numericFactor.logDensity();
//        
//        return sum;
//      }
//    }
  }
}
