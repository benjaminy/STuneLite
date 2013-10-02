/*
 *
 */

import com.mallardsoft.tuple.*;

public abstract class Comparison extends Algebraic<Double, Pair<Double, Double>, Boolean, Double> implements BooleanValued
{
  protected Comparison(RealValued l, RealValued r, String n)
  {
    super(l, r, n);
  }

  protected Double calcAlgebraicPrediction(Pair<Double, Double> d1, Pair<Double, Double> d2)
  {
    // if (name.equals("betterThanBest")||name.equals("appRunTime")) { System.out.printf("Foo %s\n", name); }
    if (d1 != null && d2 != null) {
      Double mean1  = Tuple.get1(d1);
      Double dev1   = Tuple.get2(d1);
      Double mean2  = Tuple.get1(d2);
      Double dev2   = Tuple.get2(d2);
      assert(mean1 != null && dev1 != null && mean2 != null && dev2 != null);
      return calcComparisonPrediction(mean1, dev1, mean2, dev2);
    }
    else if (d1 == null && d2 == null) {
      Double ret = null;
      try {
        ret = calcComparisonPrediction();
      }
      catch (IllegalArgumentException e) { }
      return ret;
    }
    else {
      Pair<Double, Double> d = d1 == null ? d2 : d1;
      Double mean  = Tuple.get1(d);
      Double dev   = Tuple.get2(d);
      assert(mean != null && dev != null);
      Double ret = null;
      try {
        ret = calcComparisonPrediction(mean, dev);
      }
      catch (IllegalArgumentException e) { }
      return ret;
    }
  }

  protected Boolean calcAlgebraicValue(Double d1, Double d2)
  {
    if (d1 != null && d2 != null) {
      return calcComparisonValue(d1, d2);
    }
    else if (d1 == null && d2 == null) {
      Boolean ret = null;
      try { ret = calcComparisonValue(); }
      catch (IllegalArgumentException e) { }
      return ret;
    }
    else {
      Double d = d1 == null ? d2 : d1;
      Boolean ret = null;
      try { ret = calcComparisonValue(d); }
      catch (IllegalArgumentException e) { }
      return ret;
    }
  }

  abstract protected Double calcComparisonPrediction(double mean1, double dev1, double mean2, double dev2);
  abstract protected Boolean calcComparisonValue(double val1, double val2);

  protected Double calcComparisonPrediction(double mean, double dev)
  { throw new IllegalArgumentException(); }
  protected Boolean calcComparisonValue(double val)
  { throw new IllegalArgumentException(); }

  protected Double calcComparisonPrediction()
  { throw new IllegalArgumentException(); }
  protected Boolean calcComparisonValue()
  { throw new IllegalArgumentException(); }
}
