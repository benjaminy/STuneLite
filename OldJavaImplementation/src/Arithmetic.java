/*
 *
 */

import com.mallardsoft.tuple.*;

public abstract class Arithmetic extends Algebraic<Double, Pair<Double, Double>, Double, Pair<Double, Double>> implements RealValued
{
  protected Pair<Double, Double> calcAlgebraicPrediction(Pair<Double, Double> d1, Pair<Double, Double> d2)
  {
    if (d1 != null && d2 != null) {
      Double mean1  = Tuple.get1(d1);
      Double dev1   = Tuple.get2(d1);
      Double mean2  = Tuple.get1(d2);
      Double dev2   = Tuple.get2(d2);
      assert(mean1 != null && dev1 != null && mean2 != null && dev2 != null);
      return calcArithmeticPrediction(mean1, dev1, mean2, dev2);
    }
    else if (d1 == null && d2 == null) {
      Pair<Double, Double> ret = null;
      try {
        ret = calcArithmeticPrediction();
      }
      catch (IllegalArgumentException e) { }
      return ret;
    }
    else {
      Pair<Double, Double> d = d1 == null ? d2 : d1;
      Double mean  = Tuple.get1(d);
      Double dev   = Tuple.get2(d);
      assert(mean != null && dev != null);
      Pair<Double, Double> ret = null;
      try {
        ret = calcArithmeticPrediction(mean, dev);
      }
      catch (IllegalArgumentException e) { }
      return ret;
    }
  }

  protected Double calcAlgebraicValue(Double d1, Double d2)
  {
    if (d1 != null && d2 != null) {
      return calcArithmeticValue(d1, d2);
    }
    else if (d1 == null && d2 == null) {
      Double ret = null;
      try { ret = calcArithmeticValue(); }
      catch (IllegalArgumentException e) { }
      return ret;
    }
    else {
      Double d = d1 == null ? d2 : d1;
      Double ret = null;
      try { ret = calcArithmeticValue(d); }
      catch (IllegalArgumentException e) { }
      return ret;
    }
  }

  abstract protected Pair<Double, Double> calcArithmeticPrediction(double mean1, double dev1, double mean2, double dev2);
  abstract protected Double calcArithmeticValue(double val1, double val2);

  protected Pair<Double, Double> calcArithmeticPrediction(double mean, double dev)
  { throw new IllegalArgumentException(); }
  protected Double calcArithmeticValue(double val)
  { throw new IllegalArgumentException(); }

  protected Pair<Double, Double> calcArithmeticPrediction()
  { throw new IllegalArgumentException(); }
  protected Double calcArithmeticValue()
  { throw new IllegalArgumentException(); }

  protected Arithmetic(RealValued l, RealValued r, String n)
  {
    super(l, r, n);
  }
}
