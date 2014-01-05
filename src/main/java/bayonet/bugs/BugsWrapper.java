package bayonet.bugs;

import static binc.Command.call;
import static binc.Command.cmd;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import binc.Command;
import static briefj.BriefMaps.*;
import briefj.BriefIO;
import briefj.BriefStrings;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Maps;
import com.beust.jcommander.internal.Sets;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import static com.google.common.base.Optional.*;
import com.google.common.io.Files;



public class BugsWrapper implements Runnable
{
  
  
  
//  @DynamicParameter(names = "--data.", description = "Variable conditioned on (CSV files)")
//  private Map<String, String> data = new HashMap<String, String>();
  
  @DynamicParameter(names = "--constant.", description = "Constants")
  private Map<String, String> constants = new HashMap<String, String>();
  
  @Parameter(names = "--data", description = "Path to the main data file in tidy format", required=true)
  private String data;

  @Parameter(names = "--model", description = "Path to the model", required=true)
  private String model;

  @Parameter(names = "--nBurnIterations")
  private int nBurnIterations = 1000;

  @Parameter(names = "--thinning")
  private int thinning = 50;

  @Parameter(names = "--nIterations")
  private int nIterations = 10000;

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
      
      // remove those that are observed.. No: what about missing value imputation?
//      for (String key : data.keySet())
//      {
//        if (!variables.contains(key))
//          throw new RuntimeException("Looks like the modified JAGS is not being used. Observed variable not listed in variables.");
//        variables.remove(key);
//      }
          
      return variables;
    } 
    catch (IOException e) { throw new RuntimeException(e); }
  }

  private File createDataFile()
  {
    Map<Variable, StringBuilder> outputMap = Maps.newHashMap();
    Map<String,Integer> nCoordinates = Maps.newHashMap();
    for (Map<String,String> fields : BriefIO.readLines(data).indexCSV())
      for (String _key : fields.keySet())
      {
        Variable variable = new Variable(_key);
        StringBuilder currentStr = getOrPut(outputMap, variable, new StringBuilder());
        currentStr.append((currentStr.length() > 0 ? ",\n" : "") + fields.get(variable));
        nCoordinates.put(variable.name, 1 + fromNullable(nCoordinates.get(variable.name)).or(0));
      }
    
    File dataFile = new File("data.txt");
    PrintWriter out = BriefIO.output(dataFile);
    
    for (String key : outputMap.keySet())
      out.append("'" + key + "' <- c(\n" + outputMap.get(key) + ")"); 
    
    for (String key : constants.keySet())
      out.append("'" + key + "' <- " + constants.get(key) + "\n");

    out.close();
    return dataFile;
  }
  
  
  private static class Variable
  {
    private final boolean isMatrix;
    private final String name;
    private final int coordinate;
    private Variable(String string)
    {
      isMatrix = MATRIX_PATTERN.matcher(string).matches();
      if (isMatrix)
      {
        List<String> matches = BriefStrings.allGroupsFromFirstMatch(MATRIX_PATTERN, string);
        if (matches.size() != 2) throw new RuntimeException();
        this.name = matches.get(0);
        this.coordinate = Integer.parseInt(matches.get(1));
      }
      else
      {
        this.name = string;
        this.coordinate = 0;
      }
    }
    private static final Pattern MATRIX_PATTERN = Pattern.compile("([^\\]]+)\\[([0-9]+)\\]");
    
    @Override
    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + coordinate;
      result = prime * result + (isMatrix ? 1231 : 1237);
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
    }
    @Override
    public boolean equals(Object obj)
    {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Variable other = (Variable) obj;
      if (coordinate != other.coordinate)
        return false;
      if (isMatrix != other.isMatrix)
        return false;
      if (name == null)
      {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      return true;
    }
    @Override
    public String toString()
    {
      return "Variable [isMatrix=" + isMatrix + ", name=" + name
          + ", coordinate=" + coordinate + "]";
    }
    
    
  }
  
  public static void main(String [] args)
  {
    String string = "var[17]";
    System.out.println(new Variable(string));
  }

}
