/*
 *
 */

import java.util.Comparator;
import java.util.Collection;
import java.util.Vector;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import com.mallardsoft.tuple.*;

/*
 * A neighborhood is a map from points to the neighbors of that point
 *
 * For now we're assuming that the neighbor relation does not have to be
 * reflexive, so if P1 is a neighbor of P2 and P2 also happens to be a neighbor
 * of P1, we'll count (P1, P2) and (P2, P1) as separate neighbor pairs.
 */
public abstract class Neighborhood extends java.util.HashMap<Point, Set<Point>>
{
  KnobSettingSpace space = null;
  public int totalNumNeighborPairs() {
    int total = 0;
    for (Map.Entry<Point, Set<Point>> entry : entrySet()) {
      total += entry.getValue().size();
    }
    return total;
  }

  public Collection<Pair<Point, Point>> getAllNeighborPairs()
  {
    Collection<Pair<Point,Point>> cs = new LinkedList<Pair<Point,Point>>();

    for (Map.Entry<Point, Set<Point>> entry : entrySet()) {
      Point x = entry.getKey();
      for (Point y : entry.getValue()) {
        cs.add(Tuple.from(x, y));
      }
    }
    return cs;
  }

  // public Collection<Double> getSlopesOfNeighborPairs(double floor, boolean allFailedMode)
  // {
  //   Collection<Pair<Point,Point>> cs = getAllNeighborPairs();
  //   Collection<Double> ss = new LinkedList<Double>();
  //   for (Pair<Point,Point> c : cs) {
  //     Point x = Tuple.get1(c);
  //     Point y = Tuple.get2(c);
  //     if (allFailedMode) {
  //       double xScore = x.failureFactor == null ? 1.0 : 1.0 / x.failureFactor;
  //       double yScore = y.failureFactor == null ? 1.0 : 1.0 / y.failureFactor;
  //       double scoreDiff = Math.abs(xScore - yScore);
  //       double dist = x.distance(y);
  //       double slope = scoreDiff / dist;
  //       ss.add(slope < floor ? floor : slope);
  //       // System.out.printf("%f  %f  %f\n", scoreDiff, dist, scoreDiff / dist);
  //     }
  //     else {
  //       if (x.actualScore != null && y.actualScore != null) {
  //         double scoreDiff = Math.abs(x.actualScore - y.actualScore);
  //         double dist = x.distance(y);
  //         double slope = scoreDiff / dist;
  //         ss.add(slope < floor ? floor : slope);
  //         // System.out.printf("%f  %f  %f\n", scoreDiff, dist, scoreDiff / dist);
  //       }
  //     }
  //   }
  //   return ss;
  // }

  public Collection<Double> getDistsBetweenNeighborPairs(Vector<Double> scalingFactors)
  {
    Collection<Double> ds = new LinkedList<Double>();
    for (Pair<Point,Point> c : getAllNeighborPairs()) {
      ds.add(space.distance(scalingFactors,Tuple.get1(c),Tuple.get2(c)));
    }
    return ds;
  }

  public Set<Point> wouldBeNeighbors(Point candidate)
  {
    return wouldBeNeighbors(candidate, keySet());
  }
  abstract public Set<Point> wouldBeNeighbors(Point candidate, Set<Point> filteredSet);

  // Adding a point to an existing neighborhood can be tricky.  So far it's been
  // workable to just build a whole new neighborhood when you might just add a
  // point.
  // 
  // public void add(Point newPoint)
}
