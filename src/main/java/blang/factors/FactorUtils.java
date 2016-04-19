package blang.factors;

import java.util.Collection;

import blang.core.FactorComposite;
import blang.core.ModelComponent;



public class FactorUtils
{
  public static void addModelComponentsRecursively(ModelComponent modelComponent, Collection<ModelComponent> toAddTo)
  {
    if (!(modelComponent instanceof Factor) && !(modelComponent instanceof FactorComposite))
      throw new RuntimeException();
    
    toAddTo.add(modelComponent);
    if (modelComponent instanceof FactorComposite)
      for (ModelComponent child : ((FactorComposite) modelComponent).components())
        addModelComponentsRecursively(child, toAddTo);
  }
}
