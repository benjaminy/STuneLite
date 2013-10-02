/*
 *
 */

public class AndGameTrueBias extends Logical
{
  public AndGameTrueBias(BooleanValued l, BooleanValued r, String n) { super(l, r, n); }

  protected double calcLogicalPrediction(double p1, double p2) { return Math.min(p1, p2); }
  protected boolean calcLogicalValue(boolean v1, boolean v2) { return v1 && v2; }

  protected Double calcLogicalPrediction(double p) { return p; }
  protected Boolean calcLogicalValue(boolean v) { return v; }

  protected Double calcLogicalPrediction() { return 1.0; }
  protected Boolean calcLogicalValue() { return false; }
}
