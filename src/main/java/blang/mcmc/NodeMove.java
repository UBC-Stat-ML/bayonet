package blang.mcmc;

import java.util.Collections;
import java.util.List;


/**
 * A move that affects a single variable in the model.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public abstract class NodeMove implements Move
{

  private Object variable = null;

  @Override
  public List<?> variablesCovered()
  {
    return Collections.singletonList(variable);
  }

  public Object getVariable()
  {
    return variable;
  }

  public void setVariable(Object variable)
  {
    this.variable = variable;
  }

}
