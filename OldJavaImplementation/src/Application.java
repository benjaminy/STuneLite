/*
 *
 */

import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Random;
import java.util.logging.Logger;
import com.mallardsoft.tuple.*;
import java.util.concurrent.locks.*;

public abstract class Application implements Comparable <Application>, ValueSource {
  public    Lock                        lock                    = new ReentrantLock();
  TuningKnobSearchProperties    props     = null;
  protected Set<Point>          tested    = null;
  protected Set<Point>          testing   = null;
  protected AppTemplate         template  = null;
  Logger l = null;

  public Set<Point>             getTestedPoints()
  { Set<Point> copy = new TreeSet<Point>();
    for (Point pt : tested) {copy.add(pt);}
    return copy; }
  public Collection<Point>      getAllPoints() { return null; }
  public AppTemplate            getTemplate() { return template; }

  public abstract String        getName                 ();
  public abstract String        getFullName             ();

  public abstract boolean       hasStartedTesting       (Point point);
  public abstract boolean       hasCompletedTesting     (Point point);

  public abstract void          testPoint               (Point pt);
  public abstract Double        getSensorReading        (Point p, Sensor s);
  public abstract void          setSensorReading        (Point p, Sensor s, double value);
  // public abstract Pair<Double, Double> getSensorPrediction (Point p, Sensor s);
  // public abstract void          setSensorPrediction     (Point p, Sensor s, Pair<Double, Double> distr);
  public abstract void          setFailureCause         (Point pt, Failure f, Boolean isCause);


  // public abstract boolean isPredictionCached (Point p, Feature f);
  public abstract boolean isValueCached (Point p, Feature f);
  // public abstract Object getCachedPrediction (Point p, Feature f);
  public abstract Object getCachedValue (Point p, Feature f);
  // public abstract void updatePredictionCache (Point p, Feature f, Object v);
  public abstract void updateValueCache (Point p, Feature f, Object v);
  // public abstract void          clearCaches             ();

  // FIXME
  public void setNote(Point pt, String k, String n) {}
  public abstract void setStatus(Point pt, Point.Status status);
  public abstract Point.Status getStatus(Point pt);

  private class EvaluateSeed implements Runnable
  {
    private int i = -1;
    private Random rGen = null;
    private SearchStats s = null;
    protected Application app = null;
    public EvaluateSeed(Random r, SearchStats ss, Application a, int ii)
    {
      assert (r != null);
      assert (a != null);
      assert (ss != null);
      rGen = r;
      app = a;
      s = ss;
      i = ii;
    }
    public void run()
    {
      System.out.printf("About to start test with seed=%d\n", i);
      // try {

      // tested = new Set<Point>[props.numSeeds];
      (new SingleSearch(props, app, rGen, s, i)).doSearch();
      // }
      // Missing scores are handled differently now
      // catch (MissingScore e) {
      //   System.out.printf("MISSING SCORES app: %s  seed: %d  |points|: %d\n",
      //     app.getFullName(), seed, e.ps.size());
      //   for (Point p : e.ps) {
      //     System.out.printf("    point: %s\n", p.toFancyString());
      //     app.requestMissingScore(p, p.predicted.baseVal);
      //   }
      // }
    }
  }

  void doSearches(SearchStats stats[]) {
    Random generatorGenerator = new Random(props.globalSeed);
    for (int s = -1; s < props.initSeed; s++) {
      long discard = generatorGenerator.nextLong();
    }
    
    if (props.multiThreadSeeds) {
      Collection<Thread> threads = new LinkedList<Thread>();
      
      for (int seed = props.initSeed; seed < props.numSeeds + props.initSeed; seed++) {
        SearchStats s = null;
        if (stats != null) {
          s = stats[seed - props.initSeed];
          if (s == null) {
            l.severe("Why is stats null? "+seed+" "+props.numSeeds);
            System.exit(1);
            assert (false);
          }
        }
        Random rGen = new Random(generatorGenerator.nextLong());
        Thread t = new Thread(new EvaluateSeed(rGen, s, this, seed));
        threads.add(t);
        t.start();
      }

      boolean allDone = false;
      while (!allDone) {
        allDone = true;
        try {
          for (Thread t : threads) {
            t.join();
          }
        }
        catch (InterruptedException e) {
          allDone = false;
        }
      }
    }
    else {
      for (int seed = props.initSeed; seed < props.numSeeds + props.initSeed; seed++) {
        SearchStats s = stats == null ? null : stats[seed - props.initSeed];
        (new EvaluateSeed(new Random(generatorGenerator.nextLong()), s, this, seed)).run();
      }
    } 
  }

  abstract void writeSearchToNewExcelFile (SingleSearch search, String moreInfo);

  public abstract Map<String, String> getSettings();
}
