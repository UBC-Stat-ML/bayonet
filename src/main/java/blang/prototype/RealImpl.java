package blang.prototype;



public class RealImpl implements Real
{
  private double value;

  @Override
  public double get()
  {
    return value;
  }

  @Override
  public void set(double value)
  {
    this.value = value;
  }

}
