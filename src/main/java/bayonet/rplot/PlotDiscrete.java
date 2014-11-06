package bayonet.rplot;

import java.io.File;
import java.util.Collection;

/**
 * Plot PMF, CDF, and tables for discrete valued distributions
 * This is based off of Alex's coda.discrete.utils which is located on Github: 
 * https://github.com/alexandrebouchard/coda-discrete-utils
 * @author jewellsean
 *
 */
public class PlotDiscrete extends RJavaBridge{

	public final int [] data;
	  
	  private PlotDiscrete(int[] data, String variableName)
	  {
	    this.data = data;
	    this.variableName = variableName;
	  }

	  public static PlotDiscrete from(int [] data, String variableName)
	  {
	    return new PlotDiscrete(data, variableName);
	  }
	  
	  public static PlotDiscrete from(Collection<Integer> data, String variableName)
	  {
	    int [] converted = new int[data.size()];
	    int i = 0;
	    for (int item : data)
	      converted[i++] = item;
	    return from(converted, variableName);
	  }
	  
	  public void toPDF(File output)
	  {
	    this.output = output;
	    RUtils.callRBridge(this);
	  }
	  
	  private transient File output;

	  @Override public String rTemplateResourceURL() { return "/bayonet/rplot/PlotDiscrete.txt"; }
	  
	  private String variableName;
	  
	  public String getVarName()
	  {
		  return RUtils.escapeQuote(variableName);
	  }
	  
	  public String getOutput()
	  {
	    return RUtils.escapeQuote(output.getAbsolutePath());
	  }
	  
	  public int[] getData()
	  {
	    return data;
	  }

	
}
