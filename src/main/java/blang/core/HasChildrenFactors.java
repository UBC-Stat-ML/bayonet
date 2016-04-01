package blang.core;

import java.util.Collection;

import blang.factors.Factor;



public interface HasChildrenFactors
{
  public Collection<Factor> getChildrenFactors();
}
