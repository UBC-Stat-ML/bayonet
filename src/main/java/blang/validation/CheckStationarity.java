package blang.validation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.junit.Assert;

import blang.ForwardSampler;
import blang.MCMCAlgorithm;
import blang.processing.Processor;
import blang.processing.ProcessorContext;
import blang.variables.RealValued;
import briefj.BriefMaps;
import briefj.BriefStrings;
import briefj.ReflexionUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.primitives.Doubles;


/**
 * A utility to flag potential bugs in MCMC samplers, via forward 
 * sampling methods. See check() for more info.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class CheckStationarity
{
  /**
   * Compares two sets of samples. 
   * 
   * A first set is obtained 
   * by performing forward sampling only. This is done independently nIndepSamples times.
   * 
   * A second step is obtained by running the forward sampler, followed by a few 
   * rounds of mcmc (the number of rounds is specified in mcmc.options.nMCMCSweeps).
   * This is also done independently nIndepSamples times.
   * 
   * Classical Fisher framework based tests can then be applied to compare the two
   * sets of samples, which should be from the same distribution if the MCMC sampler 
   * and the forward sampler are both correct. The null hypothesis is that the code is 
   * correct.
   * 
   * Note that even when the code is correct, the check can incorrectly reject the null
   * up to uncorrectedPValueThreshold fraction of the time.
   * 
   * @param mcmc
   * @param nIndepSamples
   * @param uncorrectedPValueThreshold p value threshold for the two sample tests
   */
  public void check(MCMCAlgorithm mcmc, int nIndepSamples, double uncorrectedPValueThreshold)
  {
    Table<Object,Test,Double> testResults = testResults(mcmc, nIndepSamples, tests, false);
    // Since we do many tests (on different real valued random variable and/or processors)
    // we correct for family wise error rate.
    double bonferroniCorrected = uncorrectedPValueThreshold / testResults.rowKeySet().size();
    boolean nullRejected = false;
    for (double pValue : testResults.values())
      if (pValue < bonferroniCorrected)
        nullRejected = true;
    
    if (showDetails)
      System.out.println("Test results:\n" + BriefStrings.indent(toString(testResults)));
    
    if (nullRejected)
    {
      // check if the problem is not from the test itself
      Table<Object,Test,Double> testResultsForTwoIndepCopies = testResults(mcmc, nIndepSamples, tests, true);
      boolean testNullRejected = false;
      for (double pValue : testResultsForTwoIndepCopies.values())
        if (pValue < bonferroniCorrected)
          testNullRejected = true;
      if (testNullRejected)
        Assert.fail("There seems to be problems with the test: two indep " +
            "copies of the forward simulated dataset were rejected. Build a smaller test " +
            "with less volatility, increase the number of indep samples, remove the rank " +
            "based test (less numerically stable?) or relax the p threshold " +
            "value.");
      else
        Assert.fail("Stationary test suggests further investigations " +
          "(e.g. try one move at a time to narrow possibilities):\n" + toString(testResults));
    }
  }
  
  /**
   * If set to true, print out summary statistics on each of the two
   * set of samples being compared.
   * 
   * @param showSampleSummaryStats
   */
  public void setShowSampleSummaryStats(boolean showSampleSummaryStats)
  {
    this.showDetails = showSampleSummaryStats;
  }
  
  /**
   * 
   * @return
   */
  public List<Test> getTests()
  {
    return tests;
  }

  /**
   * 
   * @param tests
   */
  public void setTests(List<Test> tests)
  {
    this.tests = tests;
  }
  
  /**
   * 
   *
   */
  private static interface Test
  {
    public double pValue(List<Double> sample1, List<Double> sample2);
  }
  
  /**
   * 
   */
  public static final Test mannWhitneyTest = new Test() 
  {
    /**
     * 
     */
    private MannWhitneyUTest testImpl = new MannWhitneyUTest();
    
    /**
     * 
     */
    @Override
    public double pValue(List<Double> sample1, List<Double> sample2)
    {
      return testImpl.mannWhitneyUTest(Doubles.toArray(sample1), Doubles.toArray(sample2));
    }
    
    /**
     * 
     */
    @Override
    public String toString() { return "MannWhitney"; }
  };
  
  /**
   * 
   */
  public static final Test wilcoxonSignedRank = new Test() 
  {
    /**
     * 
     */
    private WilcoxonSignedRankTest testImpl = new WilcoxonSignedRankTest();
    
    /**
     * 
     */
    @Override
    public double pValue(List<Double> sample1, List<Double> sample2)
    {
      return testImpl.wilcoxonSignedRankTest(Doubles.toArray(sample1), Doubles.toArray(sample2), false);
    }
    
    /**
     * 
     */
    @Override
    public String toString() { return "WilcoxonSignedRank"; }
  };
  
  /**
   * 
   */
  public static final Test tTest = new Test()
  {
    /**
     * 
     */
    @Override
    public double pValue(List<Double> sample1, List<Double> sample2)
    {
      return TestUtils.tTest(Doubles.toArray(sample1), Doubles.toArray(sample2));
    }
    
    /**
     * 
     */
    @Override
    public String toString() { return "TTest"; }
  };
  
  /**
   * T test on the squares.
   */
  public static final Test higherMomentTTest = new Test()
  {
    /**
     * Set to higher value to test higher moments.
     */
    private int order = 2;

    /**
     * 
     */
    @Override
    public double pValue(List<Double> sample1, List<Double> sample2)
    {
      return TestUtils.tTest(raise(sample1), raise(sample2));
    }
    
    /**
     * 
     * @param sample
     * @return
     */
    private double[] raise(List<Double> sample)
    {
      double [] result = new double[sample.size()];
      for (int i = 0; i < result.length; i++)
        result[i] = Math.pow(sample.get(i), order);
      return result;
    }

    @Override
    public String toString() { return "TTestOnOrder(" + order + ")"; }
  };
  
  /**
   * 
   */
  private List<Test> tests = Lists.newArrayList(
//      mannWhitneyTest,    // note: seems numerically unstable (under/over flows?) see issue #30
      higherMomentTTest, 
//      wilcoxonSignedRank, // note: seems numerically unstable (under/over flows?) see issue #30
      tTest);
  
  /**
   * 
   */
  private boolean showDetails = false;
  
  /**
   * 
   * @param testResults
   * @return
   */
  private static String toString(Table<Object, Test, Double> testResults)
  {
    StringBuilder result = new StringBuilder();
    for (Object stat : testResults.rowKeySet())
      result.append(stat + "\n  " +  Joiner.on("\n  ").join(testResults.row(stat).entrySet()) + "\n");
    return result.toString();
  }
  
  /**
   * 
   * @param mcmc
   * @param nIndepSamples
   * @param tests
   * @param useForwardInBoth
   * @return
   */
  private  Table<Object,Test,Double> testResults(
      MCMCAlgorithm mcmc, 
      int nIndepSamples, 
      List<Test> tests,
      boolean useForwardInBoth)
  {
    Map<Object,List<Double>> 
      forwardOnly    = sample(false, mcmc, nIndepSamples),
      forwardAndPost = useForwardInBoth ? 
                         sample(false, mcmc, nIndepSamples) :
                         sample(true,  mcmc, nIndepSamples);
    
    Set<Object> keys = forwardOnly.keySet();
    if (!keys.equals(forwardAndPost.keySet()))
      throw new RuntimeException("" + keys + " vs \n " + forwardAndPost.keySet());
    
    if (showDetails)
    {
      for (Object key : keys)
      {
        System.out.println(key);
        DescriptiveStatistics stats = new DescriptiveStatistics(Doubles.toArray(forwardOnly.get(key)));
        System.out.println("  Forward only:");
        System.out.println(BriefStrings.indent(stats.toString(), "    "));
        stats = new DescriptiveStatistics(Doubles.toArray(forwardAndPost.get(key)));
        System.out.println("  Forward " + (useForwardInBoth ? "(indep repeat)" : "and post:"));
        System.out.println(BriefStrings.indent(stats.toString(), "    "));
      }
    }
    
    Table<Object,Test,Double> result = ArrayTable.create(keys, tests);
    for (Object stat : keys)  // Compute one p-value for each real valued variable or processor
      for (Test test : tests) // and for each test
      {
        final double pValue = test.pValue(forwardOnly.get(stat), forwardAndPost.get(stat));
        result.put(stat, test, pValue); 
      }
    
    return result;
  }

  /**
   * 
   * @param doPosterior
   * @param mcmc
   * @param nIndepSamples
   * @return
   */
  private static Map<Object,List<Double>> sample(boolean doPosterior, MCMCAlgorithm mcmc, int nIndepSamples)
  {
    Map<Object,List<Double>> result = Maps.newHashMap();
    ForwardSampler forwardSampler = new ForwardSampler(mcmc.model);
    for (int i = 0; i < nIndepSamples; i++)
    {
      forwardSampler.simulate(mcmc.options.random);
      if (doPosterior)
        for (int j = 0; j < mcmc.options.nMCMCSweeps; j++)
          mcmc.sampler.sweep(mcmc.options.random);
      collectStatistics(mcmc, result);
    }
    return result;
  }
  
  /**
   * Uses the random variables that can be viewed as real valued to 
   * peform the different tests. Also look for processor (functions of
   * real variables) that can be viewed as real valued.
   * @param mcmc
   * @param values
   */
  private static void collectStatistics(MCMCAlgorithm mcmc, Map<Object,List<Double>> values)
  {
    for (RealValued realValuedVariable : mcmc.model.getLatentVariables(RealValued.class))
      BriefMaps.getOrPutList(values, mcmc.model.getName(realValuedVariable)).add(realValuedVariable.getValue());
    
    for (Processor processor : mcmc.processors)
      processor.process(new ProcessorContext(mcmc.options.nMCMCSweeps - 1, mcmc.model, mcmc.options));
    
    for (RealValued realValuedProcessor : ReflexionUtils.sublistOfGivenType(mcmc.processors, RealValued.class))
      BriefMaps.getOrPutList(values, realValuedProcessor).add(realValuedProcessor.getValue());
    
  }

}
