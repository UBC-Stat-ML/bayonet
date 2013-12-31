package bayonet;

import static briefj.CommandLineUtils.*;
import static briefj.BriefIO.*;
import tutorialj.Tutorial;

import com.beust.jcommander.Parameter;


public class Main implements Runnable
{
  @Parameter(names = "--input", required = true)
  private String input;
  
  public static void main(String [] args)
  {
    start(new Main(), args);
  }

  @Override
  public void run()
  {
    System.out.println("Hello world");
    for (String line : readLines(input))
      System.out.println(line);
  }
}
