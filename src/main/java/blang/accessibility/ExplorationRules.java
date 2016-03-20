package blang.accessibility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import briefj.ReflexionUtils;



public class ExplorationRules
{
  public static List<ExplorationRule> defaultExplorationRules = Arrays.asList(
      ExplorationRules::arrayViews,
      ExplorationRules::arrays,
      ExplorationRules::knownImmutableObjects,
      ExplorationRules::standardObjects);
  
  public static List<ArrayConstituentNode> arrays(Object object)
  {
    Class<? extends Object> c = object.getClass();
    if (!c.isArray())
      return null;
    ArrayList<ArrayConstituentNode> result = new ArrayList<>();
    final int length = Array.getLength(object);
    for (int i = 0; i < length; i++)
      result.add(new ArrayConstituentNode(object, i));
    return result;
  }
  
  public static List<ArrayConstituentNode> arrayViews(Object object)
  {
    if (!(object instanceof ArrayView))
      return null;
    ArrayList<ArrayConstituentNode> result = new ArrayList<>();
    ArrayView view = (ArrayView) object;
    List<Field> annotatedDeclaredFields = ReflexionUtils.getAnnotatedDeclaredFields(view.getClass(), ViewedArray.class, true);
    if (annotatedDeclaredFields.size() != 1)
      throw new RuntimeException();
    Object array = ReflexionUtils.getFieldValue(annotatedDeclaredFields.get(0), view);
    for (int index : view.viewedIndices)
      result.add(new ArrayConstituentNode(array, index));
    return result;
  }
  
  private static abstract class ArrayView
  {
    public final ImmutableList<Integer> viewedIndices;

    public ArrayView(ImmutableList<Integer> viewedIndices)
    {
      this.viewedIndices = viewedIndices;
    }
  }
  
  public static final class ObjectArrayView<T> extends ArrayView
  {
    @ViewedArray
    private final T[] viewedArray;
    
    public ObjectArrayView(ImmutableList<Integer> viewedIndices, T[] viewedArray)
    {
      super(viewedIndices);
      this.viewedArray = viewedArray;
    }

    public T get(int indexIndex)
    {
      return viewedArray[viewedIndices.get(indexIndex)];
    }
    
    public void set(int indexIndex, T object)
    {
      viewedArray[viewedIndices.get(indexIndex)] = object;
    }
  }
  
  public static final class DoubleArrayView<T> extends ArrayView
  {
    @ViewedArray
    private final double[] viewedArray;
    
    public DoubleArrayView(ImmutableList<Integer> viewedIndices, double[] viewedArray)
    {
      super(viewedIndices);
      this.viewedArray = viewedArray;
    }

    public double get(int indexIndex)
    {
      return viewedArray[viewedIndices.get(indexIndex)];
    }
    
    public void set(int indexIndex, double object)
    {
      viewedArray[viewedIndices.get(indexIndex)] = object;
    }
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD})
  public static @interface ViewedArray
  {
  }
  
  // bad idea, since other fields will then not be processed!
//  public static interface ArrayView
//  {
//  }
//  
//  @Retention(RetentionPolicy.RUNTIME)
//  @Target({ElementType.FIELD})
//  public static @interface ViewedArray
//  {
//    public int index() default 0;
//  }
//  
//  @Retention(RetentionPolicy.RUNTIME)
//  @Target({ElementType.FIELD})
//  public static @interface ViewedIndices
//  {
//    public int index() default 0;
//  }
  
  // not safe: e.g. bad behavior if a list of integer is used, with some 
  // entries initialized to null, and a sublist is created on that. Then 
  // the sublist will miss some dependencies. 
  // Actually, even simpler problem: with the same example even without nulls,
  // these dependencies will be ignored because they are on immutable objects
//  public static List<ListConstituentNode> listExplorationRule(Object object)
//  {
//    if (!(object instanceof List))
//      return null;
//    @SuppressWarnings({ "unchecked" })
//    List<? extends Object> list = (List<? extends Object>) object;
//    ArrayList<ListConstituentNode> result = new ArrayList<>();
//    for (int i = 0; i < list.size(); i++)
//      result.add(new ListConstituentNode(object, i));
//    return result;
//  }
  
  public static List<? extends ConstituentNode<?>> knownImmutableObjects(Object object)
  {
    if (object instanceof String || object instanceof Number)
      return Collections.emptyList();
    else
      return null;
  }
  
  // standard object as in not an array object
  public static List<FieldConstituentNode> standardObjects(Object object)
  {
    ArrayList<FieldConstituentNode> result = new ArrayList<>();
    
    // process all enclosing classes, if any
    Object outerObject = ReflexionUtils.getOuterClass(object);
    if (outerObject != null)
      result.addAll(standardObjects(outerObject));
  
    // find all fields (including those of super class(es), recursively, if any
    for (Field f : ReflexionUtils.getDeclaredFields(object.getClass(), true))
      result.add(new FieldConstituentNode(object, f));
    
    return result;
  }
}
