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
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class IntegerValuedVectorProcessor implements NodeProcessor<IntegerValuedVector>, RealValued
{
	private IntegerValuedVector variable;
	private OutputManager output = null;
	private int interval = 2;
	private int current = 0;
	private boolean progress;
	private boolean CODA;
	private int nMCMCSweeps;

	  @Override
	  public void process(ProcessorContext context)
	  {
	    ensureInitialized(context);
	    String key = context.getModel().getName(variable);
	    int iteration = context.getMcmcIteration();
	    output.write(key, "mcmcIter", iteration, key, variable.evaluateTestFunction());
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
	public void setReference(IntegerValuedVector variable)
	{
		this.variable = variable;
	}

	@Override
  public double getValue() 
	{
	  return variable.evaluateTestFunction();
  }

}
