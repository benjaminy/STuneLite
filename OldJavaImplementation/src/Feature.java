/*
 * A Feature is either a sensor or something computed from sensors
 */

public interface Feature<ValueType, PredictionType> extends Comparable<Feature<ValueType, PredictionType>>
{
  public String getName();
  public PredictionType getPrediction(ValueAndPredictionSource search, Point p);
  public ValueType getValue(ValueSource source, Point p);
  int magicNumber();
}
