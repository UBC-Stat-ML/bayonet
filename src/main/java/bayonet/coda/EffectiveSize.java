package bayonet.coda;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import bayonet.rplot.RJavaBridge;
import bayonet.rplot.RUtils;
import briefj.BriefFiles;
import briefj.BriefStrings;



public class EffectiveSize extends RJavaBridge
{
  private final File inputChain, inputIndex;
  
  private static final Pattern pattern = Pattern.compile("EffectiveSize = (.*)");
  
  public static double effectiveSize(List<Double> data)
  {
    File inputChain = BriefFiles.createTempFile();
    File inputIndex = BriefFiles.createTempFile();
    CodaParser.listToCoda(inputIndex, inputChain, data, "dummy");
    EffectiveSize effectiveSize = new EffectiveSize(inputChain, inputIndex);
    List<Double> essValues = effectiveSize.getESSValues();
    if (essValues.size()!=1)
      throw new RuntimeException();
    return essValues.get(0);
  }
  
  public EffectiveSize(File inputChain, File inputIndex)
  {
    this.inputChain = inputChain;
    this.inputIndex = inputIndex;
  }
  
  public List<Double> getESSValues()
  {
    String callStr = RUtils.callRBridge(this);
    String match = BriefStrings.firstGroupFromFirstMatch(pattern, callStr);
    String [] values = match.split("\\s+");
    List<Double> result = Lists.newArrayList();
    for (String value : values)
      result.add(Double.parseDouble(value));
    return result;
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
