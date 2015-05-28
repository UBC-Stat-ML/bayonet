package bayonet.rplot;

import java.io.File;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import binc.Command;
import briefj.BriefFiles;




public class PlotHistogram extends RJavaBridge
{
  public final double lineWidth;
  public final Pair<Double, Double> xLimits, yLimits;
  public final double [] data;

  public PlotHistogram withXLimit(double min, double max)
  {
    Pair<Double, Double> xLimits = Pair.of(min, max);
    return new PlotHistogram(data, lineWidth, xLimits, yLimits);
  }
  
  public PlotHistogram withYLimit(double min, double max)
  {
    Pair<Double, Double> yLimits = Pair.of(min, max);
    return new PlotHistogram(data, lineWidth, xLimits, yLimits);
  }
  
  public PlotHistogram withLineWidth(double lineWidth)
  {
    return new PlotHistogram(data, lineWidth, xLimits, yLimits);
  }
  
  private PlotHistogram(double[] data, double lineWidth, Pair<Double, Double> xLimits,
      Pair<Double, Double> yLimits)
  {
    this.lineWidth = lineWidth;
    this.xLimits = xLimits;
    this.yLimits = yLimits;
    this.data = data;
  }

  public static PlotHistogram from(double [] data)
  {
    return new PlotHistogram(data);
  }
  
  public static PlotHistogram from(Collection<? extends Number> data)
  {
    double [] converted = new double[data.size()];
    int i = 0;
    for (Number item : data)
      converted[i++] = item.doubleValue();
    return from(converted);
  }
  
  public void toPDF(File output)
  {
    this.output = output;
    RUtils.callRBridge(this);
  }
  
  private static Command open = Command.byPath(new File("/usr/bin/open"));
  
  public void openTemporaryPDF()
  {
    this.output = BriefFiles.createTempFile("pdf");
    RUtils.callRBridge(this);
    Command.call(open.withArg(this.output.getAbsolutePath()));
  }
  
  private transient File output;
  
  public PlotHistogram(double[] data)
  {
    this(data, 6, null, null);
  }

  @Override public String rTemplateResourceURL() { return "/bayonet/rplot/PlotHistogram.txt"; }
  
  public String getOutput()
  {
    return RUtils.escapeQuote(output.getAbsolutePath());
  }
  public double[] getData()
  {
    return data;
  }
}