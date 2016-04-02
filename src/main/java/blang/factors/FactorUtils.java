package blang.factors;

import java.util.Collection;

import blang.core.FactorComposite;



public class FactorUtils
{
  public static void addFactorsRecursively(Object factorOrComposite, Collection<Factor> toAddTo)
  {
    if (factorOrComposite instanceof Factor)
      toAddTo.add((Factor) factorOrComposite);
    if (factorOrComposite instanceof FactorComposite)
      for (Factor child : ((FactorComposite) factorOrComposite).componentFactors())
        addFactorsRecursively(child, toAddTo);
  }
}
