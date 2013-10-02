/*
 *
 */

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;

public class Failure implements BooleanValued
{
  private String name = null;
  private BooleanValued feature = null;
  private String key = null;

  public Failure(String n, BooleanValued f, String k)
  {
    name = new String(n);
    feature = f;
    key = new String(k);
  }

  public String getName() { return name; }
  public String getKey() { return key; }
  public Double getPrediction(ValueAndPredictionSource source, Point pt)
  {
    if (source.isPredictionCached(pt, this)) {
      Object v = source.getCachedPrediction(pt, this);
      if (v != null) {
        try { return (Double)v; }
        catch (ClassCastException e) {
          System.out.printf("getPrediction: %s is of incorrect type.\n", v);
          assert(false);
        }
      }
      return null;
    }
    else {
      Double p = feature.getPrediction(source, pt);
      if (p != null) {
        if (source.getNumPointsTested() > 0  && source.getProps().scaleFailureProbsHistory) {
          int count = source.getFailureModeCount(this);
          if (count > 0) {
            double failurePortion = ((double)count) / ((double)source.getNumPointsTested());
            failurePortion = Math.min(failurePortion, 0.9);
            double effectiveP = Math.pow(p, 1.0 - failurePortion);
            if (false) { System.out.printf("Failure p:%2f ep:%2f fp=%f c=%d nt=%d\n",
              p, effectiveP, failurePortion, count, source.getNumPointsTested()); }
            p = effectiveP;
          }
        }
      }
      source.updatePredictionCache(pt, this, p);
      return p;
    }
  }
  public Boolean getValue(ValueSource source, Point p)
  {
    Boolean val = source.isCauseOfFailure(p, this);
    if (false) { System.out.printf("\"%s\".getValue(_, %s) -> v:%s\n", this, p, val); }
    return val;
  }
  public int magicNumber() { return 600; }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        Failure other = null;
        try { other = (Failure)f; }
        catch (ClassCastException e) { assert(false); }
        int n = name.compareTo(other.getName());
        return n;
        // if (n == 0) {
        //   return Double.compare(val, other.val);
        // }
        // else {
        //   return n;
        // }
      }
      return magicDiff;
    }
  }

  public String toString()
  {
    return "failure " + name + " ("+key+")";
  }

  // public Failure(String spec, Application a)
  // {
  //   app = a;
  //   satisfiedModes  = new LinkedList<Integer>();
  //   violatedModes   = new LinkedList<Integer>();
  //   satisfiedPoints = new HashSet<Point>();
  //   violatedPoints  = new HashSet<Point>();
  // 
  //   // Failure: TooBigForSPR - NodeRouteProd ? ; SPRTimedOut, RouterCongestion
  //   String nameSplit[] = spec.split("-");
  //   if (nameSplit.length != 2) {
  //     System.err.printf("Bad constraint spec %s\n", spec);
  //     System.exit(1);
  //   }
  //   String name = nameSplit[0].trim();
  //   String sensorSplit[] = nameSplit[1].split("?");
  //   if (sensorSplit.length != 2) {
  //     System.err.printf("Bad constraint spec %s\n", nameSplit[1]);
  //     System.exit(1);
  //   }
  //   String sensorName = sensorSplit[0].trim();
  //   sensor = app.lookupSensor(sensorName);
  //   if (sensor == null) {
  //     System.err.printf("Bad sensor name %s\n", sensorName);
  //     System.exit(1);
  //   }
  //   String successesAndFailures[] = sensorSplit[1].split(";");
  //   if (successesAndFailures.length != 2) {
  //     System.err.printf("Bad constraint spec %s\n", sensorSplit[1]);
  //     System.exit(1);
  //   }
  //   String successes[]  = successesAndFailures[0].split(",");
  //   String failures[]   = successesAndFailures[1].split(",");
  //   for (String success : successes) {
  //     String trimmed = success.trim();
  //     if (trimmed.length() > 0) {
  //       Integer code = app.lookupStatus(trimmed);
  //       if (code == null) {
  //         System.err.printf("Bad failure mode %s\n", trimmed);
  //         System.exit(1);
  //       }
  //       satisfiedModes.add(code);
  //     }
  //   }
  //   for (String failure : failures) {
  //     String trimmed = failure.trim();
  //     if (trimmed.length() > 0) {
  //       Integer code = app.lookupStatus(trimmed);
  //       if (code == null) {
  //         System.err.printf("Bad failure mode %s\n", trimmed);
  //         System.exit(1);
  //       }
  //       violatedModes.add(code);
  //     }
  //   }
  // }

  // public void classifyPoints(Collection<Point> points)
  // {
  //   for (Point pt : points) {
  //     int status = app.getStatus(pt);
  //     if (app.wasSuccessful(pt) || satisfiedModes.contains(status)) {
  //       satisfiedModes.add(pt);
  //     }
  //     else if (violatedModes.contains(status)) {
  //       violatedPoints.add(pt);
  //     }
  //   }
  // }
}
