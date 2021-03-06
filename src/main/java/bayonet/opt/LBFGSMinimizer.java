package bayonet.opt;


import java.util.LinkedList;

/**
 * @author Dan Klein
 */
public class LBFGSMinimizer implements GradientMinimizer {
  double EPS = 1e-10;
  int maxIterations = 20;
  int maxHistorySize = 5;
  LinkedList<double[]> inputDifferenceVectorList = new LinkedList<double[]>();
  LinkedList<double[]> derivativeDifferenceVectorList = new LinkedList<double[]>();
  public boolean verbose = false;

  private int nRequiredIterations;
  private Boolean converged = null;
  public double[] minimize(DifferentiableFunction function, double[] initial, double tolerance) {
    BacktrackingLineSearcher lineSearcher = new BacktrackingLineSearcher();
    converged = false;
    double[] guess = DoubleArrays.clone(initial);
    int iteration;
    for (iteration = 0; iteration < maxIterations; iteration++) {
      double value = function.valueAt(guess);
      if (verbose)
        System.out.println("Value before iteration " + iteration + ": " + value);
      double[] derivative = function.derivativeAt(guess);
      double[] initialInverseHessianDiagonal = getInitialInverseHessianDiagonal(function);
      double[] direction = implicitMultiply(initialInverseHessianDiagonal, derivative);
      DoubleArrays.scale(direction, -1.0);
      if (iteration == 0)
        lineSearcher.stepSizeMultiplier = 0.01;
      else
        lineSearcher.stepSizeMultiplier = 0.5;
      double[] nextGuess = lineSearcher.minimize(function, guess, direction);
      double nextValue = function.valueAt(nextGuess);
      double[] nextDerivative = function.derivativeAt(nextGuess);
      if (converged(value, nextValue, tolerance))
      {
        converged = true;
        return nextGuess;
      }
      updateHistories(guess, nextGuess, derivative,  nextDerivative);
      guess = nextGuess;
      value = nextValue;
      derivative = nextDerivative;
    }
    this.nRequiredIterations = iteration;
    System.out.println("LBFGSMinimizer.minimize: Exceeded maxIterations without converging.");
    return guess;
  }
  
  public boolean converged()
  {
    if (converged == null)
      throw new RuntimeException("Convergence status undefined before calling miminize()");
    return converged;
  }

  private boolean converged(double value, double nextValue, double tolerance) {
    if (value == nextValue)
      return true;
    double valueChange = Math.abs(nextValue - value);
    double valueAverage = Math.abs(nextValue + value + EPS) / 2.0;
    if (valueChange / valueAverage < tolerance)
      return true;
    return false;
  }

  private void updateHistories(double[] guess, double[] nextGuess, double[] derivative, double[] nextDerivative) {
    double[] guessChange = DoubleArrays.addMultiples(nextGuess, 1.0, guess, -1.0);
    double[] derivativeChange = DoubleArrays.addMultiples(nextDerivative, 1.0, derivative,  -1.0);
    pushOntoList(guessChange, inputDifferenceVectorList);
    pushOntoList(derivativeChange,  derivativeDifferenceVectorList);
  }

  private void pushOntoList(double[] vector, LinkedList<double[]> vectorList) {
    vectorList.addFirst(vector);
    if (vectorList.size() > maxHistorySize)
      vectorList.removeLast();
  }

  private int historySize() {
    return inputDifferenceVectorList.size();
  }

  private double[] getInputDifference(int num) {
    // 0 is previous, 1 is the one before that
    return inputDifferenceVectorList.get(num);
  }

  private double[] getDerivativeDifference(int num) {
    return derivativeDifferenceVectorList.get(num);
  }

  private double[] getLastDerivativeDifference() {
    return derivativeDifferenceVectorList.getFirst();
  }

  private double[] getLastInputDifference() {
    return inputDifferenceVectorList.getFirst();
  }


  private double[] implicitMultiply(double[] initialInverseHessianDiagonal, double[] derivative) {
	double[] rho = new double[historySize()];
	double[] alpha = new double[historySize()];
    double[] right = DoubleArrays.clone(derivative);
    // loop last backward
    for (int i = historySize()-1; i >= 0; i--) {
      double[] inputDifference = getInputDifference(i);
      double[] derivativeDifference = getDerivativeDifference(i);
      rho[i] = DoubleArrays.innerProduct(inputDifference, derivativeDifference);
      if (rho[i] == 0.0)
        throw new RuntimeException("LBFGSMinimizer.implicitMultiply: Curvature problem.");
      alpha[i] = DoubleArrays.innerProduct(inputDifference, right) / rho[i];
      right = DoubleArrays.addMultiples(right, 1.0, derivativeDifference, -1.0*alpha[i]);
    }
    double[] left = DoubleArrays.pointwiseMultiply(initialInverseHessianDiagonal, right);
    for (int i = 0; i < historySize(); i++) {
      double[] inputDifference = getInputDifference(i);
      double[] derivativeDifference = getDerivativeDifference(i);
      double beta = DoubleArrays.innerProduct(derivativeDifference, left) / rho[i];
      left = DoubleArrays.addMultiples(left, 1.0, inputDifference, alpha[i] - beta);
    }
    return left;
  }

  private double[] getInitialInverseHessianDiagonal(DifferentiableFunction function) {
    double scale = 1.0;
    if (derivativeDifferenceVectorList.size() >= 1) {
      double[] lastDerivativeDifference = getLastDerivativeDifference();
      double[] lastInputDifference = getLastInputDifference();
      double num = DoubleArrays.innerProduct(lastDerivativeDifference, lastInputDifference);
      double den = DoubleArrays.innerProduct(lastDerivativeDifference, lastDerivativeDifference);
      scale = num / den;
    }
    return DoubleArrays.constantArray(scale, function.dimension());
  }

  public LBFGSMinimizer() {
  }

  public LBFGSMinimizer(int maxIterations) {
    this.maxIterations = maxIterations;
  }

  public int getnRequiredIterations()
  {
    return nRequiredIterations;
  }

}
