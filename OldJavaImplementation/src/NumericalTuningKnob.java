/*
 *
 */

/*
 * long and double will both extend numerical
 */

public abstract class NumericalTuningKnob implements TuningKnob
{
  TuningKnobSearchProperties props = null;
  public double min = Double.NaN, max = Double.NaN;
  public int columnIndex = -1;
  public String name = null;

  public NumericalTuningKnob(TuningKnobSearchProperties p, String nameInit, double minInit, double maxInit, int col)
  {
    assert (p != null);
    assert (nameInit != null);
    assert (Double.NEGATIVE_INFINITY < minInit);
    assert (minInit < maxInit);
    assert (maxInit < Double.POSITIVE_INFINITY);
    props = p;
    name = new String(nameInit);
    min = minInit;
    max = maxInit;
    columnIndex = col;
  }

  public String getName() { return name; }
  public int getColumnIdx() { return columnIndex; }

  public double longestDist()
  {
    return max - min;
  }

  public boolean legalValue(Object v)
  {
    if (v == null)
      return false;
    Double d = (Double)v;
    return (d >= min) && (d <= max);
  }

  public int compareValues(Object v1, Object v2) {
    if (v1 instanceof Double && v2 instanceof Double) {
      double diff = (Double)v1 - (Double)v2;
      if (diff < -props.epsilon) return -1;
      if (diff > props.epsilon) return 1;
      return 0;
    }
    else {
      assert (false);
      return -1;
    }
  }

  public double valueToCoordinate(Object i)
  {
    return ((Double)i);
  }

  public Object coordinateToValue(double coord)
  {
    return coord;
  }

  public double minCoordinateValue() { return (double)min; }
  public double maxCoordinateValue() { return (double)max; }

  public String valueToString(Object v)
  {
    assert (v != null);
    return ((Double)v).toString();
  }

  public double distance(Object v1, Object v2) {
    if (v1 instanceof Double && v2 instanceof Double) {
      // assert(legalValue(v1));
      // assert(legalValue(v2));
      return Math.abs((Double)v1 - (Double)v2);
    }
    else {
      assert (false);
      return Double.NaN;
    }
  }
}
