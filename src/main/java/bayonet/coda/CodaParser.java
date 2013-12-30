package bayonet.coda;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static com.google.common.base.Splitter.*;

import briefj.BriefIO;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import static briefj.BriefIO.*;



public class CodaParser
{
  public static void fromCoda(File codaIndex, File codaContents, File destinationDirectory)
  {
    BriefIO.createParentDirs(destinationDirectory);
    LinkedList<Block> blocks = readCodaIndex(codaIndex);
    
    // state of the writing process:
    int nLinesLeftInBlock = 0;
    Block currentBlock = null;
    PrintWriter out = null;
    
    for (String line : readLines(codaContents))
    {
      if (nLinesLeftInBlock == 0)
      {
        // a new block is starting
        Block newBlock = blocks.pollFirst();
        nLinesLeftInBlock = newBlock.nSamples;
        
        // only need to change the output file if starting (currentBlock == null)
        // or if the variable name changes (vs sometimes only the indices change)
        if (currentBlock == null || !currentBlock.variableName.equals(newBlock.variableName))
        {
          if (out != null) out.close();
          out = output(new File(destinationDirectory, newBlock.variableName + ".csv"));
        }
        
        currentBlock = newBlock;
      }
      
      // parse one point
//      Scanner lineScanner = new Scanner(line).useDelimiter("\\s+");
//      @SuppressWarnings("unused")
//      String iterationIndex = lineScanner.next(); // we do not need to use this field
      String [] fields = line.split("\\s+");
      double value = Double.parseDouble(fields[1]);//lineScanner.nextDouble(); // payload
      for (int index : currentBlock.indices)
        out.print(index + ",");
      out.println(value);
      nLinesLeftInBlock--;
    }
    if (out != null) out.close();
  }
  
  public static void toCoda(File codaIndex, File codaContents, File variableFilesDirectory)
  {
    PrintWriter codaIndexOut =  output(codaIndex);
    PrintWriter codaContentsOut=output(codaContents);
    
    int currentLine = 0;
    int startOfBlockLine = 0;
    for (File variableFile : ls(variableFilesDirectory, "csv"))
    {
      String variableName = variableFile.getName().replace(".csv", "");
      List<String> previousIndices = null;
      int currentMCMCIter = 0;
      for (List<String> fields : readLines(variableFile).splitCSV())
      {
        List<String> currentIndices = fields.subList(0, Math.max(0, fields.size() - 2));
        if (previousIndices == null) previousIndices = currentIndices;
        if (!previousIndices.equals(currentIndices))
        {
          codaIndexOut.println(codeIndexLine(variableName, previousIndices, startOfBlockLine, currentLine - 1));
          previousIndices = currentIndices;
          startOfBlockLine = currentLine;
        }
        codaContentsOut.println("" + (currentMCMCIter+1) + "  " + fields.get(fields.size() - 1));
        currentLine++;
        currentMCMCIter++;
      }
      codaIndexOut.println(codeIndexLine(variableName, previousIndices, startOfBlockLine, currentLine - 1));
    }
    
    codaIndexOut.close();
    codaContentsOut.close();
  }
  
  private static String codeIndexLine(String variableName, List<String> previousIndices, int startOfBlockLine, int endOfBlockLine)
  {
    return "" + 
      variableName + 
      (previousIndices.isEmpty() ? "" : "[" + Joiner.on(",").join(previousIndices) + "]") +
      " " + (startOfBlockLine + 1) + " " + (endOfBlockLine + 1);
  }
  
  private static LinkedList<Block> readCodaIndex(File codaIndex)
  {
    LinkedList<Block> result = Lists.newLinkedList();
    for (String line : readLines(codaIndex))
    {
//      Scanner lineScanner = new Scanner(line);
      String[] fields = line.split("\\s+");
      String[] variableAndIndex = fields[0].replace("]", "").split("\\["); //on("\\[")
//        .splitToList(fields[0].replace("]",""));
      final int 
        firstInclusive = Integer.parseInt(fields[1]), //lineScanner.nextInt(),
        secondInclusive= Integer.parseInt(fields[2]); //lineScanner.nextInt();
      final int nSamples = secondInclusive - firstInclusive + 1;
      
      String variable = variableAndIndex[0]; //variableAndIndex.get(0);
      String [] indicesStr = variableAndIndex.length == 2 ? variableAndIndex[1].split(",") : new String[0];
      int [] indices = new int[indicesStr.length];
      for (int i = 0; i < indices.length; i++)
        indices[i] = Integer.parseInt(indicesStr[i]);
      
      result.add(new Block(variable, indices, nSamples));
    }
    return result;
  }

  private static class Block
  {
    private final String variableName;
    private final int [] indices;
    private final int nSamples;
    public Block(String variableName, int[] indices, int nSamples)
    {
      this.variableName = variableName;
      this.indices = indices;
      this.nSamples = nSamples;
    }
    @Override
    public String toString()
    {
      return "Block [variableName=" + variableName + ", indices="
          + Arrays.toString(indices) + ", nSamples=" + nSamples + "]";
    }
  }
  
  public static void main(String [] args)
  {
    File originalCodaIndex = new File("/Users/bouchard/temp/CODAindex.txt");
    File originalCoda = new File("/Users/bouchard/temp/CODAchain1.txt");
    
    File dest = new File("/Users/bouchard/temp/created");
    
    fromCoda(originalCodaIndex, originalCoda, dest);
    
    File newCodaIndex = new File("/Users/bouchard/temp/created2/CODAindex.txt");
    File newCoda = new File("/Users/bouchard/temp/created2/CODAchain1.txt");
    toCoda(newCodaIndex, newCoda, dest);
  }
  
}
