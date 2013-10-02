/*
 *
 */

import java.util.Collection;
import java.util.LinkedList;
import com.mallardsoft.tuple.*;

public class RealAggregate extends Aggregate<Double, Pair<Double, Double>> implements RealValued
{
  public enum Operator { sum, product, max, min, mean, meanAndDev };
  protected Operator op = null;
  private static final boolean bigDumpDebug = false;

  public RealAggregate(Operator o, RealValued s, BooleanValued f, String n)
  {
    super(s, f, n);
    if (o == Operator.sum || o == Operator.product || o == Operator.max || o == Operator.min ||
        o == Operator.mean || o == Operator.meanAndDev) {
      op = o;
    }
    else {
      assert(false);
    }
  }

  protected Pair<Double, Double> calcPredictionFromValue(Double v)
  { return Tuple.from(v, 0.0); }
  protected Double calcValueFromPrediction(Pair<Double, Double> v)
  { return Tuple.get1(v); }

  protected Object initAccum()
  {
    switch (op) {
      case sum:     return 0.0;
      case product: return 1.0;
      case max:     return Double.NEGATIVE_INFINITY;
      case min:     return Double.POSITIVE_INFINITY;
      case mean:    return Tuple.from((Double)0.0, (Integer)0);
      case meanAndDev:
      { if (bigDumpDebug) { System.err.printf("init\n"); }
        return new LinkedList<Double>(); }
      default:      assert(false);
    }
    return null;
  }

  protected Object accumulate(Object a, Double v)
  {
    switch (op) {
      case sum:     return (Double)a + v;
      case product: return (Double)a * v;
      case max:     return Math.max((Double)a, v);
      case min:     return Math.min((Double)a, v);
      case mean:
      { Pair<Double, Integer> p = (Pair<Double, Integer>)a;
        return Tuple.from(Tuple.get1(p) + v, Tuple.get2(p) + 1); }
      case meanAndDev:
      { LinkedList<Double> l = (LinkedList<Double>)a;
        if (bigDumpDebug) { System.err.printf("  added value: %f\n", v); }
        l.add(v);
        return l; }
      default:      assert(false);
    }
    return null;
  }

  protected Pair<Double, Double> finish(Object a)
  {
    switch (op) {
      case sum: case product: case max: case min:
      { return calcPredictionFromValue((Double)a);}
      case mean:
      { Pair<Double, Integer> p = (Pair<Double, Integer>)a;
        return calcPredictionFromValue(Tuple.get1(p) / (double)Tuple.get2(p)); }
      case meanAndDev:
      { Collection<Double> vals = (Collection<Double>)a;
        if (bigDumpDebug) { 
          for (Double v : vals) {
            System.err.printf("  still have it: %f\n", v);
          }
          System.err.printf("finished\n");
        }
        return Stats.meanStdDev (vals); }
      default:      assert(false);
    }
    return null;
  }
}
