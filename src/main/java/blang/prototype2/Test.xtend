package blang.prototype2

import static briefj.ReflexionUtils.getDeclaredFields;

class Test {
  
  static interface Real {
    def double doubleValue()
  }
  
  static interface Vec<T> {
    def T get(int i)
    def int size()
  }
  
  interface MyInterface {
    
  }
  
  static class Normal {
    ()=>Real mean
    
    new (()=>Real mean) {
      this.mean = mean
    }
  }
  
  def static myTest() {
    val Object obj = "asdfsd"
    val Object obj2 = "asdfasdfasdfa"
    return ["asdf" + obj + obj2]
  }
  
  def static void main(String [] args) {
    
    val MyInterface i = null;
    
    val Object test = myTest();
    println(getDeclaredFields(test.class, true))
    println(test)
    
//    val Real mu = [5.0]
//    val Vec<Real> vec = new Vec<Real>() {
//      override get(int i) {
//        
//      }
//      override size() {
//        0
//      }
//    }
//    
//    val aNormal = new Normal([   [mu.doubleValue + 5.0]   ])
//    
//    val anotherNormal = new Normal([ vec.get(0)  ])
  }
}