/*
 *
 */

import java.util.Random;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntTuningKnob extends NumericalTuningKnob
{

  public IntTuningKnob(TuningKnobSearchProperties p, String nameInit, double minInit, double maxInit, int col)
  {
    super(p, nameInit, minInit, maxInit, col);
    long minInt = Math.round(minInit), maxInt = Math.round(maxInit);
    double minDiff = Math.abs(((double)minInt) - minInit);
    double maxDiff = Math.abs(((double)maxInt) - maxInit);
    assert(minDiff < props.epsilon);
    assert(maxDiff < props.epsilon);
  }

  public int numLegalValues()
  {
    return 1 + ((int)Math.round(max)) - ((int)Math.round(min));
  }

  public int valueToOrd(Object i)
  {
    return (int)(Math.round((Double)i) - Math.round(min));
  }

  public Double ordToValue(int i)
  {
    Double val = i + min;
    // assert(legalValue(val));
    return val;
  }

  public Double nextRandom(Random rGen)
  {
    int raw = Math.abs(rGen.nextInt());
    int ordValue = raw % numLegalValues();
    return ordToValue(ordValue);
  }

  public String valueToString(Object v)
  {
    assert (v != null);
    double val = (Double)v;
    long rounded = Math.round(val);
    double diff = Math.abs(val - ((double)rounded));
    if (diff < props.epsilon) {
      return ((Long)rounded).toString();
    }
    return ((Double)v).toString();
  }

  /*
   * Parameters: s - the string to convert to a value in this tuning knob's set
   * Returns: A legal value if s can be parsed, and null otherwise
   */
  public Double stringToValue(String s)
  {
    try {
      double d = Double.parseDouble(s);
      long l = Math.round(d);
      double diff = Math.abs(d - (double)l);
      if (diff > props.epsilon) {
        return null;
      }
      return d;
      // int val = (int)l;
      // if (legalValue(val)) {
      //   return val;
      // }
      // else {
      //   return null;
      // }
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  public String toString()
  {
    return String.format("IntRange %s [%d..%d] %d", name, Math.round(min), Math.round(max), columnIndex);
  }

  public int compareTo(TuningKnob other)
  {
    if (other == null) {
      return 1;
    }
    else {
      if (other instanceof IntTuningKnob) {
        IntTuningKnob i = (IntTuningKnob)other;
        double minDiff = min - i.min;
        double maxDiff = max - i.max;
        int columnDiff = columnIndex - i.columnIndex;
        if (minDiff < -props.epsilon) { return -1; }
        if (minDiff >  props.epsilon) { return 1; }
        if (maxDiff < -props.epsilon) { return -1; }
        if (maxDiff >  props.epsilon) { return 1; }
        if (columnDiff < 0) { return -1; }
        if (columnDiff > 0) { return 1; }
        return name.compareTo(i.name);
      }
      else {
        // there aren't any other kinds of knobs yet
        assert(false);
        return -1;
      }
    }
  }

  private class IntIterator implements Iterator
  {
    protected int i = 0;
    protected int iterMax = 0;

    public IntIterator() {
      i = (int)Math.round(min);
      iterMax = (int)Math.round(max);
    }

    public boolean hasNext()
    {
      return i <= iterMax;
    }

    public Object next()
    {
      if (i > iterMax) {
        throw new NoSuchElementException();
      }
      Double v = (double)i;
      i++;
      return v;
    }

    public void remove() { throw new UnsupportedOperationException(); }
  }

  public Iterator iterator()
  {
    return new IntIterator();
  }
}
