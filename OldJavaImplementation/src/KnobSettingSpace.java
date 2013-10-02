/*
 * When the dimensions of a soace are ordered, they go from least significant at
 * low vector indices to most significant at high vector indices.  The standard
 * iterator goes from least to most significant.
 */

import java.util.Iterator;
import java.util.Vector;

public class KnobSettingSpace extends Vector <TuningKnob> implements Comparable<KnobSettingSpace>
{
  TuningKnobSearchProperties props;

  public KnobSettingSpace(TuningKnobSearchProperties p, int s)
  {
    super(s);
    assert (p != null);
    props = p;
  }

  public int totalSize() {
    int s = 1;
    for (TuningKnob k : this) {
      s *= k.numLegalValues();
    }
    return s;
  }

  /* distance just calculates a basic Manahattan or Euclidean distance, given
   * distance functions for each of the individual dimensions. */
  public double distance(Vector<Double> scalingFactors, Point pt1, Point pt2)
  {
    Iterator i1 = pt1.iterator();
    Iterator i2 = pt2.iterator();
    Iterator <TuningKnob> knobs = iterator();
    Iterator <Double> factors = scalingFactors == null ? null : scalingFactors.iterator();
  
    double d = 0.0;
    while (i1.hasNext() && i2.hasNext() && knobs.hasNext()) {
      Object v1 = i1.next();
      Object v2 = i2.next();
      TuningKnob knob = knobs.next();
      double scalingFactor = 1.0;
      if (factors != null) {
        if (factors.hasNext()) {
          scalingFactor = factors.next();
        }
        else { assert (false); }
      }

      double di = scalingFactor * knob.distance(v1, v2);
      switch (props.distanceMethod) {
        case manhattan: d += di; break;
        case euclidean: d += di * di; break;
        default: assert (false);
      }
    }
    assert(!(i1.hasNext() || i2.hasNext() || knobs.hasNext()));
  
    switch (props.distanceMethod) {
      case manhattan: break;
      case euclidean: d = Math.sqrt(d); break;
      default: assert (false);
    }

    return d;
  }

  public double maxDistance(Vector<Double> scalingFactors)
  {
    Iterator <Double> factors = scalingFactors == null ? null : scalingFactors.iterator();
  
    double d = 0.0;
    for (TuningKnob knob : this) {
      double scalingFactor = 1.0;
      if (factors != null) {
        if (factors.hasNext()) {
          scalingFactor = factors.next();
        }
        else { assert (false); }
      }

      if (knob instanceof NumericalTuningKnob) {
        NumericalTuningKnob nKnob = (NumericalTuningKnob)knob;
        double minUnscaled = nKnob.minCoordinateValue();
        double maxUnscaled = nKnob.maxCoordinateValue();
        double di = scalingFactor * (maxUnscaled - minUnscaled);
        switch (props.distanceMethod) {
          case manhattan: d += di; break;
          case euclidean: d += di * di; break;
          default: assert (false);
        }
      }
      else { assert (false); }
    }

    switch (props.distanceMethod) {
      case manhattan: break;
      case euclidean: d = Math.sqrt(d); break;
      default: assert (false);
    }

    return d;
  }

  public Vector<Double> unitScalingFactors()
  {
    Vector<Double> s = new Vector<Double>();
    for (TuningKnob t : this) {
      s.add(1.0);
    }
    return s;
  }

  public int compareTo(KnobSettingSpace other)
  {
    int sDiff = size() - other.size();
    if (sDiff == 0) {
      Iterator <TuningKnob> knobs1 = iterator();
      Iterator <TuningKnob> knobs2 = other.iterator();

      while (knobs1.hasNext() && knobs2.hasNext()) {
        TuningKnob knob1 = knobs1.next();
        TuningKnob knob2 = knobs2.next();
        int c = knob1.compareTo(knob2);
        if (c != 0)
          return c;
      }
      assert(!knobs1.hasNext()); assert(!knobs2.hasNext());
      return 0;
    }
    else {
      return sDiff;
    }
  }

  public Object getKnobSetting(Point p, TuningKnob k)
  {
    Iterator<Object> settings = p.iterator();
    Iterator<TuningKnob> knobs = iterator();
    while (settings.hasNext() && knobs.hasNext()) {
      Object setting = settings.next();
      TuningKnob knob = knobs.next();
      if (k.compareTo(knob) == 0) {
        return setting;
      }
    }
    return null;
  }
}
