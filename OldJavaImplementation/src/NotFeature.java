/*
 *
 */

public class NotFeature extends Logical
{
  public NotFeature(BooleanValued in, String n) { super(in, in, n); }

  protected double calcLogicalPrediction(double p1, double p2) { return 1.0 - p1; }
  protected boolean calcLogicalValue(boolean v1, boolean v2) { return !v1; }
}
