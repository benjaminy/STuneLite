/*
 *
 */

public class ImpliesFeature extends Logical
{
  public ImpliesFeature(BooleanValued l, BooleanValued r, String n) { super(l, r, n); }
  protected double calcLogicalPrediction(double p1, double p2) { return 1.0 - p1 + (p1 * p2); }
  protected boolean calcLogicalValue(boolean v1, boolean v2) { return (!v1) || v2; }
}
