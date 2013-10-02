/*
 *
 */

public class XorFeature extends Logical
{
  private OrFeature internal = null;
  TuningKnobSearchProperties props = null;

  public XorFeature(TuningKnobSearchProperties p, BooleanValued l, BooleanValued r, String n) {
    super(l, r, n);
    props = p;
    NotFeature nl = new NotFeature(l, n + "_nl");
    NotFeature nr = new NotFeature(r, n + "_nr");
    AndFeature and1 = new AndFeature(p, l, nr, n + "_and1");
    AndFeature and2 = new AndFeature(p, nl, r, n + "_and2");
    internal = new OrFeature(p, and1, and2, n + "_or");
  }

  protected double calcLogicalPrediction(double p1, double p2) { return internal.calcLogicalPrediction(p1, p2); }
  protected boolean calcLogicalValue(boolean v1, boolean v2) { return internal.calcLogicalValue(v1, v2); }
}
