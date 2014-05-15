package bayonet.rplot;

import java.io.File;

import org.apache.commons.math3.analysis.MultivariateFunction;



/**
 * Creates a contour plot for a 2D function.
 * 
 * Usage:
 * PlotContour.fromFunction(a2DFunction).toPDF(aFile);
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 * @author Sean Jewell (jewellsean@gmail.com)
 */
public class PlotContourSaveFile extends RJavaBridge
{
  public double min_x = -1.0;
  public double max_x = 1.0;
  public double min_y = -1.0;
  public double max_y = 1.0;
  public int res_x = 100;
  public int res_y = 100;
  
  public double [] x, y;
  public double [][] z;
  
  /**
   * Center the view around zero with min/max of 
   * both x and y coordinates set at -/+borderDistance
   * @param borderDistance
   */
  public void centerToZero(double borderDistance)
  {
    this.min_x = -borderDistance;
    this.max_x = borderDistance;
    this.min_y = -borderDistance;
    this.max_y = borderDistance;
  }
  
  /**
   * 
   * @param f Should be a 2D function. 
   * @return
   */
  public static PlotContourSaveFile fromFunction(MultivariateFunction f)
  {
    return new PlotContourSaveFile(f);
  }
  
  private final MultivariateFunction function;
  private String message = null;
  
  /**
   * 
   * @param function
   */
  public PlotContourSaveFile(MultivariateFunction function)
  {
    this.function = function;
  }

  /**
   * Save to pdf 
   * 
   * @param output
   */
  public void toPDF(File output, File csvOut)
  {
    create();
    this.output = output;
    this.csvOutput = csvOut;
    message  = RUtils.callRBridge(this);
  }
  
  /**
   * @return The message returned by last call to R
   */
  public String getMessage()
  {
    return message;
  }
  
  private void create()
  {
    final double int_x = max_x - min_x,
                 int_y = max_y - min_y;
    x = new double[res_x]; 
    y = new double[res_y];
    for (int i = 0; i < res_x; i++) x[i] = min_x + ((double) i)/((double) res_x)*int_x;
    for (int i = 0; i < res_y; i++) y[i] = min_y + ((double) i)/((double) res_y)*int_y;
    z = new double[res_x][res_y];
    for (int i =0 ; i < res_x; i++)
      for (int j = 0; j < res_y; j++)
      {
        double [] cur = new double[]{x[j],y[i]};
        z[i][j] = function.value(cur);
      }
  }
  
  @Override public String rTemplateResourceURL() { return "/bayonet/rplot/PlotContourSaveFile.txt"; }

  private File output;
  public String getOutput()
  {
    return RUtils.escapeQuote(output.getAbsolutePath());
  }
  
  private File csvOutput;
  public String getCsvOutput()
  {
    return RUtils.escapeQuote(csvOutput.getAbsolutePath());
  }
  
  

}
