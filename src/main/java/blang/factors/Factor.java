package blang.factors;


// TODO: other types of factors?
// - derivative factor
// - hessian factor
// - interval factor
// - deterministic factor which would not inherit Factor
// actually: deterministic and stochastic factors as separate
// (Factor: StochasticFactor,   

// TODO: make the @SubModel recursion automatic on factors,
// so that interval factors can be added within a factor (although may get tricky because of init order)

// TODO: simplify back factors? there seems not real reason for this complexity

// TODO: should we do plates? if so, how to do plates?

public interface Factor
{
  public double logDensity();
}
