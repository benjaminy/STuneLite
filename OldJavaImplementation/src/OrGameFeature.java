/*
 *
 */

public class OrGameFeature extends Logical
{
  TuningKnobSearchProperties props = null;

  public OrGameFeature(TuningKnobSearchProperties p, BooleanValued l, BooleanValued r, String n)
  {
    super(l, r, n);
    assert (p != null);
    props = p;
  }

  protected double calcLogicalPrediction(double p1, double p2) { return Math.max(p1, p2); }
  protected boolean calcLogicalValue(boolean v1, boolean v2) { return v1 || v2; }

  protected Double calcLogicalPrediction(double p) { if (p > (1.0 - props.epsilon)) {return p;} else {return null;} }
  protected Boolean calcLogicalValue(boolean v) { if (v) {return v;} else {return null;} }
}
