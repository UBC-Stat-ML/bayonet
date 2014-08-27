package blang.variables;

import java.io.File;

import bayonet.coda.CodaParser;
import bayonet.coda.SimpleCodaPlots;
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
public class IntegerVariableProcessor implements NodeProcessor<IntegerVariable> 
{
  private IntegerVariable variable;
  private OutputManager output = null;
  private int interval = 2;
  private int current = 0;
  private boolean progress;
  private boolean CODA;
  private int nMCMCSweeps;

  /**
   * This processor allows for a variety of different customizations
   * In the most basic setup, the processor will always write the thinned samples to the output file
   * Optionally, the processor can generate CODA plots either: 
   * 1) Throughout the sampling for debugging purposes, at exponential times and for the last sample; or
   * 2) At the end of sampling. That is, it will wait to generate the CODA plot until the last iterate. 
   * 
   */
  @Override
  public void process(ProcessorContext context)
  {
    ensureInitialized(context);
    String key = context.getModel().getName(variable);
    int iteration = context.getMcmcIteration();
    output.write(key, "mcmcIter", iteration, key, variable.getIntegerValue());
    output.flush();

    if (CODA)
    {
      current++;
      if (current == interval && progress)
      {
        interval = interval * 2;
        current = 0;
        generateCODA();       
      }

      if (iteration == (nMCMCSweeps - 1))
      {
        generateCODA();
      }
    }

  }

  private void generateCODA()
  {
    File 
    indexFile = new File(output.getOutputFolder(), "CODAindex.txt"),
    chainFile = new File(output.getOutputFolder(), "CODAchain1.txt");
    CodaParser.CSVToCoda(indexFile, chainFile, output.getOutputFolder());
    SimpleCodaPlots codaPlots = new SimpleCodaPlots(chainFile, indexFile);
    codaPlots.toPDF(new File(output.getOutputFolder(), "codaPlots.pdf"));
  }
  
  private void ensureInitialized(ProcessorContext context)
  {
    if (output != null)
      return;
    output = new OutputManager();
    String varName = context.getModel().getName(variable);
    File csvSamples = new File(Results.getResultFolder(), varName + "-csv");
    output.setOutputFolder(csvSamples);
    progress = context.getOptions().progressCODA;
    nMCMCSweeps = context.getOptions().nMCMCSweeps;
    CODA = context.getOptions().CODA;
  }

  @Override
  public void setReference(IntegerVariable variable)
  {
    this.variable = variable;
  }

}
