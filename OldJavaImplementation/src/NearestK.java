/*
 *
 */

import java.util.Comparator;
import java.util.Collection;
import java.util.Vector;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * A neighborhood is a map from points to the neighbors of that point
 */
public class NearestK extends Neighborhood
{
  TuningKnobSearchProperties props = null;
  SingleSearch search = null;
  Vector<Double> scalingFactors = null;

  // This constructor uses (overwrites) the points' predicted.scratch field, but
  // does not need the values to be maintained.
  public NearestK(TuningKnobSearchProperties p, SingleSearch s, Feature f, Collection<Point> points) {
    super();
    assert (p != null);
    assert (s != null);
    search = s;
    props = p;
    space = search.app.getTemplate().getKnobSpace();
    if (f != null) {
      if (f instanceof Sensor) {
        scalingFactors = search.getScalingFactors((Sensor)f);
      }
    }

    for (Point x : points) {
      Object xReading = (f == null) ? null : f.getValue(search, x);
      if (xReading != null) {
        Set<Point> xNeighbors = new HashSet<Point>();
        put(x, xNeighbors);
        SortedSet<Point> sorted = new TreeSet<Point>(new Point.ScratchComparator());
        for (Point y : points) {
          Object yReading = f.getValue(search, y);
          if (yReading != null && !x.equals(y)) {
            y.scratch = space.distance(scalingFactors, x, y);
            sorted.add(y);
          }
        }
        int i = 0;
        for (Point y : sorted) {
          if (i >= props.kForNearestNeighbors) {
            break;
          }
          xNeighbors.add(y);
          i++;
        }
      }
    }
  }

  public Set<Point> wouldBeNeighbors(Point candidate, Set<Point> filteredSet)
  {
    Set<Point> neighbors = new HashSet<Point>();
    SortedSet<Point> sorted = new TreeSet<Point>(new Point.ScratchComparator());
    for (Point y : filteredSet) {
      if (!candidate.equals(y)) {
        y.scratch = space.distance(scalingFactors, candidate, y);
        sorted.add(y);
      }
    }
    int i = 0;
    for (Point y : sorted) {
      if (i >= props.kForNearestNeighbors) {
        break;
      }
      neighbors.add(y);
      i++;
    }
    return neighbors;
  }
}
