/*
 *
 */

import com.mallardsoft.tuple.*;

public class BooleanAggregate extends Aggregate<Boolean, Double> implements BooleanValued
{
  public enum Operator { and, or };
  protected Operator op = null;

  public BooleanAggregate(Operator o, BooleanValued s, BooleanValued f, String n)
  {
    super(s, f, n);
    if (o == Operator.and || o == Operator.or) {
      op = o;
    }
    else {
      assert(false);
    }
  }

  protected Double calcPredictionFromValue(Boolean v)
  { return ((Boolean)v) ? 1.0 : 0.0; }
  protected Boolean calcValueFromPrediction(Double v)
  { return v > 0.5; }

  protected Object initAccum()
  {
    switch (op) {
      case and: return true;
      case or:  return false;
      default:  assert(false);
    }
    return null;
  }
  protected Object accumulate(Object a, Boolean v)
  {
    switch (op) {
      case and: return (Boolean)a && v;
      case or:  return (Boolean)a || v;
      default:  assert(false);
    }
    return null;
  }
  protected Double finish(Object a)
  { return calcPredictionFromValue((Boolean)a); }
}
