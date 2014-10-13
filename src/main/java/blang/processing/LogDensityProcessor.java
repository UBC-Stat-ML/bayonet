package blang.processing;

import blang.variables.RealVariable;
import blang.variables.RealVariableProcessor;



public class LogDensityProcessor implements Processor
{
  private RealVariableProcessor processor = null;
  private RealVariable logDensityVariable = null;

  @Override
  public void process(ProcessorContext context)
  {
    ensureInit();
    logDensityVariable.setValue(context.getModel().logDensity());
    processor.process(context);
  }

  private void ensureInit()
  {
    if (processor == null)
    {
      logDensityVariable = new RealVariable(0.0);
      processor = new RealVariableProcessor("logDensity", logDensityVariable);
    }
  }

}
