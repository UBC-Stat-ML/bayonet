package bayonet.factors;

import java.util.Random;

import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.jgrapht.UndirectedGraph;
import org.junit.Assert;
import org.junit.Test;

import bayonet.graphs.GraphUtils;
import bayonet.marginal.algo.SumProduct;
import bayonet.marginal.discrete.DiscreteFactorGraph;



public class SumProductTests
{
  
  @Test
  public void testOnRegularMarkov()
  {
    for (int len = 10; len < 100000; len *= 10)
    {
      DiscreteFactorGraph<Integer> markov = buildRegularMarkov(2, len);
      SumProduct<Integer> sp = new SumProduct<Integer>(markov);
      double computed = (sp.logNormalization());
      double analytic = -len * Math.log(2.0);
      Assert.assertEquals(computed, analytic, 10e-9);
    }
  }
  
  public static DiscreteFactorGraph<Integer> buildRegularMarkov(int nStates, int length)
  {
    // build topology
    UndirectedGraph<Integer, ?> topology = GraphUtils.newUndirectedGraph();
    for (int i = 0; i < length; i++)
    {
      topology.addVertex(i);
      if (i > 0)
        topology.addEdge(i-1, i);
    }
    
    // build potentials
    DiscreteFactorGraph<Integer> result = new DiscreteFactorGraph<Integer>(topology);
    for (int i = 0; i < length; i++)
    {
      // create unary
      double [][] data = new double[1][nStates];
      for (int s = 0; s < nStates; s++)
        data[0][s] = 1.0/nStates/2.0;
      result.setUnary(i, data);
      
      if (i > 0) 
      {
        // create binary
        SimpleMatrix matrix = new SimpleMatrix(nStates, nStates);
        
        for (int s1 = 0; s1 < nStates; s1++)
          for (int s2 = 0; s2 < nStates; s2++)
            matrix.set(s1, s2, 1.0);
        
        result.setBinary(i-1, i, matrix);
      }
        
    }
    
    return result;
  }
  
  public static void randomTree(
      Random rand, 
      int nLevelsToGo, 
      DiscreteFactorGraph<String> result, 
      String node)
  {
    int nChildren = 1 + rand.nextInt(3);
    for (int c = 0; c < nChildren; c++)
    {
      
    }
  }
  
//  public static DiscreteFactorGraph<Integer> buildRandomMarkov(Random rand, int nStates, int length)
//  {
//    // build topology
//    UndirectedGraph<Integer, ?> topology = GraphUtils.newUndirectedGraph();
//    for (int i = 0; i < length; i++)
//    {
//      topology.addVertex(i);
//      if (i > 0)
//        topology.addEdge(i-1, i);
//    }
//    
//    // build potentials
//    DiscreteFactorGraph<Integer> result = new DiscreteFactorGraph<Integer>(topology);
//    for (int i = 0; i < length; i++)
//    {
//      // create unary
//      double [] data = //new double[nStates];//
//        RandomMatrices.createRandom(1, nStates, 0, 1.0/nStates, rand).data;
////      for (int s = 0; s < nStates; s++)
////        data[s] = 1.0/nStates/2.0;
//      result.setUnary(i, data);
//      
//      if (i > 0) 
//      {
//        // create binary
//        SimpleMatrix matrix = //new SimpleMatrix(nStates, nStates);
//          new SimpleMatrix(RandomMatrices.createRandom(nStates, nStates, 0, 1.0/nStates, rand));
//        
//
//
//        
//        result.setBinary(i-1, i, matrix);
//      }
//        
//    }
//    
//    return result;
//  }
  
  public static void main(String [] args)
  {
    Random rand = new Random(1);
    
    int len = 8000;
    DiscreteFactorGraph<Integer> markov = buildRegularMarkov(2, len);
    long start = System.currentTimeMillis();
    SumProduct<Integer> sp = new SumProduct<Integer>(markov);
    System.out.println(sp.logNormalization());
    System.out.println(-len * Math.log(2.0));
    System.out.println("Time: " + (System.currentTimeMillis() - start)/1000.0);
  }
}
