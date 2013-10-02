/*
 *
 */

import com.mallardsoft.tuple.*;

public class RealConstant extends Constant<Double, Pair<Double, Double>> implements RealValued
{
  protected double val = Double.NaN;

  public RealConstant(double v, String n)
  {
    super(n);
    val = v;
  }

  public Pair<Double, Double> getPrediction(ValueAndPredictionSource source, Point p) { return Tuple.from(val, 0.0); }
  public Double getValue(ValueSource source, Point p) { return val; }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        RealConstant other = null;
        try { other = (RealConstant)f; }
        catch (ClassCastException e) { assert(false); }
        int n = name.compareTo(other.getName());
        if (n == 0) {
          return Double.compare(val, other.val);
        }
        else {
          return n;
        }
      }
      return magicDiff;
    }
  }
}
