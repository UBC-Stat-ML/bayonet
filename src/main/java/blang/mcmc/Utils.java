package blang.mcmc;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import blang.factors.Factor;
import briefj.BriefCollections;
import briefj.BriefLists;
import briefj.ReflexionUtils;

import com.google.common.collect.Lists;



public class Utils
{
  // TODO: this stuff needs to be tested more extensively
  
  
  
  public static boolean isFactorAssignmentCompatible(
      List<? extends Factor> factors, 
      List<Field> fieldsToPopulate)
  {
    return assignFactorConnections(null, factors, fieldsToPopulate, true);
  }
  
  public static void assignFactorConnections(Operator mcmcMoveInstance, 
      List<? extends Factor> factors, 
      List<Field> fieldsToPopulate)
  {
    boolean result = assignFactorConnections(mcmcMoveInstance, factors, fieldsToPopulate, false);
    if (!result)
      throw new RuntimeException();
  }
  
  private static boolean assignFactorConnections(
      Operator mcmcMoveInstance, 
      List<? extends Factor> factors, 
      List<Field> fieldsToPopulate,
      boolean onlyPeek)
  {
    fieldsToPopulate = BriefLists.sort(fieldsToPopulate, fieldsComparator);
    factors = Lists.newLinkedList(factors);
    for (Field field : fieldsToPopulate)
      if (List.class.isAssignableFrom(field.getType()))
        assignListConnection(mcmcMoveInstance, field, factors, onlyPeek);
      else
        assignSingleConnection(mcmcMoveInstance, field, factors, onlyPeek);
    return factors.isEmpty();
  }
  
  private static void assignSingleConnection(Operator mcmcMoveInstance, Field field, List<? extends Factor> factors, boolean onlyPeek)
  {
    Iterator<? extends Factor> iterator = factors.iterator();
    while (iterator.hasNext())
    {
      Factor factor = iterator.next();
      if (field.getType().isAssignableFrom(factor.getClass()))
      {
        if (!onlyPeek)
          ReflexionUtils.setFieldValue(field, mcmcMoveInstance, factor);
        iterator.remove();
        return;
      }
    }
  }

  public static void assignVariable(Operator mcmcMoveInstance, Object variable)
  {
    Field field = getSampledVariableField(mcmcMoveInstance.getClass());
    ReflexionUtils.setFieldValue(field, mcmcMoveInstance, variable);
  }
  
  public static Field getSampledVariableField(Class<? extends Operator> moveType)
  {
    List<Field> matches = ReflexionUtils.getAnnotatedDeclaredFields(moveType, SampledVariable.class, true);
    if (matches.size() != 1)
      throw new RuntimeException("There should be exactly one @" + SampledVariable.class.getSimpleName() + " annotated field" +
          " in " + moveType);
    return BriefCollections.pick(matches);
  }

  private static void assignListConnection(Operator mcmcMoveInstance, Field field, List<? extends Factor> factors, boolean onlyPeek)
  {
    List<? super Factor> fieldList = onlyPeek ? null : Lists.newArrayList();
    if (!onlyPeek)
      ReflexionUtils.setFieldValue(field, mcmcMoveInstance, fieldList);
    Iterator<? extends Factor> iterator = factors.iterator();
    Class<?> genericType = getGenericType(field);
    while (iterator.hasNext())
    {
      Factor factor = iterator.next();
      
      if (genericType.isAssignableFrom(factor.getClass()))
      {
        if (!onlyPeek)
          fieldList.add(factor);
        iterator.remove();
      }
    }
  }


  public static Comparator<Field> fieldsComparator = new Comparator<Field>() {

    @Override
    public int compare(Field f1, Field f2)
    {
      final Class<?> 
        t1 = getType(f1),
        t2 = getType(f2);
      final boolean 
        t2ExtendsT1 = t1.isAssignableFrom(t2),
        t1ExtendsT2 = t2.isAssignableFrom(t1);
      if (t2ExtendsT1 && t1ExtendsT2)
      {
        // if the two are of the same type, put non-list first
        final boolean 
          o1IsList = List.class.isAssignableFrom(f1.getType()),
          o2IsList = List.class.isAssignableFrom(f2.getType());
        if (o1IsList && !o2IsList)
          return 1;
        if (o2IsList && !o1IsList)
          return -1;
      }
      else
      {
        // enumerate most specific types first
        if (t2ExtendsT1)
          return 1;
        if (t1ExtendsT2)
          return -1;
      }
      // otherwise, sort in alphabetic order of field name
      return f1.getName().compareTo(f2.getName());
    }

    private Class<?> getType(Field field)
    {
      if (Factor.class.isAssignableFrom(field.getType())) 
        return field.getType();
      if (List.class.isAssignableFrom(field.getType()))
        return getGenericType(field);
      throw new RuntimeException("Fields annotated by @ConnectedFactor should be of type Factor or List: " + field);
    }
  };
  
  public static Class<?> getGenericType(Field field)
  {
    ParameterizedType genericType = (ParameterizedType) field.getGenericType();
    return (Class<?>) genericType.getActualTypeArguments()[0];
  }


  
//  public static class TestClass 
//  {
//    Factor aFactor;
//    Factor bFactor;
//    List<Factor> aaaa;
//    Exponential aexp;
//    List<Exponential> bexp;
//  }
//  
  public static void main(String [] args)
  {
    System.out.println("@" + SampledVariable.class.getSimpleName());
//    
//    List<Field> fields = ReflexionUtils.getDeclaredFields(TestClass.class, true);
//    Collections.sort(fields, fieldsComparator);
//    for (Field f : fields)
//      System.out.println(f.getName());
  }
}
