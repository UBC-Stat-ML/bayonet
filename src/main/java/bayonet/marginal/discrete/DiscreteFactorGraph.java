package bayonet.marginal.discrete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ejml.simple.SimpleMatrix;
import org.jgrapht.UndirectedGraph;

import bayonet.marginal.BaseFactorGraph;
import bayonet.marginal.BinaryFactor;
import bayonet.marginal.FactorOperation;
import bayonet.marginal.UnaryFactor;

import com.google.common.collect.Lists;



public class DiscreteFactorGraph<V> extends BaseFactorGraph<V>
{
  public DiscreteFactorGraph(UndirectedGraph<V, ?> topology)
  {
    super(topology);
  }

  @Override
  public FactorOperation<V> marginalizationOperation()
  {
    return discreteFactorGraphOperations;
  }
  
  public void setBinary(V mNode, V oNode, SimpleMatrix m2oPotentials)
  { 
    int nM = m2oPotentials.numRows();
    int nO = m2oPotentials.numCols();
    
    setBinary(mNode, oNode, new DiscreteBinaryFactor<V>(m2oPotentials.transpose().getMatrix().data, mNode, oNode, nM, nO));
    setBinary(oNode, mNode, new DiscreteBinaryFactor<V>(m2oPotentials.getMatrix().data, oNode, mNode, nO, nM));
  }
  
  public void setUnary(V node, double [] values)
  {
    setUnaries(node, new SimpleMatrix(1, values.length, true, values));
  }
  
  private int nSites = -1;
  public void setUnaries(V node, SimpleMatrix site2ValuePotentials)
  {
    checkNSites(site2ValuePotentials.numRows());
    setUnary(node, createUnary(node, site2ValuePotentials));
  }
  
  public static <V> UnaryFactor<V> createUnary(V node, SimpleMatrix site2ValuePotentials)
  {
    return new DiscreteUnaryFactor<V>(node, site2ValuePotentials.getMatrix().data, new int[site2ValuePotentials.numRows()], site2ValuePotentials.numCols());
  }
  
  @SuppressWarnings("unchecked")
  public void unariesTimesEqual(V node, SimpleMatrix site2ValuePotentials)
  {
    checkNSites(site2ValuePotentials.numRows());
    DiscreteUnaryFactor<V> newOne = new DiscreteUnaryFactor<V>(node, site2ValuePotentials.getMatrix().data, new int[site2ValuePotentials.numRows()], site2ValuePotentials.numCols());
    DiscreteUnaryFactor<V> oldOne = (DiscreteUnaryFactor<V>) unaries.get(node);
    if (oldOne == null)
      unaries.put(node, newOne);
    else
      unaries.put(node, discreteFactorGraphOperations.pointwiseProduct(Arrays.asList(newOne, oldOne)));
  }
  
  private void checkNSites(int tested)
  {
    if (nSites == -1)
      nSites = tested;
    else if (nSites != tested)
      throw new RuntimeException();
  }

