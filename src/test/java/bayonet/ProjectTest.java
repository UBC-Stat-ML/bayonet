package bayonet;

import org.junit.Test;

import bayonet.coda.TestCodaParser;

import tutorialj.Tutorial;


/**
 * # bayonet
 * 
 * Installing from source
 * ----------------------
 * 
 * Requires: gradle, git, eclipse
 * 
 * - Clone the repository
 * - Type ``gradle eclipse`` from the root of the repository
 * - From eclipse:
 *   - ``Import`` in ``File`` menu
 *   - ``Import existing projects into workspace``
 *   - Select the root
 *   - Deselect ``Copy projects into workspace`` to avoid having duplicates
 * - You can call bayonet.Main from the command line via ``./bayonet``
 *   - Move the executable ``bayonet`` to a PATH folder (or add this folder to PATH)
 *   to have access from anywhere
 *   - Changes done via eclipse will be reflected right away
 *   
 */
@Tutorial(startTutorial = "README.md", nextStep = TestCodaParser.class)
public class ProjectTest
{
  @Test
  public void test()
  {
    
  }
}
