package blang.annotations.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.junit.Assert;
import org.junit.Test;




public class RecursiveAnnotationProducerTest
{
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface MyAnnotation
  {
    public Class<? extends Number> [] test();
  }
  
  @MyAnnotation(test = Integer.class)
  public static interface AnInterface {}
  
  @MyAnnotation(test = Short.class)
  public static class BaseClass implements AnInterface {}

  @MyAnnotation(test = {Double.class, Long.class})
  public static class DescClass extends BaseClass implements AnInterface {}
  
  @Test
  public void test()
  {
    RecursiveAnnotationProducer<MyAnnotation, Class<? extends Number>> producer = RecursiveAnnotationProducer.ofClasses(MyAnnotation.class, true, "test");
    Collection<Class<? extends Number>> result = producer.getProducts(DescClass.class);
    LinkedHashSet<Class<? extends Number>> reference = new LinkedHashSet<>(Arrays.asList(Double.class, Long.class, Short.class, Integer.class));
    Assert.assertEquals(new LinkedHashSet<>(result), reference);
  }
  
}
