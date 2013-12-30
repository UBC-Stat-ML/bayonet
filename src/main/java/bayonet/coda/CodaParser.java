package bayonet.coda;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static com.google.common.base.Splitter.*;

import briefj.BriefIO;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import static briefj.BriefIO.*;



public class CodaParser
{
  
  
  public static void fromCoda(File codaIndex, File codaContents, File destinationDirectory)
  {
    BriefIO.createParentDirs(destinationDirectory);
    LinkedList<Block> blocks = readCodaIndex(codaIndex);
    
    int nLinesLeftInBlock = 0;
    Block currentBlock = null;
    PrintWriter out = null;
    for (String line : readLines(codaContents))
    {
      if (nLinesLeftInBlock == 0)
      {
        if (out != null) out.close();
        Block newBlock = blocks.pollFirst();
        nLinesLeftInBlock = currentBlock.nSamples;
        
        if (currentBlock == null || !currentBlock.variableName.equals(newBlock.variableName))
          out = output(new File(destinationDirectory, currentBlock.variableName));
        
        currentBlock = newBlock;
      }
      Scanner lineScanner = new Scanner(line);
      @SuppressWarnings("unused")
      String iterationIndex = lineScanner.next(); // not used
      double value = lineScanner.nextDouble(); // payload
      for (int index : currentBlock.indices)
        out.print(index + ",");
      out.println(value);
      nLinesLeftInBlock--;
    }
    if (out != null) out.close();
    
  }
  
  private static LinkedList<Block> readCodaIndex(File codaIndex)
  {
    LinkedList<Block> result = Lists.newLinkedList();
    for (String line : readLines(codaIndex))
    {
      Scanner lineScanner = new Scanner(line);
      List<String> variableAndIndex = on("\\]")
        .splitToList(lineScanner.next().replace("]",""));
      final int 
        firstInclusive = lineScanner.nextInt(),
        secondInclusive= lineScanner.nextInt();
      final int nSamples = secondInclusive - firstInclusive + 1;
      
      String variable = variableAndIndex.get(0);
      int [] indices = new int[variableAndIndex.size() - 1];
      for (int i = 0; i < indices.length; i++)
        indices[i] = Integer.parseInt(variableAndIndex.get(i+1));
      
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
  }
  
}
