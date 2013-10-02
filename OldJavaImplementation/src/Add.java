/*
 *
 */

import com.mallardsoft.tuple.*;

public class Add extends Arithmetic
{
  public Add(RealValued l, RealValued r, String n) { super(l, r, n); }

  protected Pair<Double, Double> calcArithmeticPrediction(double mean1, double dev1, double mean2, double dev2)
  {
    return Tuple.from(mean1 + mean2, dev1 + dev2);
  }

  protected Double calcArithmeticValue(double d1, double d2) { return d1 + d2; }
}
