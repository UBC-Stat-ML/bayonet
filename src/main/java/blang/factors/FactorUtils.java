package blang.factors;

import java.util.Collection;

import blang.core.HasChildrenFactors;



public class FactorUtils
{
  public static void addFactorsRecursively(Factor f, Collection<Factor> toAddTo)
  {
    toAddTo.add(f);
    if (f instanceof HasChildrenFactors)
      for (Factor child : ((HasChildrenFactors) f).factors())
        addFactorsRecursively(child, toAddTo);
  }
}
