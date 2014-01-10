package bayonet;

import static briefj.CommandLineUtils.start;
import bayonet.bugs.BugsWrapper;


public class Main
{
  
  public static void main(String [] args)
  {
    try 
    {
      start(new BugsWrapper(), args);
    }
    catch (Exception e)
    {
      System.err.println(e.getMessage());
    }
  }

}
