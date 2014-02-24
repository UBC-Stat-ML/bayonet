package blang.parsing;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blang.ProbabilityModel;
import briefj.BriefLists;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;



public class VariableNames
{
  private final Map<Node,String> shortNames = Maps.newHashMap();
  private final Map<Node,List<Field>> longNames = Maps.newHashMap();
  private final Set<String> 
    allShortNames = Sets.newHashSet(),
    collisions = Sets.newHashSet();   
  
  public void add(Node variable, List<Field> fieldsPath)
  {
    if (variable.isFactor())
      throw new RuntimeException();
    String shortName = BriefLists.last(fieldsPath).getName();
    if (allShortNames.contains(shortName))
      collisions.add(shortName);
    allShortNames.add(shortName);
    shortNames.put(variable, shortName);
    longNames.put(variable, fieldsPath);
  }
  
  public Field getSimpleField(Node variable)
  {
    return BriefLists.last(longNames.get(variable));
  }
  
  public String get(Node variable)
  {
    String shortName = shortNames.get(variable);
    if (collisions.contains(shortName))
      return ProbabilityModel.fieldsPathToString(longNames.get(variable));
    else
      return shortName;
  }
}