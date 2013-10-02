/*
 *
 */

import com.mallardsoft.tuple.*;

public class Divide extends Arithmetic
{
  public Divide(RealValued l, RealValued r, String n) { super(l, r, n); }

  protected Pair<Double, Double> calcArithmeticPrediction(double mean1, double dev1, double mean2, double dev2)
  {
    // I don't know if this math is right at all.  Check it some day!!!
    double meanInv = 1.0 / mean2;
    double devInv = Math.abs(dev2 / (mean2 * (mean2 - dev2)));
    double mean = mean1 * meanInv;
    double dev = Math.sqrt((mean1 * mean1 * devInv * devInv) + (meanInv * meanInv * dev1 * dev1) + (dev1 * dev1 * devInv * devInv));
    return Tuple.from(mean, dev);
  }

  protected Double calcArithmeticValue(double d1, double d2) { return d1 / d2; }
}
