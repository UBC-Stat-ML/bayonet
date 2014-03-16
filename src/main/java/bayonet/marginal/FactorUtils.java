package bayonet.marginal;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;



public class FactorUtils
{
  public static <V> void checkIntegrity(Pair<V, V> messageToCompute,
      BinaryFactor<V> binaryFactor, List<UnaryFactor<V>> toMultiply)
  {
    V source = messageToCompute.getLeft(),
    destination = messageToCompute.getRight();
    if (!binaryFactor.marginalizedNode().equals(source))
      throw new RuntimeException();
    if (!binaryFactor.otherNode().equals(destination))
      throw new RuntimeException();
    for (UnaryFactor<V> factor : toMultiply)
      if (!factor.connectedVariable().equals(source))
        throw new RuntimeException();
  }
}
