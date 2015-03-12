package blang.factors;

import java.util.Collections;
import java.util.List;

import blang.annotations.FactorComponent;



public class FactorComponentList<T>
{
  @FactorComponent
  public final T item;
  
  @FactorComponent
  public final FactorComponentList<T> next;
  
  public final List<T> list;
  
  private FactorComponentList(List<T> list, int index)
  {
    this.list = Collections.unmodifiableList(list);
    this.item = list.get(index);
    int nextIndex = index + 1;
    next = nextIndex < list.size() 
        ? new FactorComponentList<T>(list, nextIndex)
        : null;
  }
  
  public FactorComponentList(List<T> list)
  {
    this(list, 0);
  }
}