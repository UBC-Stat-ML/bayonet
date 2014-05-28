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
 *
 */
public class RealVariableProcessor implements NodeProcessor<RealVariable> 
{
  private  RealVariable variable;
  private OutputManager output = null;
  private int interval = 2;
  private int current = 0;

  @Override
  public void process(ProcessorContext context)
  {
    ensureInitialized(context);
    String key = context.getModel().getName(variable);
    output.write(key, "mcmcIter", context.getMcmcIteration(), key, variable.getValue());
    output.flush();
    current++;
    if (current == interval)
    {
      interval = interval * 2;
      current = 0;
      File 
        indexFile = new File(output.getOutputFolder(), "CODAindex.txt"),
        chainFile = new File(output.getOutputFolder(), "CODAchain1.txt");
      CodaParser.CSVToCoda(indexFile, chainFile, output.getOutputFolder());
      SimpleCodaPlots codaPlots = new SimpleCodaPlots(chainFile, indexFile);
      codaPlots.toPDF(new File(output.getOutputFolder(), "codaPlots.pdf"));
    }
  }
  
  private void ensureInitialized(ProcessorContext context)
  {
    if (output != null)
      return;
    output = new OutputManager();
    String varName = context.getModel().getName(variable);
    File csvSamples = new File(Results.getResultFolder(), varName + "-csv");
    output.setOutputFolder(csvSamples);
  }

  @Override
  public void setReference(RealVariable variable)
  {
    this.variable = variable;
  }

}
