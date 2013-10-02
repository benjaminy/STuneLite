/*
 *
 */

public class OrMutexFeature extends Logical
{
  public OrMutexFeature(BooleanValued l, BooleanValued r, String n) { super(l, r, n); }

  protected double calcLogicalPrediction(double p1, double p2) { return p1 + p2; }
  protected boolean calcLogicalValue(boolean v1, boolean v2) { return v1 || v2; }
}
