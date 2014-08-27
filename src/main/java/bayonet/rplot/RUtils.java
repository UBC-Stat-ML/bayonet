package bayonet.rplot;

import java.io.File;

import org.mvel2.templates.TemplateRuntime;


import binc.Command;
import briefj.BriefFiles;
import briefj.BriefIO;
import briefj.BriefStrings;




public class RUtils
{
  
  public static Command Rscript = Command.cmd("Rscript");
  
  public static String callR(String commands)
  {
    return callR(commands, null);
  }
  
  public static String callR(String commands, File scriptFile)
  {
    if (scriptFile == null)
      scriptFile = BriefFiles.createTempFile();
    BriefIO.write(scriptFile, commands);
    return Command.call(Rscript.withArgs(scriptFile.getAbsolutePath()));
  }
  
  public static String callGeneratedRScript(String templateResourceURL, Object context)
  {
    return callGeneratedRScript(templateResourceURL, context, null);
  }
  
  public static String callGeneratedRScript(String templateResourceURL, Object context, File scriptFile)
  {
    if (scriptFile == null)
      scriptFile = BriefFiles.createTempFile();
    String template = BriefIO.resourceToString(templateResourceURL); 
    String generatedScript = (String) TemplateRuntime.eval(template, context);
    return callR(generatedScript, scriptFile);
  }
  
  public static String callRBridge(RJavaBridge bridge)
  {
    File workFolder = bridge.getWorkFolder();
    File scriptFile = workFolder == null ? null : new File(workFolder, "script-" + BriefStrings.generateUniqueId() + ".r");
    return callGeneratedRScript(bridge.rTemplateResourceURL(), bridge, scriptFile);
  }
  
  public static void main(String [] args)
  {
    double [] data = new double[]{1.2, 1.5, 2.1};
    PlotHistogram.from(data).toPDF(new File("/Users/bouchard/temp/anotherhist.pdf"));
  }
  
  public static String escapeQuote(String str)
  {
    str = str.replace("\\", "\\\\");
    return str.replace("'", "\\'");
  }
}
