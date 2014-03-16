package bayonet.marginal.discrete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;
import org.jgrapht.UndirectedGraph;

import bayonet.distributions.Multinomial;
import bayonet.marginal.BaseFactorGraph;
import bayonet.marginal.BinaryFactor;
import bayonet.marginal.FactorOperations;
import bayonet.marginal.Sampler;
import bayonet.marginal.UnaryFactor;

import com.google.common.collect.Lists;


/**
 * A factor graph where the nodes are discrete random variables, and the factors
 * are either unary or binary. In other words, a Markov random field or 
 * undirected graphical model.
 * 
 * This implementation additionally efficiently supports case where
 * we have several factor graphs, each with the same binary factors,
 * but with different unary factors. This arises for example in phylogenetics, 
 * where each site (location of the genome) carries different observations,
 * but the evolution is assumed to happen on the same tree.
 * We therefore use the terminology site for each of these independent 
 * sub factor graphs.
 * 
 * Note that we represent only one unary per node, but if two are needed,
 * use  unariesTimesEqual() to pointwise multiply them.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <V> A datatype used to label the variables.
 */
public class DiscreteFactorGraph<V> extends BaseFactorGraph<V>
{
  /**
   * 
   * @param topology The undirected graphical model. Note that since
   *   factor are at most binary, we represent the factor graph
   *   with just connection among the variables.
   */
  public DiscreteFactorGraph(UndirectedGraph<V, ?> topology)
  {
    super(topology);
  }
  
  /**
   * Set a binary factor. 
   * 
   * @throws RuntimeException if the binary factor already exists 
   * 
   * @param mNode One variable label
   * @param oNode Another variable label
   * @param m2oPotentials A matrix encoding the binary factor shared by all sites,
   *    where the rows index mNode's states, and columns index oNode's states
   */
  public void setBinary(V mNode, V oNode, SimpleMatrix m2oPotentials)
  { 
    int nM = m2oPotentials.numRows();
    int nO = m2oPotentials.numCols();
    
    setBinary(mNode, oNode, new DiscreteBinaryFactor<V>(m2oPotentials.transpose().getMatrix().data, mNode, oNode, nM, nO));
    setBinary(oNode, mNode, new DiscreteBinaryFactor<V>(m2oPotentials.getMatrix().data, oNode, mNode, nO, nM));
  }
  
  /**
   * Set a binary factor. 
   * 
   * @throws RuntimeException if the binary factor already exists 
   * 
   * @param mNode One variable label
   * @param oNode Another variable label
   * @param m2oPotentials A matrix encoding the binary factor shared by all sites,
   *    where the rows index mNode's states, and columns index oNode's states
   */
  public void setBinary(V mNode, V oNode, double [][] m2oPotentials)
  { 
    setBinary(mNode, oNode, new SimpleMatrix(m2oPotentials));
  }

  /**
   * Set a unary factor.
   * 
   * @throws RuntimeException if the binary factor already exists 
   * 
   * @param node The variable label to which the factor will be attached to.
   * @param site2ValuePotentials A matrix where the row index sites, and the columns index node's state.
   */
  public void setUnaries(V node, SimpleMatrix site2ValuePotentials)
  {
    checkNSites(site2ValuePotentials.numRows());
    setUnary(node, createUnary(node, site2ValuePotentials));
  }
  
  /**
   * Set a unary factor.
   * 
   * @throws RuntimeException if the binary factor already exists 
   * 
   * @param node The variable label to which the factor will be attached to.
   * @param site2ValuePotentials A 2d array where the row index sites, and the columns index node's state.
   */
  public void setUnaries(V node, double [][] site2ValuePotentials)
  {
    setUnaries(node, new SimpleMatrix(site2ValuePotentials));
  }
  
  /**
   * Set a unary factor in the special case where there is only one site.
   * 
   * Syntactic sugar around setUnaries(V, SimpleMatrix) where the array
   * values is viewed as a 1 by array.length matrix.
   * 
   * @param node The variable label to which the factor will be attached to.
   * @param values See description.
   */
  public void setUnary(V node, double [] values)
  {
    setUnaries(node, new SimpleMatrix(1, values.length, true, values));
  }
  
  /**
   * Modifies in place a unary, pointwise multiplying each entry with the provided values.
   * 
   * If no unary are currently set, set the unary to be the provided one.
   * 
   * @param node The label of the variable for which the unary will be updated in place.
   * @param site2ValuePotentials A matrix where the row index sites, and the columns index node's state.
   */
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
  
