/*
 *
 */

public abstract class Logical extends Algebraic<Boolean, Double, Boolean, Double> implements BooleanValued
{
  protected Logical(BooleanValued l, BooleanValued r, String n)
  {
    super (l, r, n);
  }

  protected Double calcAlgebraicPrediction(Double p1, Double p2)
  {
    if (p1 != null && p2 != null) {
      return calcLogicalPrediction(p1, p2);
    }
    else if (p1 == null && p2 == null) {
      Double ret = null;
      try { ret = calcLogicalPrediction(); }
      catch (IllegalArgumentException e) { }
      return ret;
    }
    else {
      if (false) {System.out.printf("Just one for %s\n", name);}
      Double p = p1 == null ? p2 : p1;
      Double ret = null;
      try { ret = calcLogicalPrediction(p); }
      catch (IllegalArgumentException e) {
        if (false) {System.out.printf("  Crapped out\n");}
      }
      return ret;
    }
  }

  protected Boolean calcAlgebraicValue(Boolean v1, Boolean v2)
  {
    if (v1 != null && v2 != null) {
      return calcLogicalValue(v1, v2);
    }
    else if (v1 == null && v2 == null) {
      Boolean ret = null;
      try { ret = calcLogicalValue(); }
      catch (IllegalArgumentException e) { }
      return ret;
    }
    else {
      Boolean v = v1 == null ? v2 : v1;
      Boolean ret = null;
      try { ret = calcLogicalValue(v); }
      catch (IllegalArgumentException e) { }
      return ret;
    }
  }

  abstract protected double calcLogicalPrediction(double p1, double p2);
  abstract protected boolean calcLogicalValue(boolean v1, boolean v2);

  protected Double calcLogicalPrediction(double p)
  { throw new IllegalArgumentException(); }
  protected Boolean calcLogicalValue(boolean v)
  { throw new IllegalArgumentException(); }

  protected Double calcLogicalPrediction()
  { throw new IllegalArgumentException(); }
  protected Boolean calcLogicalValue()
  { throw new IllegalArgumentException(); }
}
