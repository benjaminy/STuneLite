/*
 *
 */

import com.mallardsoft.tuple.*;

public class KnobConstant extends Constant<Double, Pair<Double, Double>> implements RealValued
{
  protected TuningKnob knob = null;

  public KnobConstant(TuningKnob k, String n)
  {
    super(n);
    knob = k;
  }

  public Pair<Double, Double> getPrediction(ValueAndPredictionSource source, Point p)
  {
    return Tuple.from(getValue(source,p), 0.0);
  }
  public Double getValue(ValueSource source, Point p)
  {
    Object setting = source.getKnobSpace().getKnobSetting(p,knob);
    return (Double)setting;
  }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        KnobConstant other = null;
        try { other = (KnobConstant)f; }
        catch (ClassCastException e) { assert(false); }
        int n = name.compareTo(other.getName());
        if (n == 0) {
          return knob.compareTo(other.knob);
        }
        else {
          return n;
        }
      }
      return magicDiff;
    }
  }
}
