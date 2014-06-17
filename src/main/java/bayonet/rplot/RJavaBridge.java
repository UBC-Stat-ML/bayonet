package bayonet.rplot;

import java.io.File;
import java.io.PrintWriter;

import briefj.BriefFiles;
import briefj.BriefIO;



public abstract class RJavaBridge
{
  public abstract String rTemplateResourceURL();
  
  public String javaDoublesToRVector(double [] numbers)
  {
    File tempFile = BriefFiles.createTempFile();
    PrintWriter out = BriefIO.output(tempFile);
    for (double number : numbers)
      out.println(number);
    out.close();
    return "scan('" + RUtils.escapeQuote(tempFile.getAbsolutePath()) + "')";
  }
  
  public String javaDoublesToRMatrix(double [][] numbers)
  {
    File tempFile = BriefFiles.createTempFile();
    PrintWriter out = BriefIO.output(tempFile);
    for (int r = 0; r < numbers.length; r++)
      for (int c = 0; c < numbers[0].length; c++)
        out.println(numbers[r][c]);
    out.close();
    return "matrix(scan('" + RUtils.escapeQuote(tempFile.getAbsolutePath()) + "'), " + numbers.length + ", " + numbers[0].length + ")";
  }
}