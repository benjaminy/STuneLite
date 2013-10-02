/*
 *
 */

import java.lang.Exception;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.AbstractMap;
import java.util.Random;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import com.mallardsoft.tuple.*;

public class SynthApp extends Application
{
  Sensor goodness = null, badness = null;
  Failure fail = null;
  Random rand = null;
  KnobSettingSpace space = null;

  public SynthApp(TuningKnobSearchProperties p, int numberOfKnobs, int settingsPerKnob, Random r)
  {
    assert (p != null);
    props = p;
    l = Logger.getLogger(props.defaultLoggerName);
    rand = r;
    space = new KnobSettingSpace(props, numberOfKnobs);
    for (int i = 0; i < numberOfKnobs; i++) {
      space.add(i, new IntTuningKnob(props, "k"+i, 1, settingsPerKnob, -1));
    }

    BooleanConstant cTrue = new BooleanConstant(true, "true");
    RealConstant zero = new RealConstant(0.0, "zero");
    RealConstant one = new RealConstant(1.0, "one");
    RealConstant rawResourceLimit = new RealConstant(10.0, "rawResourceLimit");
    RealConstant dispersionScaler = new RealConstant(0.9, "dispersionScaler");

    goodness = new Sensor("goodness", "g");
    badness  = new Sensor("badness", "b");
    Divide invGoodness = new Divide(one, goodness, "invGoodness");

    RealAggregate rawBest = new RealAggregate(RealAggregate.Operator.max, goodness, cTrue, "rawBest");
    PointDispersion rawDispersion = new PointDispersion(goodness, cTrue, "rawPointDispersion");
    RealDefault preScaledDispersion = new RealDefault("dispersion", rawDispersion, one);
    Add scaledDispersion = new Add(preScaledDispersion, dispersionScaler, "scaledDispersion");
    AvgConfidenceInterval confidence = new AvgConfidenceInterval(goodness, goodness, cTrue, "confidence");
    Multiply bestCorrection = new Multiply(confidence, preScaledDispersion, "bestCorrection");
    Add best = new Add(rawBest, bestCorrection, "best");
    GreaterThan better = new GreaterThan(props, goodness, best, "better");
    RealDefault resourceDispersion = new RealDefault("resourceDispersion", rawResourceLimit, one);
    Add scaledResourceDispersion = new Add(resourceDispersion, dispersionScaler, "scaledResourceDispersion");
    Multiply resourceLimit = new Multiply(rawResourceLimit, scaledResourceDispersion, "resourceLimit");
    GreaterThan worse = new GreaterThan(props, badness, rawResourceLimit, "worse");
    fail = new Failure("fail", worse, "fail");

    Map<String, Feature> fe = new HashMap<String, Feature>();
    fe.put("goodness", goodness);
    fe.put("invGoodness", invGoodness);
    fe.put("badness", badness);
    fe.put("cTrue", cTrue);
    fe.put("rawBest", rawBest);
    fe.put("rawDispersion", rawDispersion);
    fe.put("zero", zero);
    fe.put("one", one);
    fe.put("preScaledDispersion", preScaledDispersion);
    fe.put("dispersionScaler", dispersionScaler);
    fe.put("scaledDispersion", scaledDispersion);
    fe.put("best", best);
    fe.put("better", better);
    fe.put("rawResourceLimit", rawResourceLimit);
    fe.put("worse", worse);
    fe.put("fail", fail);

    Set<RealValued> plotting = new TreeSet<RealValued>();
    plotting.add(goodness);
    if (false) {
      plotting.add(badness);
    }
    if (true) {
      plotting.add(invGoodness);
    }
    if (false) {
      plotting.add(rawDispersion);
      plotting.add(scaledDispersion);
      plotting.add(rawBest);
      plotting.add(best);
    }
    Set<RealValued> directPredict = new TreeSet<RealValued>();
    directPredict.add(invGoodness);

    Objective objective = new Objective(better, fail);

    Map<String, String> settings = new HashMap<String, String>();
    template = new AppTemplate("synth", settings, fe, objective, space, plotting, directPredict);

    int totalSize = 1;
    int dims[] = new int[space.size()];
    int i = 0;
    for (TuningKnob knob : space) {
      totalSize *= knob.numLegalValues();
      dims[i] = knob.numLegalValues();
      i++;
    }
    

  }

  public KnobSettingSpace getKnobSpace() { return space; }

