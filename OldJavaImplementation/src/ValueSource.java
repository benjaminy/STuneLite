/*
 *
 */

import java.util.Set;
import com.mallardsoft.tuple.*;

public interface ValueSource
{
  KnobSettingSpace getKnobSpace();
  Boolean isCauseOfFailure(Point pt, Failure f);
  Set<Point> getTestedPoints();

  Double getSensorReading(Point pt, Sensor f);
  Object getCachedValue(Point pt, Feature f);
  boolean isValueCached(Point pt, Feature f);
  void updateValueCache(Point pt, Feature f, Object value);
}
