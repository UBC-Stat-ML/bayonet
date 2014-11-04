package blang.variables;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import bayonet.coda.CodaParser;
import bayonet.coda.EffectiveSize;
import bayonet.coda.SimpleCodaPlots;
import bayonet.rplot.RUtils;
import blang.processing.NodeProcessor;
import blang.processing.ProcessorContext;
import briefj.OutputManager;
import briefj.run.Results;


/**
 * 
 * Collects samples of a real variables into a csv file under a directory named 
 * upon the name given to the variable by the ProbabilityModel's getName().
 * 
 * After 2 iterations, create a plot via coda, then after 4, 8, 16, etc. The plot
 * is in the same directory, called codaPlot.pdf.
 * This requires R.
 * 
 * TODO: avoid going through coda
 * TODO: produce richer and nicer plots
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 * @author Sean Jewell (jewellsean@gmail.com)
 *
 */
public class RealVariableProcessor implements NodeProcessor<RealVariable> 
{
  private RealValued variable;
  private OutputManager output = null;
  private OutputManager samplesOutput = null;
  private int interval = 4;
  private int current = 0;
  private String variableName = null;
  private boolean progress;
  private boolean CODA;
  private SummaryStatistics statistics = new SummaryStatistics();
  
  public RealVariableProcessor()
  {
  }
  
  public RealVariableProcessor(String variableName, RealValued variable)
  {
    this.variableName = variableName;
    this.variable = variable;
  }
  
  /**
   * This processor allows for a variety of different customizations
   * In the most basic setup, the processor will always write the thinned samples to the output file
   * Optionally, the processor can generate CODA plots either: 
   * 1) Throughout the sampling for debugging purposes, at exponential times and for the last sample (progress = true); or
   * 2) Only at the end of sampling (progress = false). That is, it will wait to generate the CODA plot until the last iterate. 
   * 
   */
  @Override
  public void process(ProcessorContext context)
  {
    ensureInitialized(context);
    int iteration = context.getMcmcIteration();
    samplesOutput.write(variableName, "mcmcIter", iteration, variableName, variable.getValue());
    
    statistics.addValue(variable.getValue());

    if (CODA)
    {
      current++;
      if (current == interval && progress)
      {
        interval = interval * 2;
        current = 0;
        generateCODA(iteration);       
      }

      if (context.isLastProcessCall())
        generateCODA(iteration);
    }
    
    if (context.isLastProcessCall())
    {
      String statString = statistics.toString();
      String[] sepStats = statString.split("\n");
      String[] toWrite = null; 
      for (int i = 1; i < sepStats.length; i++) // avoid the first element; serves as an unneeded header
      {
        String statSplit = sepStats[i];
        String[] stat = statSplit.split(":");
        toWrite = ArrayUtils.addAll(toWrite, stat);
      }
      output.write(variableName + "-summary", (Object []) toWrite);
      output.flush();
    }
  }
  
  public StopWatch watch = new StopWatch();
  private File csvSamples;

  private void generateCODA(int iteration)
  {
    samplesOutput.flush();
    File 
    indexFile = new File(csvSamples, "CODAindex.txt"),
    chainFile = new File(csvSamples, "CODAchain1.txt");
    CodaParser.CSVToCoda(indexFile, chainFile, csvSamples);
    SimpleCodaPlots codaPlots = new SimpleCodaPlots(chainFile, indexFile);
    codaPlots.toPDF(new File(csvSamples, "codaPlots.pdf"));
    
    EffectiveSize essCalculator = new EffectiveSize(chainFile, indexFile);
    List<Double> essValues = null;
    try {
    essValues =  essCalculator.getESSValues();
    } catch (Exception e)
    {
      return;
    }
    if (essValues.size() != 1)
      throw new RuntimeException();
    double ess = essValues.get(0);
    double time = watch.getTime() / 1000.0;
    double essPerSec = ess/time;
    output.printWrite(variableName + "-ess", "iteration", iteration, "ess", ess, "time", time, "essPerSec", essPerSec);
  }
  
  private void ensureInitialized(ProcessorContext context)
  {
    if (output != null)
      return;
    watch.start();
    output = new OutputManager();
    samplesOutput = new OutputManager();
    if (variableName == null)
      variableName = context.getModel().getName(variable);
    csvSamples = new File(Results.getResultFolder(), variableName + "-csv");
    output.setOutputFolder(Results.getResultFolder());
    samplesOutput.setOutputFolder(csvSamples);
    progress = context.getOptions().progressCODA;
    CODA = context.getOptions().CODA;
  }

  @Override
  public void setReference(RealVariable variable)
  {
    this.variable = variable;
  }

}
