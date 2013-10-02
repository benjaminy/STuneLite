/*
 *
 */

import com.mallardsoft.tuple.*;

public class RealSelect extends Select<Double, Pair<Double, Double>> implements RealValued
{
  public RealSelect(String n, BooleanValued s, RealValued t, RealValued e, TuningKnobSearchProperties p)
  {
    super(n, s, t, e, p);
  }
}
