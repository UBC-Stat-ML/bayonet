package blang.annotations.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blang.annotations.Processors;
import blang.annotations.Samplers;
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
  private boolean useAnnotations = true;
  private final Class<A> annotationClass;
  
  public static interface Producer<I,P>
  {
    public P produce(I initiator, Class productType);
  }
  
  public AnnotationBasedFactory(
      Class<A> annotationClass)
  {
    this.annotationClass = annotationClass;
  }
  
  public void setUseAnnotation(boolean use)
  {
    this.useAnnotations = use;
  }

  private final Set<Class<? extends P>> 
    exclusions = Sets.newHashSet();
  
  private final Map<Class<? extends I>, Set<Class<? extends P>>>
    inclusions = Maps.newHashMap();
  
  public void exclude(Class<? extends P> product)
  {
    exclusions.add(product);
  }
  
  public void add(Class<? extends I> initiator, Class product)
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
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<P> build(Collection<? extends I> initiators, Producer<I,P> producer)
  {
    List<P> result = Lists.newArrayList();

    loop:for (I initiator : initiators)
    {
      A annotation = initiator.getClass().getAnnotation(annotationClass);
      
      Set<Class<? extends P>> productTypes = inclusions.get(initiator.getClass());
      if (productTypes == null)
        productTypes = Sets.newHashSet();
      
      if (annotation == null && productTypes.isEmpty())
        continue loop;
      
      if (useAnnotations)
      {
        Class [] productTypesFromAnnotations;
        
        if (annotationClass == Samplers.class)
          productTypesFromAnnotations = ((Samplers) annotation).value();
        else if (annotationClass == Processors.class)
          productTypesFromAnnotations = ((Processors) annotation).value();
        else
          throw new RuntimeException("Unknown type. Just fill in the boiler code in AnnotationBasedFactor.");

        ((Set) productTypes).addAll(Arrays.asList(productTypesFromAnnotations));
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