  private final FactorOperation<V> discreteFactorGraphOperations = new FactorOperation<V>() 
  {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public UnaryFactor<V> pointwiseProduct(final List<? extends UnaryFactor<V>> unaries)
    {
      final int nFactors = unaries.size();
      final DiscreteUnaryFactor [] cast = new DiscreteUnaryFactor[nFactors];
      for (int factorIndex = 0; factorIndex < nFactors; factorIndex++)
        cast[factorIndex] = (DiscreteUnaryFactor) unaries.get(factorIndex);
      
      final int nSites = cast[0].nSites();
      final int nVariableValues = cast[0].nVariableValues();
      
      final int [] newScales = new int[nSites];
      final double [] newMatrix = new double[nSites * nVariableValues]; //new SimpleMatrix(nSites, nVariableValues);
      
      for (int site = 0; site < nSites; site++)
      {
        int sumScales = 0;
        for (int factor = 0; factor < nFactors; factor++)
          sumScales += cast[factor].scales[site];
        newScales[site] = sumScales;
      }
      
      for (int site = 0; site < nSites; site++)
        for (int varValue = 0; varValue < nVariableValues; varValue++)
        {
          double prodUnnorm = 1.0;
          for (int factor = 0; factor < nFactors; factor++)
            prodUnnorm *= cast[factor].get(site, varValue);
          newMatrix[nVariableValues * site + varValue] = prodUnnorm;
//          newMatrix.set(site, varValue, prodUnnorm);
        }
      
      return new DiscreteUnaryFactor(cast[0].node, newMatrix, newScales, nVariableValues);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public UnaryFactor<V> marginalize(
        final BinaryFactor<V> _binary,
        final List<UnaryFactor<V>> unariesOnMarginalized)
    {
      final int maxDegree = 2;
      if (unariesOnMarginalized.size() <= maxDegree)
      {
        final int nSites = DiscreteFactorGraph.this.nSites;
        final int degree = unariesOnMarginalized.size();
        final DiscreteUnaryFactor<V> 
          dbf0 = degree >= 1 ? (DiscreteUnaryFactor) unariesOnMarginalized.get(0) : null,
          dbf1 = degree == 2 ? (DiscreteUnaryFactor) unariesOnMarginalized.get(1) : null;
          
        final int [] 
          scales0 = degree >= 1 ? dbf0.scales : null,
          scales1 = degree == 2 ? dbf1.scales : null;
        
        final DiscreteBinaryFactor<V> binary = (DiscreteBinaryFactor) _binary;
//        final double [] o2mPotentials = binary.o2mPotentials;
        
        final double [] newMatrix = new double[nSites * binary.nOtherVariableValues()]; //new SimpleMatrix(nSites, binary.nOtherVariableValues());
        final int [] newScales = new int[nSites];
        
        final int nOtherValues = binary.nOtherVariableValues();
        final int nMarginalizedValues = binary.nMarginalizedVariableValues();
        
        // Warning: this part of the code is less readable and easy to maintain
        // because it is in the inner loop of phylogenetic computations
             if (degree == 0) ;
        else if (degree == 1) for (int site = 0; site < nSites; site++) newScales[site] = scales0[site];
        else                  for (int site = 0; site < nSites; site++) newScales[site] = scales0[site] + scales1[site];
             
         if (degree == 0) 
           for (int site = 0; site < nSites; site++)
             for (int otherIndex = 0; otherIndex < nOtherValues; otherIndex++)
             {
               double sum = 0.0;
               for (int margIndex = 0; margIndex < nMarginalizedValues; margIndex++)
                 sum += binary.get(otherIndex, margIndex); //o2mPotentials.get(otherIndex, margIndex);
               newMatrix[site * nOtherValues + otherIndex] = sum; //newMatrix.set(site, otherIndex, sum);
             }
         else if (degree == 1) 
           for (int site = 0; site < nSites; site++)
             for (int otherIndex = 0; otherIndex < nOtherValues; otherIndex++)
             {
               double sum = 0.0;
               for (int margIndex = 0; margIndex < nMarginalizedValues; margIndex++)
                 sum += binary.get(otherIndex, margIndex) //o2mPotentials.get(otherIndex, margIndex);
                       * dbf0.get(site, margIndex); //site2valuePotentials0.get(site, margIndex);
               newMatrix[site * nOtherValues + otherIndex] = sum; //newMatrix.set(site, otherIndex, sum);
             }
         else 
           for (int site = 0; site < nSites; site++)
             for (int otherIndex = 0; otherIndex < nOtherValues; otherIndex++)
             {
               double sum = 0.0;
               for (int margIndex = 0; margIndex < nMarginalizedValues; margIndex++)
                 sum += binary.get(otherIndex, margIndex) //o2mPotentials.get(otherIndex, margIndex);
                       * dbf0.get(site, margIndex) //site2valuePotentials0.get(site, margIndex);
                       * dbf1.get(site, margIndex); //site2valuePotentials1.get(site, margIndex);
               newMatrix[site * nOtherValues + otherIndex] = sum; //newMatrix.set(site, otherIndex, sum);
             }
        
        return new DiscreteUnaryFactor<V>(binary.otherNode(), newMatrix, newScales, nOtherValues);
      }
      else
        return marginalizeOnReducedUnariesDegree(this, maxDegree, _binary, unariesOnMarginalized);
    }
  };
  
  private static <V> UnaryFactor<V> marginalizeOnReducedUnariesDegree(
      FactorOperation<V> operation, 
      int maxDegree,
      final BinaryFactor<V> binary,
      final List<UnaryFactor<V>> unariesOnMarginalized)
  {
    if (unariesOnMarginalized.size() <= maxDegree)
      throw new RuntimeException();
    ArrayList<UnaryFactor<V>> reducedList = Lists.newArrayList();
    // first items stay as is
    for (int i = 0; i < maxDegree - 1; i++)
      reducedList.add(unariesOnMarginalized.get(i));
    // last one is obtained by reducing the rest
    reducedList.add(operation.pointwiseProduct(unariesOnMarginalized.subList(maxDegree - 1,  unariesOnMarginalized.size())));
    if (reducedList.size() != maxDegree)
      throw new RuntimeException();
    return operation.marginalize(binary, reducedList);
  }
  
  private static final class DiscreteBinaryFactor<V> implements BinaryFactor<V>
  {
    private final double [] o2mPotentials;
    private final V m, o;
    private final int nM, nO;
    
    private DiscreteBinaryFactor(double [] o2mPotentials, V m, V o, int nM, int nO)
    {
      this.nO = nO;
      this.nM = nM;
      if (nO * nM != o2mPotentials.length)
        throw new RuntimeException();
      this.o2mPotentials = o2mPotentials;
      this.m = m;
      this.o = o;
    }
    
    private double get(int oIndex, int nIndex)
    {
      return o2mPotentials[oIndex * nO + nIndex];
    }

    private int nOtherVariableValues()
    {
      return nO;
    }
    
    private int nMarginalizedVariableValues()
    {
      return nM;
    }

    @Override public V marginalizedNode() { return m; }
    @Override public V otherNode() { return o; }
  }
  
  private static final class DiscreteUnaryFactor<V> implements UnaryFactor<V>
  {
    private static final int MIN_SCALE = -50;
    private static final double UNDERFLOW_THRESHOLD = Math.exp(MIN_SCALE);
    private static final double UNDERFLOW_THRESHOLD_INVERSE = Math.exp(-MIN_SCALE);
    
//    private static final int MAX_SCALE = +50;
//    private static final double OVERFLOW_THRESHOLD = Math.exp(MAX_SCALE);
//    private static final double OVERFLOW_THRESHOLD_INVERSE = Math.exp(-MAX_SCALE);
    
    private final double [] site2valuePotentials;
    private final int [] scales; // base e
    private final double logNormalization;
    private final V node;
    private final int nSites;
    private final int nVariableValues;
    
    private DiscreteUnaryFactor(V node, double [] site2valuePotentials, int [] scales, int nVariableValues)
    {
      this.nSites = scales.length;
      this.nVariableValues = nVariableValues;
      if (site2valuePotentials.length != nSites * nVariableValues)
        throw new RuntimeException();
      this.node = node;
      this.site2valuePotentials = site2valuePotentials;
      this.scales = scales;
      
      double logNorm = 0.0;
      double tempProd = 1.0;
      for (int site = 0; site < nSites(); site++)
      {
        double currentNorm = norm(site);
        final int currentScale = scales[site];
        
        // update normalization
        logNorm = logNorm - currentScale;
        tempProd *= currentNorm;
        
        while (tempProd > 0 && tempProd < UNDERFLOW_THRESHOLD)
        {
          tempProd *= UNDERFLOW_THRESHOLD_INVERSE;
          logNorm += MIN_SCALE;
        }
        
        // rescale if needed
        while (currentNorm > 0 && currentNorm < UNDERFLOW_THRESHOLD)
        {
          scales[site] = scales[site] - MIN_SCALE;
          for (int valueIndex = 0; valueIndex < nVariableValues(); valueIndex++)
            set(site, valueIndex, get(site,valueIndex) * UNDERFLOW_THRESHOLD_INVERSE);
          currentNorm = norm(site); 
        }
        
//        while (tempProd > OVERFLOW_THRESHOLD)
//        {
//          tempProd *= OVERFLOW_THRESHOLD_INVERSE;
//          logNorm += MAX_SCALE;
//        }
//        
//        // rescale if needed
//        while (currentNorm > 0 && currentNorm > OVERFLOW_THRESHOLD)
//        {
//          scales[site] = scales[site] - MIN_SCALE;
//          for (int valueIndex = 0; valueIndex < nVariableValues(); valueIndex++)
//            set(site, valueIndex, get(site,valueIndex) * UNDERFLOW_THRESHOLD_INVERSE);
//          currentNorm = norm(site); 
//        }
      }
      logNorm += Math.log(tempProd);
      
      this.logNormalization = logNorm;
    }
    
    private double get(final int site, final int valueIndex)
    {
      return site2valuePotentials[site * nVariableValues + valueIndex];
    }
    
    private void set(final int site, final int valueIndex, final double value)
    {
      site2valuePotentials[site * nVariableValues + valueIndex] = value;
    }

    private int nVariableValues()
    {
      return nVariableValues;
    }

    @Override public V connectedVariable() { return node; }
    
    private double logNormalization(int site)
    {
      return Math.log(norm(site)) - scales[site];
    }
    
    private double norm(int site)
    {
      double sum = 0.0;
      for (int valueIndex = 0; valueIndex < nVariableValues; valueIndex++)
        sum += get(site, valueIndex);
      return sum;
    }

    @Override
    public double logNormalization()
    {
      return logNormalization;
    }

    private int nSites()
    {
      return nSites;
    }
    
  }
  
//  public static void main(String [] args)
//  {
//    PhylogeneticHeldoutDatasetOptions phyloOptions = new PhylogeneticHeldoutDatasetOptions();
//    phyloOptions.alignmentFile = "/Users/bouchard/Documents/data/utcs/23S.E/R0/cleaned.alignment.fasta";
//    phyloOptions.treeFile = "/Users/bouchard/Documents/data/utcs/23S.E.raxml.nwk"; 
//    phyloOptions.maxNSites = 1;
//    phyloOptions.minFractionObserved = 0.9;
//    PhylogeneticHeldoutDataset phyloData = PhylogeneticHeldoutDataset.loadData(phyloOptions);
//    SimpleCTMC ctmc = new SimpleCTMC(RateMatrixLoader.k2p(), 1);
//    GMFct<Taxon> pots = DiscreteBP.toGraphicalModel(phyloData.rootedTree, ctmc, phyloData.obs, 0);
//    
//    DiscreteFactorGraph<Taxon> converted = fromGM(pots, 10000);
//    
//    for (int i = 0; i < 100; i++)
//    {
//      long start = System.currentTimeMillis();
//      TreeSumProd<Taxon> tsp = new TreeSumProd<Taxon>(
//          pots);
////      
////      
//      System.out.println("method1 = " + tsp.logZ());
//      System.out.println("time = " + (System.currentTimeMillis()-start));
//      System.out.println();
////      
//      
//      
//      start = System.currentTimeMillis();
//      SumProduct<Taxon> sp = new SumProduct<Taxon>(converted);
//      System.out.println("method2 = " + sp.computeMarginal(new Taxon("internal66")).logNormalization());
//      System.out.println("time = " + (System.currentTimeMillis()-start));
//      System.out.println();
//    }
//  }
  
//  public static <V> DiscreteFactorGraph<V> fromGM(GMFct<V> model, int nSites)
//  {
//    // create graph
//    UndirectedGraph<V, DefaultEdge> ug = new SimpleGraph<V, DefaultEdge>(DefaultEdge.class);
//    DiscreteFactorGraph<V> newFG = new DiscreteFactorGraph<V>(ug);
//    
//    // add vertex
//    for (V vertex : model.graph().vertexSet())
//    {
//      ug.addVertex(vertex);
//     
//      int nValues = model.nStates(vertex);
//      SimpleMatrix newMatrix = new SimpleMatrix(nSites, nValues);
//      
//      boolean shouldAdd = false;
//      for (int s = 0; s < nSites; s++)
//        for (int valueIndex = 0; valueIndex < nValues; valueIndex++)
//        {
//          double value = model.get(vertex, valueIndex);
//          newMatrix.set(s, valueIndex, value);
//          if (value != 1.0)
//            shouldAdd = true;
//        }
//      if (shouldAdd)
//        newFG.setUnaries(vertex, newMatrix);
//    }
//    
//    // add edges
//    for (UnorderedPair<V, V> e : Graphs.edgeSet(model.graph()))
//    {
//      V n0 = e.getFirst(), 
//        n1 = e.getSecond();
//      ug.addEdge(n0, n1);
//      
//      int nValues0 = model.nStates(n0),
//          nValues1 = model.nStates(n1);
//      
//      SimpleMatrix trans = new SimpleMatrix(nValues0, nValues1);
//      
//      for (int s0 = 0; s0 < nValues0; s0++)
//        for (int s1 = 0; s1 < nValues1; s1++)
//          trans.set(s0, s1, model.get(n0, n1, s0, s1));
//      
//      newFG.setBinary(n0, n1, trans);
//    }
//    
//    return newFG;
//  }
  
}
