/*
 *
 */

import com.mallardsoft.tuple.*;

public class RealDefault extends DefaultFeature<Double, Pair<Double, Double>> implements RealValued
{
  public RealDefault(String n, RealValued f, RealValued d)
  {
    super(n, f, d);
  }
}
