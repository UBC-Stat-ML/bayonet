package bayonet;

import static briefj.CommandLineUtils.start;
import bayonet.bugs.BugsWrapper;


public class Main
{
  
  public static void main(String [] args)
  {
    start(new BugsWrapper(), args);
  }

}
