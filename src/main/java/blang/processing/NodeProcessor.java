package blang.processing;



public interface NodeProcessor<T> extends Processor
{
  public void setReference(T variable);
}
