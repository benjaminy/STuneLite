/*
 *
 */

import com.mallardsoft.tuple.*;

public class Negate extends Arithmetic
{
  public Negate(RealValued i, String n) { super(i, i, n); }

  protected Pair<Double, Double> calcArithmeticPrediction(double mean1, double dev1, double mean2, double dev2)
  {
    return Tuple.from(-mean1, dev1);
  }

  protected Double calcArithmeticValue(double d1, double d2) { return -d1; }
}
