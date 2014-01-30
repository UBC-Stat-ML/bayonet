package bayonet.rplot;

import java.io.File;

import org.mvel2.templates.TemplateRuntime;


import binc.Command;
import briefj.BriefIO;




public class RUtils
{
  
  public static Command Rscript = Command.cmd("Rscript");
  
  
  public static String callR(String commands)
  {
    File tempFile = BriefIO.createTempFile();
    BriefIO.write(tempFile, commands);
    return Command.call(Rscript.withArgs(tempFile.getAbsolutePath()));
  }
  
  public static String callGeneratedRScript(String templateResourceURL, Object context)
  {
    String template = BriefIO.resourceToString(templateResourceURL); 
    String generatedScript = (String) TemplateRuntime.eval(template, context);
    return callR(generatedScript);
  }
  
  public static String callRBridge(RJavaBridge bridge)
  {
    return callGeneratedRScript(bridge.rTemplateResourceURL(), bridge);
  }
  
  public static void main(String [] args)
  {
    double [] data = new double[]{1.2, 1.5, 2.1};
    PlotHistogram.from(data).toPDF(new File("/Users/bouchard/temp/anotherhist.pdf"));
  }
  
  static String escapeQuote(String str)
  {
    str = str.replace("\\", "\\\\");
    return str.replace("'", "\\'");
  }
}
