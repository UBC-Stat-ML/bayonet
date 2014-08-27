package bayonet.rplot;

import java.io.File;
import java.io.PrintWriter;

import briefj.BriefFiles;
import briefj.BriefIO;
import briefj.BriefStrings;



public abstract class RJavaBridge
{
  public abstract String rTemplateResourceURL();
  
  /**
   * Folder to put generated R script, csv used to transfer data,
   * etc. If null, all these will be temporary.
   */
  private File workFolder = null;
  
  public String javaDoublesToRVector(double [] numbers)
  {
    File tempFile = tempFile(); 
    PrintWriter out = BriefIO.output(tempFile);
    for (double number : numbers)
      out.println(number);
    out.close();
    return "scan('" + RUtils.escapeQuote(tempFile.getAbsolutePath()) + "')";
  }
  
  private File tempFile()
  {
    if (workFolder == null)
      return BriefFiles.createTempFile();
    String id = BriefStrings.generateUniqueId();
    return new File(workFolder, id);
  }

  public String javaDoublesToRMatrix(double [][] numbers)
  {
    File tempFile = tempFile(); //BriefFiles.createTempFile();
    PrintWriter out = BriefIO.output(tempFile);
    for (int r = 0; r < numbers.length; r++)
      for (int c = 0; c < numbers[0].length; c++)
        out.println(numbers[r][c]);
    out.close();
    return "matrix(scan('" + RUtils.escapeQuote(tempFile.getAbsolutePath()) + "'), " + numbers.length + ", " + numbers[0].length + ")";
  }

  public File getWorkFolder()
  {
    return workFolder;
  }

  public void setWorkFolder(File workFolder)
  {
    this.workFolder = workFolder;
  }

}