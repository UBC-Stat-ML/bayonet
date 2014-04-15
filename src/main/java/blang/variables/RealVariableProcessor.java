package blang.variables;

import java.io.File;

import bayonet.coda.CodaParser;
import bayonet.coda.SimpleCodaPlots;
import blang.processing.NodeProcessor;
import blang.processing.ProcessorContext;
import briefj.OutputManager;
import briefj.tomove.Results;



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
