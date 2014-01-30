package bayonet.coda;

import java.io.File;

import bayonet.rplot.RJavaBridge;
import bayonet.rplot.RUtils;



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
