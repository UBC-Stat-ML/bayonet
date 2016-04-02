package blang.factors;

import java.util.Collection;

import blang.core.CompositeFactors;



public class FactorUtils
{
  public static void addFactorsRecursively(Factor f, Collection<Factor> toAddTo)
  {
    toAddTo.add(f);
    if (f instanceof CompositeFactors)
      for (Factor child : ((CompositeFactors) f).componentFactors())
        addFactorsRecursively(child, toAddTo);
  }
}
