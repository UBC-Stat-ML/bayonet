package blang.accessibility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class AccessibilityGraphTest
{

  
  public static void main(String [] args)
  {
    ArrayList<Mutable> full = new ArrayList<>();
    
    for (int i = 0; i < 3; i++)
    {
      Mutable m = new Mutable();
      m.value = "#" + i;
      full.add(m);
    }
    
    Factor1 f1 = new Factor1(full);
    
    AccessibilityGraph g = AccessibilityGraph.inferGraph(f1);
    
    g.toDotFile(new File("temp.dot"));
  }
  
  // TODO: anonymous inner things!!
  
  // TODO: self loops
  
  static class Factor1
  {
    final List<Mutable> list;
    Factor1(List<Mutable> m) { this.list = m; }
  }
  
  static class Mutable
  {
    String value;
    @Override
    public String toString()
    {
      return value;
    }
  }
}
