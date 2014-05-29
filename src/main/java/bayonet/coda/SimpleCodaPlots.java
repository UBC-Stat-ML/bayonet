package bayonet.coda;

import java.io.File;

import bayonet.rplot.RJavaBridge;
import bayonet.rplot.RUtils;
import blang.MCMCAlgorithm;



public class SimpleCodaPlots extends RJavaBridge
{
  private final File inputChain, inputIndex;
  
  public SimpleCodaPlots(File inputChain, File inputIndex)
  {
    this.inputChain = inputChain;
    this.inputIndex = inputIndex;
  }

  public void toPDF(File output)
  {
    this.output = output;
    RUtils.callRBridge(this);
  }
  
  private File output;
  public String getOutput()
  {
    return output.getAbsolutePath();
  }

  public String getOutputESS()
  {
    return output.getParent() + "/ess-" + (System.currentTimeMillis() - MCMCAlgorithm.startTime) + ".csv";
  }
  
  @Override public String rTemplateResourceURL() { return "/bayonet/coda/SimpleCodaPlots.txt"; }

  public File getInputChain()
  {
    return inputChain;
  }

  
  
  public File getInputIndex()
  {
    return inputIndex;
  }

}
