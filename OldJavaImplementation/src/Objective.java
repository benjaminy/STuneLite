/*
 *
 */

import com.mallardsoft.tuple.*;

public class Objective
{
  private BooleanValued feature, failure;
  public Objective(BooleanValued e, BooleanValued a)
  {
    assert(e != null);
    assert(a != null);
    feature = e;
    failure = a;
  }
  public BooleanValued getFeature() { return feature; }
  public BooleanValued getFailure() { return failure; }
}
