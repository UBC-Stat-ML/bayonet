package blang.factors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;


/**
 * A list of FactorArguments or FactorComponents, useful for 
 * factors with varying arity.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <T>
 */
public class FactorList<T>
{
  public static <T> FactorList<T> ofComponents(List<T> list)
  {
    list = Collections.unmodifiableList(new ArrayList<T>(list));
    return new FactorList<T>(list, 0, list.size(), true, false);
  }
  
  /**
   * 
   * @param list
   * @param makeStochastic Should the elements in the list be stochastic?
   * @return
   */
  public static <T> FactorList<T> ofArguments(List<T> list, boolean makeStochastic)
  {
    list = Collections.unmodifiableList(new ArrayList<T>(list));
    return new FactorList<T>(list, 0, list.size(), false, makeStochastic);
  }
  
  public final List<T> list;
  
  @FactorComponent
  private final T component;
  
  @FactorArgument(makeStochastic = false)
  private final T argumentNonStoch;
  
  @FactorArgument(makeStochastic = true)
  private final T argumentStoch;
  
  @FactorComponent
  private final FactorList<T> left;
  
  @FactorComponent
  private final FactorList<T> right;
  
  private FactorList(List<T> list, int leftIncl, int rightExcl, boolean asComponent, boolean makeStoch)
  {
    this.list = list;
    if (rightExcl == leftIncl)
    {
      component = null;
      argumentNonStoch = null;
      argumentStoch = null;
      left = null;
      right = null;
    }
    else
    {
      int index = leftIncl;
      this.component = asComponent ? list.get(index) : null;
      if (!asComponent)
      {
        this.argumentStoch = makeStoch ? list.get(index) : null;
        this.argumentNonStoch = makeStoch ? null : list.get(index);
      }
      else
      {
        this.argumentNonStoch = null;
        this.argumentStoch = null;
      }
      int recLeftIncl = leftIncl + 1;
      int mid = recLeftIncl + (rightExcl - recLeftIncl) / 2;
      left  = new FactorList<T>(list, recLeftIncl, mid,       asComponent, makeStoch);
      right = new FactorList<T>(list, mid,         rightExcl, asComponent, makeStoch);
    }
  }
}