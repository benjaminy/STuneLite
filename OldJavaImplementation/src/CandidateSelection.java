/*
 *
 */

import java.util.Comparator;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Random;

public class CandidateSelection
{
  public static Point select(TuningKnobSearchProperties props, Collection<Point> candidates,
      SingleSearch search, Random rGen) {

    double total = 0.0;
    SortedSet<Point> sorted = new TreeSet<Point>(new HeuristicScoreComparator(search));
    double min = Double.POSITIVE_INFINITY; double max = Double.NEGATIVE_INFINITY;

    for (Point p : candidates) {
      Double score = search.getHeuristicScore(p);
      assert (score != null);
      total += score;
      min = Math.min(score, min);
      max = Math.max(score, max);
      sorted.add(p);
      if (false) { System.out.printf("pt=%s, score=%e min=%e, max=%e\n", p, score, min, max); }
    }

    double range = max - min;
    double dart = rGen.nextDouble();

    Point pToTest = null;
    int i = 0;
    if (false) {System.out.printf("Dumb debugging %e %e %e %e %e\n", range, props.epsilon, min, max, dart);}
    if (range < props.epsilon) { // there's no real range, just pick one randomly
      i = rGen.nextInt(candidates.size());
      for (Point pt : candidates) {
        i--;
        if (i < 0) {
          pToTest = pt;
          break;
        }
      }
    }
    switch (props.candidateSelectionMethod) {
      case circleMethod:
      {
        dart = Math.sqrt (1 - (dart - 1) * (dart - 1));
        dart = Math.sqrt (1 - (dart - 1) * (dart - 1));
        dart = Math.sqrt (1 - (dart - 1) * (dart - 1));

        // System.out.printf("dart %f t %f", dart, total);

        for (Point p : sorted) {
          pToTest = p;
          dart -= search.getHeuristicScore(p) / total;
          if (dart < 0.0)
            break;
          i++;
        }
        break;
      }
      case exponentialMethod:
      {
        double boundary = 0.125;
        for (Point p : sorted) {
          pToTest = p;
          if (dart > boundary)
            break;
          boundary /= 2.0;
          i++;
        }
        break;
      }
      case tournamentMethod:
      {
        for (Point p1 : candidates) {
          p1.scratch = search.getHeuristicScore(p1);
        }
        SortedSet<Point> scratchSortedCandidates = new TreeSet<Point>(new Point.ScratchComparator());
        for (Point p : candidates) {
          scratchSortedCandidates.add(p);
        }
        double pTotal = 0.0;
        for (Point p1 : scratchSortedCandidates) {
          p1.scratch = search.getHeuristicScore(p1);
          for (Point p2 : scratchSortedCandidates) {
            if (p1.equals(p2)) {
              break;
            }
            p1.scratch *= 1 - search.getHeuristicScore(p2);
          }
          // p1.scratch = p1.scratch * p1.scratch * p1.scratch * p1.scratch;
          pTotal += p1.scratch;
        }
        dart *= pTotal;
        for (Point p : candidates) {
          pToTest = p;
          dart -= p.scratch;
          if (dart < 0.0)
            break;
          i++;
        }
        break;
      }
      case takeHighestMethod:
      {
        double bestSoFar = Double.NEGATIVE_INFINITY;
        for (Point p : candidates) {
          if (search.getHeuristicScore(p) > bestSoFar) {
            bestSoFar = search.getHeuristicScore(p);
            pToTest = p;
          }
        }
        break;
      }
      default:
      {
        assert (false);
      }
    }
    assert (pToTest != null);
    return pToTest;
  }

  public static class HeuristicScoreComparator implements Comparator<Point>
  {
    SingleSearch search = null;
    public HeuristicScoreComparator(SingleSearch s)
    {
      assert (s != null);
      search = s;
    }

    /* This comparison claims that points with higher value are "lower" in the
     * sorting order.  It is designed this way so that the standard iterator
     * will visit higer-valued points first. */
    public int compare(Point p1, Point p2)
    {
      Double score1 = search.getHeuristicScore(p1);
      Double score2 = search.getHeuristicScore(p2);
      assert (score1 != null);
      assert (score2 != null);
      double diff = score1 - score2;
      return diff < 0 ? 1 : (diff > 0 ? -1 : 0);
    }
  }
}
