package bayonet.coda;


import static bayonet.coda.CodaParser.CSVToCoda;
import static bayonet.coda.CodaParser.codaToCSV;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import briefj.BriefFiles;

import com.google.common.io.Files;

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
  public void testParseWrite() throws IOException
  {
    File originalCodaIndex = new File("src/test/resources/CODAindex.txt");
    File originalCoda = new File("src/test/resources/CODAchain1.txt");
    
    File dest = 
      Files.createTempDir(); 
    //new File("/Users/bouchard/temp/created");
    
    codaToCSV(originalCodaIndex, originalCoda, dest);
    
    File newCodaIndex = 
      BriefFiles.createTempFile(); 
      //new File("/Users/bouchard/temp/created2/CODAindex.txt");
    File newCoda = BriefFiles.createTempFile(); //new File("/Users/bouchard/temp/created2/CODAchain1.txt");
    CSVToCoda(newCodaIndex, newCoda, dest);
    
    File finalDest = Files.createTempDir();
    
    codaToCSV(newCodaIndex, newCoda, finalDest);
    
    Assert.assertTrue(BriefFiles.ls(finalDest, "csv").size() == (BriefFiles.ls(dest, "csv").size()));
    
    Assert.assertTrue(!BriefFiles.ls(finalDest, "csv").isEmpty());
    
    for (File f : BriefFiles.ls(finalDest, "csv"))
    {
      File f2 = new File(dest, f.getName());
      Assert.assertTrue("Files do not match: " + f + " vs " + f2, Files.equal(f, f2));
    }
  }
}
