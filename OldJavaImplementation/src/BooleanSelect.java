/*
 *
 */

import com.mallardsoft.tuple.*;

public class BooleanSelect extends Select<Boolean, Double> implements BooleanValued
{
  public BooleanSelect(String n, BooleanValued s, BooleanValued t, BooleanValued e, TuningKnobSearchProperties p)
  {
    super(n, s, t, e, p);
  }
}
