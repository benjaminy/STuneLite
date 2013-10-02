/*
 *
 */

import java.util.Set;
import com.mallardsoft.tuple.*;

public interface ValueAndPredictionSource extends ValueSource
{
  
  int getNumPointsTested();
  int getFailureModeCount(Failure f);
  Pair<Double, Double> getSensorPrediction(Point pt, Sensor f);
  Object getCachedPrediction(Point pt, Feature f);
  boolean isPredictionCached(Point pt, Feature f);
  void updatePredictionCache(Point pt, Feature f, Object pred);
  TuningKnobSearchProperties getProps();
}
