package blang.accessibility;

import java.util.List;



public class ListConstituentNode extends ConstituentNode<Integer>
{
  public ListConstituentNode(Object container, Integer key)
  {
    super(container, key);
  }

  @Override
  public Object resolve()
  {
    @SuppressWarnings("rawtypes")
    List list = (List) container;
    return list.get(key);
  }
}