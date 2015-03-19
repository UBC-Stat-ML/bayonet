package bayonet.bugs;

import java.io.File;

import binc.Command;
import binc.Commands;
import briefj.BriefFiles;
import briefj.BriefIO;
import briefj.DefaultCharset;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;



public class StanWrapper
{
  /**
   * Compile a stan model. First check if we have a cached version
   * (within a foder called cachedModels created in stanHome).
   * Add it to the cache otherwise after compilation.
   * 
   * @param stanModelContents The string contents of the model
   * @param stanHome The folder containing stan's makefile
   * @return
   */
  public static File compile(String stanModelContents, File stanHome)
  {
    if (!stanHome.exists())
      throw new RuntimeException();
    
    File cachedDirectory = new File(stanHome, "cachedModels");
    cachedDirectory.mkdir();
    
    // try to find in cache
    Hasher hasher = Hashing.sha1().newHasher();
    hasher.putString(stanModelContents, DefaultCharset.defaultCharset);
    String hash = "model_" + hasher.hash().toString();
    for (File previous : BriefFiles.ls(cachedDirectory))
      if (previous.getName().equals(hash))
        return previous;
    
    // write the model file
    File modelFile = new File(cachedDirectory, "" + hash + ".stan");
    BriefIO.write(modelFile, stanModelContents);
    
    // compile it
    String arg = modelFile.getAbsolutePath().replaceAll("[.]stan$", "");
    Command.call(Commands.make.ranIn(stanHome).withArg(arg).withStandardOutMirroring());
    
    File output = new File(cachedDirectory, hash);
    if (!output.exists())
      throw new RuntimeException();
    
    return output;
  }
}
