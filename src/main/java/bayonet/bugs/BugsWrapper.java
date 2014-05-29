package bayonet.bugs;

import static binc.Command.call;
import static binc.Command.cmd;
import static briefj.BriefMaps.getOrPut;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import bayonet.coda.CodaParser;
import binc.Command;
import briefj.BriefIO;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;



public class BugsWrapper implements Runnable
{
  
  
  
  
  public static final String VAGRANT_PREFIX = "/home/vagrant/.data/homework2/";

//  @DynamicParameter(names = "--data.", description = "Variable conditioned on (CSV files)")
//  private Map<String, String> data = new HashMap<String, String>();
  
  @DynamicParameter(names = "--constant.", description = "Constants")
  private Map<String, String> constants = new HashMap<String, String>();
  
  @Parameter(names = "--data", description = "Path to the main data file in tidy format")
  private String data = null;

  @Parameter(names = "--model", description = "Path to the model")
  private String model = null;

  @Parameter(names = "--nBurnIterations")
  private int nBurnIterations = 1000;

  @Parameter(names = "--thinning")
  private int thinning = 50;

  @Parameter(names = "--nIterations")
  private int nIterations = 10000;
  
  @Parameter(names = "--destination")
  private File destination = new File(".");

  @Override
  public void run()
  {
    if (model == null || !new File(model).exists())
    {
      System.err.println("A valid model file should be specified.");
      System.exit(1);
    }
    
    if (data == null)
    {
      // try to infer (file with csv)
      // TODO: remove hard-coded paths?
      String ext = FilenameUtils.getExtension(model);
      String stem = ext.equals("") ? model : new File(model).getName().replaceFirst("[.]" + ext + "$", "");
      data = VAGRANT_PREFIX + "/" + stem + ".csv";
      System.out.println("Data: " + data);
    }
    if (!new File(data).exists())
    {
      System.err.println("A data file should be specified. By default, model.bugs should have a corresponding data.csv file.");
      System.exit(1);
    }
    
    // create data file
    File dataFile = createDataFile();
    
    // create script
    File scriptFile = createScript(dataFile);
    
    // run jags
    call(jags.withArgs(scriptFile.getAbsolutePath()).withStandardOutMirroring());
    
    // transform the output back to a reasonable format
    File codaIndex = new File("CODAindex.txt");
    File codaContents = new File("CODAchain1.txt");
    CodaParser.codaToCSV(codaIndex, codaContents, destination);
    
  }

  private File createScript(File dataFile)
  {
    StringBuilder contents = new StringBuilder();
    
    contents.append("model in \"" + model + "\"\n");
    contents.append("data in \"" + dataFile.getAbsolutePath() + "\"\n");
    contents.append("compile, nchains(1)\n"); // TODO: multi chain via parallelization
    contents.append("initialize\n");
    contents.append("update " + nBurnIterations + "\n"); 
    for (String variable : variables())
      contents.append("monitor " + variable + ", thin(" + thinning + ")\n");
    contents.append("update " + nIterations + "\n");
    contents.append("coda *\n");
    contents.append("exit");
    
    File scriptFile = new File("script.jags");
    try
    {
      FileUtils.writeStringToFile(scriptFile, contents.toString());
      return scriptFile;
    } 
    catch (IOException e) { throw new RuntimeException(e); }
  }
  
  
  private final Command jags = cmd("jags");
  
  private Set<String> variables()
  {
    // fire up our modified version of jags to peek the variable names
    try
    {
      File temp = File.createTempFile("jags-temp-file", ".jags");
      FileUtils.writeStringToFile(temp, "model in \"" + model + "\"");
      String result = call(jags.withArgs(temp.getAbsolutePath()));
      Set<String> variables = Sets.newHashSet();
      for (String line : result.split("\n"))
        if (line.matches("^variable[:].*"))
          variables.add(line.replaceFirst("^variable[:]\\s*", ""));
          
      return variables;
    } 
    catch (IOException e) { throw new RuntimeException(e); }
  }

  private File createDataFile()
  {
    Map<String, StringBuilder> outputMap = Maps.newHashMap();
    for (Map<String,String> fields : BriefIO.readLines(data).indexCSV())
      for (String key : fields.keySet())
      {
        StringBuilder currentStr = getOrPut(outputMap, key, new StringBuilder());
        currentStr.append((currentStr.length() > 0 ? ",\n" : "") + fields.get(key));
      }
    
    File dataFile = BriefIO.createTempFile(); //new File("data.txt");
    PrintWriter out = BriefIO.output(dataFile);
    
    for (String key : outputMap.keySet())
      out.append("'" + key + "' <- c(\n" + outputMap.get(key) + ")"); 
    
    for (String key : constants.keySet())
      out.append("'" + key + "' <- " + constants.get(key) + "\n");

    out.close();
    return dataFile;
  }
  
}