  /**
   * Modifies in place a unary, pointwise multiplying each entry with the provided values.
   * 
   * If no unary are currently set, set the unary to be the provided one.
   * 
   * @param node The label of the variable for which the unary will be updated in place.
   * @param site2ValuePotentials A 2d array where the row index sites, and the columns index node's state.
   */
  public void unariesTimesEqual(V node, double [][] site2ValuePotentials)
  {
    unariesTimesEqual(node, new SimpleMatrix(site2ValuePotentials));
  }
  
  /**
   * Create a UnaryFactor.
   * 
   * @param <V> The type indexing variables.
   * @param node The label of the variable to which this unary is planned to belong to.
   * @param site2ValuePotentials A matrix where the row index sites, and the columns index node's state.
   * @return
   */
  public static <V> UnaryFactor<V> createUnary(V node, SimpleMatrix site2ValuePotentials)
  {
    return new DiscreteUnaryFactor<V>(node, site2ValuePotentials.getMatrix().data, new int[site2ValuePotentials.numRows()], site2ValuePotentials.numCols());
  }
  
  /**
   * Create a UnaryFactor.
   * 
   * @param <V> The type indexing variables.
   * @param node The label of the variable to which this unary is planned to belong to.
   * @param site2ValuePotentials A 2d array where the row index sites, and the columns index node's state.
   * @return
   */
  public static <V> UnaryFactor<V> createUnary(V node, double [][] site2ValuePotentials)
  {
    return createUnary(node, new SimpleMatrix(site2ValuePotentials));
  }
  
  /**
   * @param <V>
   * @param _factor
   * @return An array indexed by (sites, state) with normalized probabilities
   *         contained in the provided UnaryFactor (cast to DiscreteUnaryFactor)
   */
  public static <V> double[][] getNormalizedCopy(UnaryFactor<V> _factor)
  {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    DiscreteUnaryFactor<V> factor = (DiscreteUnaryFactor) _factor;
    double [][] result = new double[factor.nSites][factor.nVariableValues];
    for (int site = 0; site < factor.nSites; site++)
      factor.copyNormalizedValues(result[site], site);
    return result;
  }

  /**
   * Used by the sum product algorithm to determine how to do marginalization and pointwise products.
   */
  @Override
  public FactorOperations<V> factorOperations()
  {
    return discreteFactorGraphOperations;
  }
  
  /* Inner working of the discrete factors (based on scalings) */
  
  /**
   * The number of sites (indep copies of the graphical model)
   */
  private int nSites = -1;
  
  /**
   * Used to check that the number of sites match across all nodes
   * @param tested
   */
  private void checkNSites(int tested)
  {
    if (nSites == -1)
      nSites = tested;
    else if (nSites != tested)
      throw new RuntimeException();
  }

