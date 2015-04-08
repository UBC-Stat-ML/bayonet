package bayonet.smc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jgrapht.UndirectedGraph;
import org.junit.Assert;
import org.junit.Test;

import bayonet.distributions.Multinomial;
import bayonet.graphs.GraphUtils;
import bayonet.marginal.DiscreteFactorGraph;
import bayonet.marginal.algo.SumProduct;
import bayonet.smc.StreamingBootstrapFilter.LatentSimulator;
import bayonet.smc.StreamingBootstrapFilter.ObservationDensity;



public class TestStreamingBootstrap
{
  private static final int len = 10;
  
  private static final double [][] transitionPrs = new double[][]{{0.8,0.15,0.05},{0.0,0.95,0.05},{0.15,0.15,0.7}};
  private static final double [][] emissionPrs = transitionPrs;
  private static final double [] initialPrs = new double[]{0.25, 0.25, 0.5};

  private static LatentSimulator<Integer> transitionDensity = new LatentSimulator<Integer>() {

    @Override
    public Integer sampleInitial(Random random)
    {
      return Multinomial.sampleMultinomial(random, initialPrs); 
    }

    @Override
    public Integer sampleForwardTransition(Random random, Integer state)
    {
      return Multinomial.sampleMultinomial(random, transitionPrs[state]);
    }
  };

  private static ObservationDensity<Integer, Integer> observationDensity = new ObservationDensity<Integer, Integer>() {

    @Override
    public double logDensity(Integer latent, Integer emission)
    {
      return Math.log(emissionPrs[latent][emission]);
    }
  };
  
  @Test
  public void test()
  {
    Random random = new Random(1);
    
    // generate data
    List<Integer> observations = generateData(random);
    
    // build discrete HMM
    DiscreteFactorGraph<Integer> dfg = createHMM(observations);
    
    // compute truth
    SumProduct<Integer> sp = new SumProduct<>(dfg);
    double truth = sp.logNormalization();
    System.out.println("truth = " + truth);
    
    // check approximation
    StreamingBootstrapFilter<Integer, Integer> lbf = new StreamingBootstrapFilter<Integer, Integer>(transitionDensity , observationDensity , observations);
    
    lbf.options.maxNumberOfVirtualParticles = 100000;
    lbf.options.numberOfConcreteParticles = 1000;
    
    double approx = lbf.sample();
    System.out.println("approx = " + approx);
    
    double relativeError = Math.abs((truth - approx) / truth);
    System.out.println("relativeError = " + relativeError);
    
    Assert.assertTrue(relativeError < 0.01);
    
//    for (int i = 0; i < 8; i++)
//    {
//      System.out.println(lbf.sample());
//      lbf.options.maxNumberOfVirtualParticles *= 10;
//      lbf.options.numberOfConcreteParticles *= 10;
//    }
  }

  private static List<Integer> generateData(Random random)
  {
    List<Integer> result = new ArrayList<>();
    int latent = transitionDensity.sampleInitial(random);
    result.add(Multinomial.sampleMultinomial(random, emissionPrs[latent]));
    for (int i = 1; i < len; i++)
    {
      latent = transitionDensity.sampleForwardTransition(random, latent);
      result.add(Multinomial.sampleMultinomial(random, emissionPrs[latent]));
    }
    return result;
  }

  private static DiscreteFactorGraph<Integer> createHMM(List<Integer> observations)
  {
    UndirectedGraph<Integer, ?> topology = GraphUtils.createChainTopology(len);
    DiscreteFactorGraph<Integer> result = new DiscreteFactorGraph<Integer>(topology);
    
    // initial distribution
    result.setUnary(0, new double[][]{initialPrs});
    
    // transition
    for (int i = 0; i < len-1; i++)
      result.setBinary(i, i+1, transitionPrs);
    
    // observations
    for (int i = 0; i < len; i++)
    {
      int currentObs = observations.get(i);
      double [] curEmissionPrs = new double[initialPrs.length];
      for (int s = 0; s < initialPrs.length; s++)
        curEmissionPrs[s] = emissionPrs[s][currentObs];
      result.unaryTimesEqual(i, new double[][]{curEmissionPrs});
    }
    
    return result;
  }
}
