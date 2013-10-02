/*
 *
 */

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Comparator;
import com.mallardsoft.tuple.*;

public class Point extends Vector<Object> implements Comparable<Point>
{
  protected KnobSettingSpace space;
  public double scratch;

  public Point (KnobSettingSpace s)
  {
    super();
    assert (s != null);
    space = s;
  }

  public Point (Point orig, TuningKnob knobRepl, Object settingRepl)
  {
    this(orig.space);
    Iterator<TuningKnob> knobIter = orig.space.iterator();
    Iterator settingIter = orig.iterator();
    while (knobIter.hasNext() && settingIter.hasNext()) {
      TuningKnob knob = knobIter.next();
      Object setting = settingIter.next();
      if (knob.equals(knobRepl)) {
        add(settingRepl);
      }
      else {
        add(setting);
      }
    }
    assert (!knobIter.hasNext());
    assert (!settingIter.hasNext());
  }

  public enum Status {
    candidate,
    testing,
    tested
  };

  public static class ScratchComparator implements Comparator<Point>
  {
    public int compare(Point p1, Point p2)
    {
      double diff = p1.scratch - p2.scratch;
      return diff < 0.0 ? 1 : (diff > 0.0 ? -1 : 0);
    }
  }

  public static class ScratchComparatorRev implements Comparator<Point>
  {
    public int compare(Point p1, Point p2)
    {
      double diff = p1.scratch - p2.scratch;
      return diff < 0.0 ? -1 : (diff > 0.0 ? 1 : 0);
    }
  }

  public int compareTo(Point other) {
    if (other == null) {
      return 1;
    }
    else {
      int sCompare = space.compareTo(other.space);
      if (sCompare == 0) {
        Iterator i1 = this.iterator();
        Iterator i2 = other.iterator();
        Iterator <TuningKnob> knobs = space.iterator();

        while (i1.hasNext() && i2.hasNext() && knobs.hasNext()) {
          Object v1 = i1.next();
          Object v2 = i2.next();
          TuningKnob knob = knobs.next();
          int c = knob.compareValues(v1, v2);
          if (c != 0)
            return c;
        }
        assert(!i1.hasNext()); assert(!i2.hasNext()); assert(!knobs.hasNext());
        return 0;
      }
      else {
        return sCompare;
      }
    }
  }
  
  public boolean equals(Object p)
  {
    if (p instanceof Point)
      return compareTo(((Point)p)) == 0;
    else return false;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    Iterator settings = iterator();
    Iterator <TuningKnob> knobs = space.iterator();
  
    sb.append("[");
    boolean first = true;
    while (settings.hasNext()) {
      Object setting = settings.next();
      if (!first)
        sb.append(", ");
      if (setting instanceof Integer) {
        sb.append(String.format("%4d", (Integer)setting));
      }
      else {
        sb.append(setting);
      }
      first = false;
    }
    sb.append("]");
    return sb.toString();
  }

  public String toFilenameString()
  {
    StringBuffer sb = new StringBuffer();
    Iterator settings = iterator();
    Iterator <TuningKnob> knobs = space.iterator();
  
    boolean first = true;
    while (settings.hasNext()) {
      Object setting = settings.next();
      if (!first)
        sb.append("_");
      sb.append(setting.toString());
      first = false;
    }
    return sb.toString();
  }
}
