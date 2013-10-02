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
import com.mallardsoft.tuple.*;

/*
 * A mesh is a map from points to the neighbors of that point
 */
public class Mesh extends Neighborhood
{
  TuningKnobSearchProperties props = null;
  static int maxPointsBetween = 0;
  SingleSearch search = null;
  Vector<Double> scalingFactors = null;

  // public Mesh(Collection<Point> pts) { this(null, pts); }

  public Mesh(TuningKnobSearchProperties p, SingleSearch s, Feature f, Collection<Point> points) {
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

    // First build a collection of all pairs of points, sorted by increasing
    // distance between the points in the pair.  (Also add an empty set of
    // neighbors to the map for each point.)
    // Set<Point.Pair> pairs = new HashSet<Point.Pair>();
    SortedSet<Pair<Point,Point>> pairs = new TreeSet<Pair<Point,Point>>(new ClosestPairs(space, scalingFactors));
    for (Point x : points) {
      Object xReading = (f == null) ? null : f.getValue(search, x);
      if (f == null || xReading != null) {
        put(x, new HashSet<Point>());
        for (Point y : points) {
          // This comparison ensures that we process each pair only once
          Object yReading = (f == null) ? null : f.getValue(search, y);
          if ((f == null || yReading != null) && x.compareTo(y) < 0) {
            Pair<Point,Point> pp = Tuple.from(x, y);
            int foo = pairs.size();
            pairs.add(pp);
            int bar = pairs.size();
            if (bar <= foo) {
              for (Pair<Point,Point> pp2 : pairs) {
                System.out.printf("%s, ", pp2);
              }
              System.out.printf("\n\nWha????? %s\n", pp);
              System.exit(1);
            }
          }
        }
      }
    }

    // int np = points.size();
    // System.out.printf("|points| = %d  |pairs| = %d  p(p-1)/2 = %d\n",
    //    np, pairs.size(), np * (np-1) / 2);

    // Visit all the pairs of points in increasing distance order
    for (Pair<Point,Point> pair : pairs) {
      Point x = Tuple.get1(pair);
      Point y = Tuple.get2(pair);
      Set<Point> pointsBetween = new HashSet<Point>();
      // If either of the points already has a neighbor that is "between" the
      // two points, then don't make them neighbors.  Otherwise, do make them
      // neighbors.
      for (Point z : get(x)) {
        if (zBetween(x, y, z)) {
          pointsBetween.add(z);
          // System.out.printf("%s between (%s, %s)\n", z, x, y);
        }
      }
      for (Point z : get(y)) {
        if (zBetween(x, y, z)) {
          pointsBetween.add(z);
          // System.out.printf("%s between (%s, %s)\n", z, x, y);
        }
      }
      if (pointsBetween.size() <= maxPointsBetween) {
        get(x).add(y);
        get(y).add(x);
      }
    }
  }

  public boolean zBetween(Point x, Point y, Point z) {
    double xy = space.distance(scalingFactors,x,y);
    double xz = space.distance(scalingFactors,x,z);
    double yz = space.distance(scalingFactors,y,z);

    boolean xCloserToZThanY = xz < xy;
    boolean yCloserToZThanX = yz < xy;
    boolean ellipseCondition = (xz + yz) < (props.betweenEllipseConst * xy);

    return xCloserToZThanY && yCloserToZThanX && ellipseCondition;
  }

  public Set<Point> wouldBeNeighbors(Point candidate, Set<Point> filteredSet)
  {
    Set<Point> ns = new HashSet<Point>();

    if (false) {
      for (Map.Entry<Point, Set<Point>> entry : entrySet()) {
        boolean addY = true;
        Point y = entry.getKey();
        for (Point z : entry.getValue()) {
          if (zBetween(candidate, y, z)) {
            addY = false;
            break;
          }
        }
        if (addY)
          ns.add(y);
      }
    }
    else if (true) { // The substantially slower, but maybe more accurate way
      for (Point y : filteredSet) {
        boolean addY = true;
        for (Point z : filteredSet) {
          if (!y.equals(z)) {
            if (zBetween(candidate, y, z)) {
              addY = false;
              break;
            }
          }
        }
        if (addY)
          ns.add(y);
      }
    }
    else {
      
    }

    return ns;
  }

  // Adding a point to the mesh is tricky, because I think the order of
  // insertion matters.
  // 
  // public void add(Point newPoint)
  // {
  //   for (Map.Entry<Point, Set<Point>> entry : entrySet()) {
  //     Point x = entry.getKey();
  //     Set<Point> toDelete = new HashSet<Point>();
  //     for (Point y : entry.getValue()) {
  //       if (zBetween(x, y, newPoint)) {
  //         toDelete.add(y);
  //       }
  //     }
  //     entry.getValue().removeAll(toDelete);
  //   }
  // 
  //   for (Map.Entry<Point, Set<Point>> entry : entrySet()) {
  //     Point x = entry.getKey();
  //   }
  // }

  public static class ClosestPairs implements Comparator<Pair<Point, Point>>
  {
    KnobSettingSpace space = null;
    Vector<Double> scaling = null;
    public ClosestPairs(KnobSettingSpace s, Vector<Double> sc) { space = s; scaling = sc; }
    public int compare(Pair<Point, Point> p1, Pair<Point, Point> p2)
    {
      Point x1 = Tuple.get1(p1);
      Point y1 = Tuple.get2(p1);
      Point x2 = Tuple.get1(p2);
      Point y2 = Tuple.get2(p2);
      double distDiff = space.distance(scaling,x1,y1) - space.distance(scaling,x2,y2);
      if (distDiff < 0.0) { return -1; }
      else if (distDiff > 0.0) { return 1; }
      else {
        return p1.compareTo(p2);
      }
    }
  }
}