  public String getName() { return "anonymous synthetic application"; }
  public String getFullName() { return "anonymous synthetic application"; }

  public Point.Status getStatus (Point pt) { return Point.Status.tested; }
  public void     setStatus           (Point point, Point.Status status) { }
  public boolean  hasStartedTesting   (Point point) { return true; }
  public boolean  hasCompletedTesting (Point point) { return true; }

  public void testPoint(Point pt)
  {
    // pt.status = Point.Status.tested;
    // I don't think synthetic apps have to do anything to test a point
  }

  public Double getSensorReading(Point point, Sensor s)
  {
    boolean defBadness = s.compareTo(badness) == 0;
    boolean defGoodness = s.compareTo(goodness) == 0;
    assert(defGoodness || defBadness);

    // This code will go off into the weeds if (#dimensions >< 2)
    int xMax = space.get(0).numLegalValues();
    int yMax = space.get(1).numLegalValues();
    double x = space.get(0).valueToCoordinate(point.get(0));
    double y = space.get(1).valueToCoordinate(point.get(1));
    // "normalized" x and y
    double nx = x / xMax;
    double ny = y / yMax;
    double circle = Math.sqrt((double)(x * x + y * y));
    // all the different crazy graphs
    switch (props.shape) {
      case 0: // simple 2D ramp
        if (defGoodness) return x + y;
        else return 0.0;
      case 1: // 2D parabola
        if (defGoodness) return (x * y) / 100;
        else return 0.0;
      case 2: // 2D parabola^2
        {
          double tempd = (x * y) / 100.0;
          if (defGoodness) return tempd * tempd / 100.0;
          else return 0.0;
        }
      case 3: // 2D ramp with sawteeth
        if (defGoodness) return x + y + (x % 10) + (y % 10);
        else return 0.0;
      case 4: // 2D ramp with big sawteeth
        if (defGoodness) return (x + y + 10*(x % 10) + 10*(y % 10))/4;
        else return 0.0;
      case 5: // 2D ramp with huge sawteeth
        if (defGoodness) return (x + y + 100*(x % 30) + 100*(y % 30))/40;
        else return 0.0;
      case 6: // One giant discontinuity
        if (defGoodness) return x < 50 ? x + 50.0 : x - 50.0;
        else return 0.0;
      case 7: // One giant discontinuity with a ramp in the other dimension
        if (defGoodness) return (x < 50 ? x + 50.0 : x - 50.0) + y;
        else return 0.0;
      case 8: // Now it's starting to get really interesting
        if (defGoodness) return (x * y % 100) + x + y;
        else return 0.0;
      case 9: // circle with sawteeth
        if (defGoodness) return circle % 32 + circle;
        else return 0.0;
      case 10: // Finally, some noise
        if (defGoodness) return circle % 32 + circle + 20 * rand.nextDouble();
        else return 0.0;
      case 11: // ramp with clean cut-off
        if (defGoodness) return x + y > 150 ? 0 : x + y;
        else return 0.0;
      case 12: // Jaggy cut-off, plus noise
        if (defGoodness) return x + y + 20 * rand.nextDouble() > 150.0 ? 0 : x + y + 20 * rand.nextDouble();
        else return 0.0;
      case 13: // Jaggy early cut-off, plus noise
        if (defGoodness) return x + y + 20 * rand.nextDouble() > 50.0 ? 0 : x + y + 20 * rand.nextDouble();
        else return 0.0;
      case 14: // Jaggy cut-off, plus noise
        if (defGoodness) return x + y + (xMax / 5.0) * rand.nextDouble() > ((xMax + yMax) * 3.0 / 4.0) ? 0 :
            x * y / (xMax / 2.0) + x + y + (xMax / 5.0) * rand.nextDouble();
        else return 0.0;
      case 15: // Jaggy cut-off, plus noise
        if (defGoodness) return x + y > ((xMax + yMax) * 3.0 / 4.0) ? 0 :
            x * y / (xMax / 2.0) + x + y + (xMax / 5.0) * rand.nextDouble();
        else return 0.0;
      case 16: // 2D ramp with sawteeth
        if (defGoodness) return x + (x % (xMax / 4)) + (0.1 * (y + (y % (yMax / 5))));
        else return 0.0;
      case 17: // Jaggy cut-off, plus noise, mostly successes
        if (defGoodness) return x + y > ((xMax + yMax) * 3.0 / 4.0) ? - (x + y) :
            x * y / (xMax / 2.0) + x + y + (xMax / 5.0) * rand.nextDouble();
        else return 0.0;
      case 18: // Jaggy cut-off, plus noise, mostly failures
        if (defGoodness) return x + y > ((xMax + yMax) * 1.0 / 10.0) ? - (x + y) :
            x * y / (xMax / 2.0) + x + y + (xMax / 5.0) * rand.nextDouble();
        else return 0.0;
      case 19: // off-center pyramid
        if (defGoodness) {
          if (x < ((1.0 * xMax) / 5.0)) {
            if (y < ((1.0 * yMax) / 5.0)) {
              return (3.0 * (xMax + yMax) / 5.0) + x + y;
            }
            else {
              return x + (8.0 * yMax / 5.0) - y;
            }
          }
          else {
            if (y < ((1.0 * yMax) / 5.0)) {
              return y + (8.0 * xMax / 5.0) - x;
            }
            else {
              return (xMax + yMax) - (x + y);
            }
          }
        }
        else return 0.0;
      case 20: // Attempt to replicate picture from "A Scalable Auto-tuning Framework for Compiler Optimization"
        {
          boolean mask1 = x > (xMax / 50.0);
          double term1 = (mask1) ? 10.0 : 0.0;
          boolean mask2 = 4.0 * x > y;
          double term2 = (mask1 && mask2) ? 50.0 - ((40.0 * x / xMax) + (10.0 * y / yMax)) : 0.0;
          boolean mask3 = Math.abs((2.0 * y) - x) < ((xMax+yMax) / 10.0);
          double term3 = (mask1 && mask2 && mask3) ? x + y : 0.0;
          if (defGoodness) return term1 + term2 + term3;
          else return 0.0;
        }
      case 21: // like 20, but narrower
        {
          boolean mask1 = nx > 0.1;
          double  term1 = (mask1) ? 10.0 : 0.0;
          boolean mask2 = 4.0 * nx > ny;
          double  term2 = (mask1 && mask2) ? 50.0 - (60.0 * nx + 30.0 * ny) : 0.0;
          boolean mask3 = Math.abs((2.0 * ny) - nx) < 0.1;
          double  term3 = (mask1 && mask2 && mask3) ? 80.0 * (nx + ny) : 0.0;
          if (defGoodness) return term1 + term2 + term3;
          else return 0.0;
        }
      case 22: // now we're playing with failures
        {
          double badness = 20.0 * (x + y) / (xMax + yMax);
          double goodness = x * y;
          if (defGoodness) if (badness > 10.0) return null; else return goodness;
          else return badness;
        }
      case 23: // 
        {
          double badness = 40.0 * Math.max(x, y) / (xMax + yMax);
          double goodness = x + y;
          if (defGoodness) if (badness > 10.0) return null; else return goodness;
          else return badness;
        }
      case 24: // Jaggy cut-off, plus noise, actual failures
      {
        Double badness = 100.0 * (x + y) / (xMax + yMax);
        Double goodness = x * y / (xMax / 2.0) + x + y + (xMax / 5.0) * rand.nextDouble();
        if (false) { System.out.printf("%s %s %s???\n", defGoodness, badness, goodness); }
        return (defGoodness) ? ((badness > 10.0) ? null : goodness) : badness;
      }
      case 25: // goodness and badness with different slopes in different dimensions
      {
        double badness  = (nx + 3.0 * ny) * 5.0;
        double goodness = (ny + 3.0 * nx) * 5.0;
        if (defGoodness) {
          if (badness > 10.0) { return null; }
          else return goodness;
        }
        else { return badness; }
      }
      case 26: // Just like 25, but more extreme
      {
        double badness  = (nx + 5.0 * ny) * 3.0;
        double goodness = (ny + 5.0 * nx) * 3.0;
        if (defGoodness) {
          if (badness > 10.0) { return null; }
          else return goodness;
        }
        else { return badness; }
      }
      case 27: // Just like 25, but more extreme
      {
        double badness  = (nx + 5.0 * ny) * 3.0;
        double mx = (nx < 0.5) ? nx : (1.0 - nx);
        double goodness = 100.0 + (ny + 5.0 * mx) * 3.0;
        if (defGoodness) {
          if (badness > 10.0) { return null; }
          else return goodness;
        }
        else { return badness; }
      }
      case 28: // like 21, but even narrower
      {
        boolean mask1 = nx > 0.1;
        double  term1 = (mask1) ? 10.0 : 0.0;
        boolean mask2 = 4.0 * nx > ny;
        double  term2 = (mask1 && mask2) ? 50.0 - (60.0 * nx + 30.0 * ny) : 0.0;
        boolean mask3 = Math.abs((2.0 * ny) - nx) < 0.05;
        double  term3 = (mask1 && mask2 && mask3) ? 80.0 * (nx + ny) : 0.0;
        if (defGoodness) return term1 + term2 + term3;
        else return 0.0;
      }
      case 29: // evil parabola
      {
        if (defGoodness) return nx * ny - nx * nx * ny * ny;
        else return 0.0;
      }
      case 30: // evil parabola 2
      {
        double nxny = nx * ny;
        if (defGoodness) return nxny - 10.0 * nxny * nxny;
        else return 0.0;
      }
      case 31: // will ln work?
      {
        if (defGoodness) return 100.0 + Math.log(nx+ny + 0.0001);
        else return 0.0;
      }
      case 32: // lower and deeper ln
      {
        if (defGoodness) return 100.0 * Math.log(nx*ny + 0.00001);
        else return 0.0;
      }
      case 33: // More evil parabolas
      {
        double goodness = nx + ny - 1.0 / (nx * ny + 0.0001);
        double badness = 6.0 * (nx + ny);
        if (defGoodness) { return (badness > 10.0) ? null : goodness; }
        else { return badness; }
      }
      case 34: // Less evil
      {
        double goodness = nx + ny - 1.0 / ((nx + 0.1) * (ny + 0.1));
        double badness = 6.0 * (nx + ny);
        if (defGoodness) { return (badness > 10.0) ? null : goodness; }
        else { return badness; }
      }
      case 35: // Much less evil
      {
        double goodness = nx + ny - 1.0 / ((nx + 0.3) * (ny + 0.3));
        double badness = 6.0 * (nx + ny);
        if (defGoodness) { return (badness > 10.0) ? null : goodness; }
        else { return badness; }
      }
      default:
        assert(false);
    }
    
    return null;
  }

