package blang.annotations.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import briefj.BriefMaps;
import briefj.ReflexionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;



/**
 * Automate certain types of factories via annotations.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 * @param <V>
 * @param <A>
 * @param <P>
 */
public class AnnotationBasedFactory<I,A extends Annotation,P>
{
  private final boolean useAnnotations;
  private final Class<A> annotationClass;
  
  public static interface Producer<I,P>
  {
    public P produce(I initiator, Class<? extends P> productType);
  }
  
  public AnnotationBasedFactory(
      boolean useAnnotations,
      Class<A> annotationClass)
  {
    this.useAnnotations = useAnnotations;
    this.annotationClass = annotationClass;
  }

  private final Set<Class<? extends P>> 
    exclusions = Sets.newHashSet();
  
  private final Map<Class<? extends I>, Set<Class<? extends P>>>
    inclusions = Maps.newHashMap();
  
  public void exclude(Class<? extends P> product)
  {
    exclusions.add(product);
  }
  
  public void add(Class<? extends I> initiator, Class<? extends P> product)
  {
    BriefMaps.getOrPutSet(inclusions, initiator).add(product);
  }

  /**
   * If useAnnotation is set to true: Reads in a list of objects of type I.
   * 
   * For each object i, the factory looks at the annotation A in the class 
   * declaration of i.
   * 
   * A is assumed to contained a function with signature
   * public Class<? extends P>[] value();
   * 
   * For each i and type t returned by value(), the Producer is 
   * used to instantiate a product p of type t, or null.
   * 
   * In addition to that, inclusions and exclusions are used similarly.
   * 
   * All the products p obtained in this fashion are returned. 
   * @param initiators
   * @param producer
   * @return
   */
  public List<P> build(Collection<? extends I> initiators, Producer<I,P> producer)
  {
    List<P> result = Lists.newArrayList();

    loop:for (I initiator : initiators)
    {
      A annotation = initiator.getClass().getAnnotation(annotationClass);
      
      Set<Class<? extends P>> productTypes = inclusions.get(initiator.getClass());
      
      if (annotation == null && productTypes == null)
        continue loop;
      
      if (useAnnotations)
      {
        try
        {
          @SuppressWarnings("unchecked")
          Class<? extends P> [] productTypesFromAnnotations = (Class<? extends P>[]) ReflexionUtils.callMethod(initiator, "value");
          productTypes.addAll(Arrays.asList(productTypesFromAnnotations));
        }
        catch (Exception e)
        {
          throw new RuntimeException("Annotation " + annotationClass.getName() + " is assumed to contained  a function of type " +
          		"public Class<? extends P>[] value(); note that this cannot be statically enforced because of a java language limitation " +
          		"(annotation cannot implement interfaces as of Java 6.");
        }
      }
    
      for (Class<? extends P> productType : productTypes)
        if (!exclusions.contains(productType))
        {
          P product = producer.produce(initiator, productType);
          if (product != null)
            result.add(product);
        }
    }
    
    return result;
  }
}
