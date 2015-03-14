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
    return new FactorList<T>(list, 0, true, false);
  }
  
  /**
   * 
   * @param list
   * @param makeStochastic Should the elements in the list be stochastic?
   * @return
   */
  public static <T> FactorList<T> ofArguments(List<T> list, boolean makeStochastic)
  {
    return new FactorList<T>(list, 0, false, makeStochastic);
  }
  
  public final List<T> list;
  
  @FactorComponent
  private final T component;
  
  @FactorArgument(makeStochastic = false)
  private final T argumentNonStoch;
  
  @FactorArgument(makeStochastic = true)
  private final T argumentStoch;
  
  @FactorComponent
  private final FactorList<T> next;
  
  private FactorList(List<T> list, int index, boolean asComponent, boolean makeStoch)
  {
    this.list = Collections.unmodifiableList(new ArrayList<T>(list));
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
    int nextIndex = index + 1;
    next = nextIndex < list.size() 
        ? new FactorList<T>(list, nextIndex, asComponent, makeStoch)
        : null;
  }
}