  public void setFailureCause(Point pt, Failure f, Boolean isCause) { }

  public Boolean isCauseOfFailure(Point pt, Failure f)
  {
    assert(pt != null);
    assert(f != null);
    
    if (f.compareTo(fail) == 0) {
      return getSensorReading(pt, badness) > 10.0;
    }
    else {
      return false;
    }
  }
  public void setSensorReading(Point point, Sensor s, double v) { assert(false); return; }
  public Pair<Double, Double> getSensorPrediction(Point point, Sensor s) { return null; }
  public void setSensorPrediction(Point point, Sensor s, Pair<Double, Double> p) {  }
  // public Pair<Double, Double> getSensorPrediction(Point point, Sensor s)
  // { return point.getSensorPrediction(s); }
  // public void setSensorPrediction(Point point, Sensor s, Pair<Double, Double> p)
  // { point.setSensorPrediction(s, p); }

  // public boolean isPredictionCached (Point p, Feature f) { return p.getPrediction(f)  != null; }
  // public boolean isValueCached      (Point p, Feature f) { return p.getValue(f)       != null; }
  public boolean isPredictionCached (Point p, Feature f) { return false; }
  public boolean isValueCached      (Point p, Feature f) { return false; }
  public Object getCachedPrediction (Point p, Feature f) { return null; }
  public Object getCachedValue      (Point p, Feature f) { return null; }
  public void updatePredictionCache (Point p, Feature f, Object v) {  }
  public void updateValueCache      (Point p, Feature f, Object v) {  }
  //public Object getCachedPrediction (Point p, Feature f) { return p.getPrediction(f); }
  //public Object getCachedValue      (Point p, Feature f) { return p.getValue(f); }
  //public void updatePredictionCache (Point p, Feature f, Object v) { p.setPrediction(f, v); }
  //public void updateValueCache      (Point p, Feature f, Object v) { p.setValue(f, v); }
  public void clearCaches ()
  {
    // for (Point pt : tested) {
    //   pt.clearCaches();
    // }
  }

  void writeSearchToNewExcelFile (SingleSearch search, String moreInfo)
  {
    // FIXME: implement this later
  }

  public int compareTo (Application other)
  {
    return getFullName().compareTo(other.getFullName());
  }

  public Map<String, String> getSettings() { assert (false); return null; }
}
