package bayonet.coda;

import static briefj.BriefIO.createTempFile;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import tutorialj.Tutorial;

import static briefj.BriefIO.*;

import bayonet.bugs.TestWrapper;

import com.google.common.io.Files;

import static bayonet.coda.CodaParser.*;

public class TestCodaParser
{
  /**
   * Coda utils
   * ----------
   * 
   * Coda is a format used by WinBugs and JAGS to store MCMC samples.
   * ``codaToCSV()`` converts to a simpler format, with each variable in a
   * separate, self-explanatory csv file.
   */
  @Test
  @Tutorial(showSource = true, nextStep = TestWrapper.class)
  public void testParseWrite() throws IOException
  {
    File originalCodaIndex = new File("src/test/resources/CODAindex.txt");
    File originalCoda = new File("src/test/resources/CODAchain1.txt");
    
    File dest = Files.createTempDir(); //new File("/Users/bouchard/temp/created");
    
    codaToCSV(originalCodaIndex, originalCoda, dest);
    
    File newCodaIndex = createTempFile(); //new File("/Users/bouchard/temp/created2/CODAindex.txt");
    File newCoda = createTempFile(); //new File("/Users/bouchard/temp/created2/CODAchain1.txt");
    CSVToCoda(newCodaIndex, newCoda, dest);
    
    File finalDest = Files.createTempDir();
    
    codaToCSV(newCodaIndex, newCoda, finalDest);
    
    Assert.assertTrue(ls(finalDest, "csv").size() == (ls(dest, "csv").size()));
    
    Assert.assertTrue(!ls(finalDest, "csv").isEmpty());
    
    for (File f : ls(finalDest, "csv"))
    {
      File f2 = new File(dest, f.getName());
      Assert.assertTrue("Files do not match: " + f + " vs " + f2, Files.equal(f, f2));
    }
  }
}