  /**
   * The algorithms used to do pointwise product and marginalization.
   */
  private final FactorOperations<V> discreteFactorGraphOperations = new FactorOperations<V>() 
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
      final double [] newMatrix = new double[nSites * nVariableValues]; 
      
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
            prodUnnorm *= cast[factor].getRawValue(site, varValue);
          newMatrix[nVariableValues * site + varValue] = prodUnnorm;
        }
      
      return new DiscreteUnaryFactor(cast[0].node, newMatrix, newScales, nVariableValues);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public UnaryFactor<V> marginalize(
        final BinaryFactor<V> _binary,
        final List<UnaryFactor<V>> unariesOnMarginalized)
    {
      /* This method supports up to two unaries on the node to be marginalized.
       * 
       * But if there are more, we call marginalizeOnReducedUnariesDegree()
       * which in turns calls pointwise products. The latter is less efficient
       * but ensure that we cover all the cases.
       */
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
        
        final double [] newMatrix = new double[nSites * binary.nOtherVariableValues()]; 
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
                 sum += binary.get(otherIndex, margIndex); 
               newMatrix[site * nOtherValues + otherIndex] = sum; 
             }
         else if (degree == 1) 
           for (int site = 0; site < nSites; site++)
             for (int otherIndex = 0; otherIndex < nOtherValues; otherIndex++)
             {
               double sum = 0.0;
               for (int margIndex = 0; margIndex < nMarginalizedValues; margIndex++)
                 sum += binary.get(otherIndex, margIndex) 
                       * dbf0.getRawValue(site, margIndex); 
               newMatrix[site * nOtherValues + otherIndex] = sum; 
             }
         else 
           for (int site = 0; site < nSites; site++)
             for (int otherIndex = 0; otherIndex < nOtherValues; otherIndex++)
             {
               double sum = 0.0;
               for (int margIndex = 0; margIndex < nMarginalizedValues; margIndex++)
                 sum += binary.get(otherIndex, margIndex) 
                       * dbf0.getRawValue(site, margIndex) 
                       * dbf1.getRawValue(site, margIndex); 
               newMatrix[site * nOtherValues + otherIndex] = sum; 
             }
        
        return new DiscreteUnaryFactor<V>(binary.otherNode(), newMatrix, newScales, nOtherValues);
      }
      else
        return marginalizeOnReducedUnariesDegree(this, maxDegree, _binary, unariesOnMarginalized);
    }
  };
  
  /**
   * 
   * @return A sampling algorithm for discrete unary factors.
   */
  public Sampler<V> getSampler()
  {
    return sampler;
  }
  
  private final Sampler<V> sampler = new Sampler<V>()
  {

    @Override
    public UnaryFactor<V> sample(Random rand, UnaryFactor<V> _factor)
    {
      @SuppressWarnings({ "rawtypes", "unchecked" })
      DiscreteUnaryFactor<V> factor = (DiscreteUnaryFactor) _factor;
      final double [][] normalized = getNormalizedCopy(_factor);
      for (int site = 0; site < factor.nSites; site++)
      {
        final double [] currentPrs = normalized[site];
        int sampledIndex = Multinomial.sampleMultinomial(rand, currentPrs);
        for (int state = 0 ; state < factor.nVariableValues; state++)
          currentPrs[state] = (state == sampledIndex ? 1.0 : 0.0);
      }
      
      return createUnary(factor.node, normalized);
    }

  };

  /**
   * Calls operation.pointwiseProduct on the list of unaries until
   * there are maxDegree items in unariesOnMarginalized.
   * @param <V>
   * @param operation
   * @param maxDegree
   * @param binary
   * @param unariesOnMarginalized
   * @return
   */
  private static <V> UnaryFactor<V> marginalizeOnReducedUnariesDegree(
      FactorOperations<V> operation, 
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
  
  /**
   * Simple binary potential where the same potential is shared across all sites
   * (graphical models). 
   * 
   * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
   *
   * @param <V>
   */
  private static final class DiscreteBinaryFactor<V> implements BinaryFactor<V>
  {
    /**
     * Packed array of transition. See get().
     */
    private final double [] o2mPotentials;
    private final V m, o;
    private final int nM, nO;
    
    /**
     * 
     * @param o2mPotentials
     * @param m The node to be marginalized
     * @param o The other node
     * @param nM The number of states in node m 
     * @param nO The number of states in node o
     */
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
    
    /**
     * Get the value of the potential for the two state indices.
     * 
     * @param oIndex
     * @param nIndex
     * @return
     */
    private double get(int oIndex, int nIndex)
    {
      return o2mPotentials[oIndex * nO + nIndex];
    }

    /**
     * @return Number of states for node o
     */
    private int nOtherVariableValues()
    {
      return nO;
    }
    
    /**
     * @return Number of states for node m
     */
    private int nMarginalizedVariableValues()
    {
      return nM;
    }

    @Override public V marginalizedNode() { return m; }
    @Override public V otherNode() { return o; }
  }
  
  /**
   * An efficient implementation of a collection of discrete positive measures.
   * 
   * The implementation is based on scalings. 
   * 
   * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
   *
   * @param <V>
   */
  private static final class DiscreteUnaryFactor<V> implements UnaryFactor<V>
  {
    /**
     * When the normalization at a site is smaller than UNDERFLOW_THRESHOLD,
     * we multiply all entries at that site by  UNDERFLOW_THRESHOLD_INVERSE, and 
     * decrease the scale of this site by MIN_SCALE
     */
    private static final int MIN_SCALE = -50;
    private static final double UNDERFLOW_THRESHOLD = Math.exp(MIN_SCALE);
    private static final double UNDERFLOW_THRESHOLD_INVERSE = Math.exp(-MIN_SCALE);
    
    /**
     * Similar to above, but to prevent overflows this time.
     */
    private static final int MAX_SCALE = +50;
    private static final double OVERFLOW_THRESHOLD = Math.exp(MAX_SCALE);
    private static final double OVERFLOW_THRESHOLD_INVERSE = Math.exp(-MAX_SCALE);
    
    /**
     * Packed version containing values proportional to the measure at a given
     * site and state. See getRawValue()
     */
    private final double [] site2valuePotentials;
    
    /**
     * Used as an intermediate quantity required to keep track of the 
     * normalization of each site individually. 
     * 
     * Holds an exponent in base e
     */
    private final int [] scales; 
    
    /**
     * The logNormalization of all sites.
     * 
     * Obtained by adding the logNormalization of each site.
     * 
     * The normalization of a site is just the sum of the values taken
     * by the measure at that site for each state.
     */
    private final double logNormalization;
    
    /**
     * The node to which this unary is attached to.
     */
    private final V node;
    
    /**
     * The number of sites.
     */
    private final int nSites;
    
    /**
     * The number of states (values) this variable can take on at each site.
     */
    private final int nVariableValues;

    /**
     * 
     * @param node
     * @param site2valuePotentials
     * @param scales
     * @param nVariableValues
     */
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
        double currentNorm = rawNorm(site);
        final int currentScale = scales[site];
        
        // update normalization
        logNorm = logNorm - currentScale;
        tempProd *= currentNorm;
        
        if (tempProd < 0)
          throw new RuntimeException("DiscreteFactors should not have negative entries");
        
        // accumulate the normalization in log scale before it underflows
        while (tempProd > 0 && tempProd < UNDERFLOW_THRESHOLD)
        {
          tempProd *= UNDERFLOW_THRESHOLD_INVERSE;
          logNorm += MIN_SCALE;
        }
        
        // rescale to prevent underflow if needed
        while (currentNorm > 0 && currentNorm < UNDERFLOW_THRESHOLD)
        {
          scales[site] = scales[site] - MIN_SCALE;
          for (int valueIndex = 0; valueIndex < nVariableValues(); valueIndex++)
            setRawValue(site, valueIndex, getRawValue(site,valueIndex) * UNDERFLOW_THRESHOLD_INVERSE);
          currentNorm = rawNorm(site); 
        }
        
        // accumulate the normalization in log scale before it overflows
        while (tempProd > OVERFLOW_THRESHOLD)
        {
          tempProd *= OVERFLOW_THRESHOLD_INVERSE;
          logNorm += MAX_SCALE;
        }
        
        // rescale to prevent overflow if needed
        while (currentNorm > OVERFLOW_THRESHOLD)
        {
          scales[site] = scales[site] - MAX_SCALE;
          for (int valueIndex = 0; valueIndex < nVariableValues(); valueIndex++)
            setRawValue(site, valueIndex, getRawValue(site,valueIndex) * OVERFLOW_THRESHOLD_INVERSE);
          currentNorm = rawNorm(site); 
        }
      }
      logNorm += Math.log(tempProd);
      
      this.logNormalization = logNorm;
    }
    
    /**
     * This value is proportional to the measure, but with an arbitrary 
     * (but fixed) proportionality constant.
     * 
     * Internal. Not to be used by the end user.
     * 
     * @param site
     * @param valueIndex
     * @return
     */
    private double getRawValue(final int site, final int valueIndex)
    {
      return site2valuePotentials[site * nVariableValues + valueIndex];
    }
    
    /**
     * Internal. Not to be used by the end user.
     * 
     * @param site
     * @param valueIndex
     * @param value
     */
    private void setRawValue(final int site, final int valueIndex, final double value)
    {
      site2valuePotentials[site * nVariableValues + valueIndex] = value;
    }
    
    /**
     * 
     * @param destination
     * @param site
     */
    private void copyNormalizedValues(double [] destination, int site)
    {
      for (int state = 0; state < nVariableValues(); state++)
        destination[state] = getRawValue(site, state);
      Multinomial.normalize(destination);
    }

    /**
     * 
     * @return
     */
    private int nVariableValues()
    {
      return nVariableValues;
    }

    @Override public V connectedVariable() { return node; }
    
    /**
     * The log normalization of a single site.
     * 
     * @param site
     * @return
     */
    @SuppressWarnings("unused")
    private double logNormalization(int site)
    {
      return Math.log(rawNorm(site)) - scales[site];
    }
    
    /**
     * Internal. Not to be used by the end user.
     * 
     * @param site
     * @return
     */
    private double rawNorm(int site)
    {
      double sum = 0.0;
      for (int valueIndex = 0; valueIndex < nVariableValues; valueIndex++)
        sum += getRawValue(site, valueIndex);
      return sum;
    }

    /**
     * The overall logNormalization (across all sites).
     */
    @Override
    public double logNormalization()
    {
      return logNormalization;
    }

    /**
     * 
     * @return The number of sites.
     */
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
