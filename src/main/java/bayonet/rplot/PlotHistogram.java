package bayonet.rplot;

import java.io.File;
import java.util.Collection;




public class PlotHistogram extends RJavaBridge
{
  public double lineWidth = 6;
  
  public static PlotHistogram from(double [] data)
  {
    return new PlotHistogram(data);
  }
  
  public static PlotHistogram from(Collection<Double> data)
  {
    double [] converted = new double[data.size()];
    int i = 0;
    for (double item : data)
      converted[i++] = item;
    return from(converted);
  }
  
  public void toPDF(File output)
  {
    this.output = output;
    RUtils.callRBridge(this);
  }
  
  private File output;
  private final double [] data;
  
  public PlotHistogram(double[] data)
  {
    this.data = data;
  }

  @Override public String rTemplateResourceURL() { return "/bayonet/rplot/PlotHistogram.txt"; }

  public String getOutput()
  {
    return output.getAbsolutePath();
  }
  public double[] getData()
  {
    return data;
  }
}