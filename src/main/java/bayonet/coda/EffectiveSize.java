package bayonet.coda;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import bayonet.rplot.RJavaBridge;
import bayonet.rplot.RUtils;
import briefj.BriefFiles;
import briefj.BriefStrings;



public class EffectiveSize extends RJavaBridge
{
  private final File inputChain, inputIndex;
  
  private static final Pattern pattern = Pattern.compile("EffectiveSize=(.*)");
  
  public static double effectiveSize(List<Double> data)
  {
    File inputChain = BriefFiles.createTempFile();
    File inputIndex = BriefFiles.createTempFile();
    CodaParser.listToCoda(inputIndex, inputChain, data, "dummy");
    EffectiveSize effectiveSize = new EffectiveSize(inputChain, inputIndex);
    String result = RUtils.callRBridge(effectiveSize);
    String match = BriefStrings.firstGroupFromFirstMatch(pattern, result);
    return Double.parseDouble(match);
  }
  
  private EffectiveSize(File inputChain, File inputIndex)
  {
    this.inputChain = inputChain;
    this.inputIndex = inputIndex;
  }

  @Override
  public String rTemplateResourceURL()
  {
    return "/bayonet/coda/EffectiveSize.txt";
  }
  
  public File getInputChain()
  {
    return inputChain;
  }
  
  public File getInputIndex()
  {
    return inputIndex;
  }
}
