/*
 *
 */

import com.mallardsoft.tuple.*;

public class BooleanDefault extends DefaultFeature<Boolean, Double> implements BooleanValued
{
  public BooleanDefault(String n, BooleanValued f, BooleanValued d)
  {
    super(n, f, d);
  }
}
