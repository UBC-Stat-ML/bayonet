package blang.accessibility;

import java.lang.reflect.Field;

import briefj.ReflexionUtils;



public class FieldConstituentNode extends ConstituentNode<Field> 
{
  public FieldConstituentNode(Object container, Field key)
  {
    super(container, key);
  }

  @Override
  public Object resolve()
  {
    if (key.getType().isPrimitive())
      return null;
    return ReflexionUtils.getFieldValue(key, container);
  }
}