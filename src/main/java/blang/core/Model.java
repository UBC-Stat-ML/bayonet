package blang.core;

import java.util.Collection;



/**
 * A class which is not necessarily a factor itself, but that contains 
 * components which themselves may be Factors or Models 
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface Model extends ModelComponent
{
  public Collection<? extends ModelComponent> components();
}
