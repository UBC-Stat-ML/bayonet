package bayonet.bugs;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import bayonet.rplot.RJavaBridge;
import bayonet.rplot.RUtils;
import briefj.BriefIO;

import com.beust.jcommander.Parameter;



public class RWrapper extends RJavaBridge implements Runnable
{
  @Parameter(names = "--model", description = "Path to the model")
  private String model = null;
  
  @Parameter(names = "--data", description = "Path to the main data file in tidy format")
  private String data = null;
  
  public String getDataFilePath() { return data; }
  
  public String getModelFileContents() { return BriefIO.fileToString(new File(model)); }
  
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
    // find out where the data is
      String ext = FilenameUtils.getExtension(model);
      String stem = ext.equals("") ? model : new File(model).getName().replaceFirst("[.]" + ext + "$", "");
      data = BugsWrapper.VAGRANT_PREFIX + "/" + stem + ".csv";
    }
    System.out.println("dataPath: " + data);

    if (!new File(data).exists())
    {
      System.err.println("A data file should be specified. Make sure you call your files question1.r, question2.r, etc.");
      System.exit(1);
    }
    
    // call R with generated skeleton R file
    System.out.println(RUtils.callRBridge(this));  
  }

  @Override
  public String rTemplateResourceURL() { return "/bayonet/bugs/RWrapper.txt"; }
}
