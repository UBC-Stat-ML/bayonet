package blang.factors;

import java.util.Collection;

import blang.core.Model;
import blang.core.ModelComponent;



public class FactorUtils
{
  public static void addModelComponentsRecursively(ModelComponent modelComponent, Collection<ModelComponent> toAddTo)
  {
    if (!(modelComponent instanceof Factor) && !(modelComponent instanceof Model))
      throw new RuntimeException();
    
    toAddTo.add(modelComponent);
    if (modelComponent instanceof Model)
      for (ModelComponent child : ((Model) modelComponent).components())
        addModelComponentsRecursively(child, toAddTo);
  }
}
