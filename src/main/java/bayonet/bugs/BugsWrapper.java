package bayonet.bugs;

import static binc.Command.call;
import static binc.Command.cmd;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import binc.Command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Sets;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;



public class BugsWrapper implements Runnable
{
  
  @DynamicParameter(names = "-data.", description = "Variable conditioned on (CSV files)")
  private Map<String, String> data = new HashMap<String, String>();
  
  @DynamicParameter(names = "-constant.", description = "Constants")
  private Map<String, String> constants = new HashMap<String, String>();

  @Parameter(names = "-model", description = "Path to the model", required=true)
  private String model;

  @Override
  public void run()
  {
    // create data file
    File dataFile = createDataFile();
    
    // create script
    File scriptFile = createScript(dataFile);
    
    // run jags
    call(jags.withArgs(scriptFile.getAbsolutePath()));
    
  }

  private File createScript(File dataFile)
  {
    StringBuilder contents = new StringBuilder();
    
    contents.append("model in \"" + model + "\"\n");
    contents.append("data in \"" + dataFile.getAbsolutePath() + "\"\n");
    contents.append("compile, nchains(1)\n"); // TODO: multi chain via parallelization
    contents.append("initialize\n");
    contents.append("update 1000\n"); // TODO: adapt n iteration
    for (String variable : variables())
      contents.append("monitor " + variable + "\n");
    contents.append("update 10000\n");
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
      
      // remove those that are observed
      for (String key : data.keySet())
      {
        if (!variables.contains(key))
          throw new RuntimeException("Looks like the modified JAGS is not being used. Observed variable not listed in variables.");
        variables.remove(key);
      }
          
      return variables;
    } 
    catch (IOException e) { throw new RuntimeException(e); }
  }

  private File createDataFile()
  {
    File dataFile = new File("data.txt");
    try
    {
      Writer dataWriter = Files.newWriter(dataFile, Charsets.UTF_8);
      
      for (String key : data.keySet())
      {
        dataWriter.append("'" + key + "' <- c(\n\n");
        dataWriter.append(Joiner.on(",\n").join(Files.readLines(new File(data.get(key)), Charsets.UTF_8)));
        dataWriter.append(")\n");
      }
      
      for (String key : constants.keySet())
        dataWriter.append("'" + key + "' <- " + constants.get(key) + "\n");
      
      dataWriter.close();
    } 
    catch (Exception e) { throw new RuntimeException(e); }
    return dataFile;
  }

}
