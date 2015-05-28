package bayonet.rplot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

import com.google.common.primitives.Doubles;

import binc.Command;
import briefj.BriefFiles;
import briefj.BriefLists;




public class PlotLine extends RJavaBridge
{
  public final double [] x, y;
  
  private PlotLine(List<? extends Number> xList, List<? extends Number> yList)
  {
    if (xList.size() != yList.size())
      throw new RuntimeException();
    x = Doubles.toArray(xList);
    y = Doubles.toArray(yList);
  }

  public static PlotLine from(List<? extends Number> x, List<? extends Number> y)
  {
    return new PlotLine(x, y);
  }
  
  public static PlotLine fromFunction(DoubleFunction<Double> function, double minX, double maxX)
  {
    List<Double> 
      xs = new ArrayList<>(),
      ys = new ArrayList<>();
    double deltaX = (maxX - minX) / 1000;
    if (deltaX <= 0)
      throw new RuntimeException();
    for (double x = minX; x <= maxX; x += deltaX)
    {
      xs.add(x);
      ys.add(function.apply(x));
    }
    return new PlotLine(xs, ys);
  }
  
  public static PlotLine from(List<? extends Number> y)
  {
    return new PlotLine(BriefLists.integers(y.size()).asList(), y);
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

  @Override public String rTemplateResourceURL() { return "/bayonet/rplot/PlotLine.txt"; }
  
  public String getOutput()
  {
    return RUtils.escapeQuote(output.getAbsolutePath());
  }
}