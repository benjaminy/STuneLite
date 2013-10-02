/*
 *
 */

import java.util.Iterator;
import java.util.Collection;
import java.util.Vector;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Random;
import java.util.logging.Logger;
import com.mallardsoft.tuple.*;

public class SingleSearch implements ValueAndPredictionSource {
  public Application                    app             = null;
  protected TuningKnobSearchProperties  props           = null;
  public List <TestRecord>              tested          = null;
  Set<Point>                            candidates      = null;
  List <TestRecord>                     selected        = null;
  protected Logger                      l               = null;
  protected Random                      rGen            = null;
  protected SearchStats                 stats           = null;
  protected int                         id              = -1;
  protected int                         numPointsTested = 0;

  protected Map<Point, Map<Feature, Object>> predictionCaches = null;
  protected Map<Point, Map<Feature, Object>> valueCaches = null;
  protected Map<Point, Double> heuristicScoreCache = null;
  protected Map<Feature, Object> aggregateCache = null;
  protected Map<Feature, Map<BooleanValued, Double>> pointDispersionCache = null;
  protected Map<Failure, Integer> failureModeCounts = null;

  protected Map<RealValued, Vector<Double>>     scalingFactorsMap = null;

  protected Map<Point, Double> mirrorPointValuesCache = null;
  protected RealValued currentMirrorCache = null;

  //  Map<String, String> notes = null;

  public SingleSearch(TuningKnobSearchProperties p, Application a, Random r, SearchStats s, int i)
  {
    assert (p != null);
    assert (a != null);
    assert (r != null);
    assert (s != null);
    props = p;
    l = Logger.getLogger(props.defaultLoggerName);
    id = i;
    app = a;
    rGen = r;
    stats = s;
    predictionCaches      = new TreeMap<Point, Map<Feature, Object>>();
    valueCaches           = new TreeMap<Point, Map<Feature, Object>>();
    heuristicScoreCache   = new TreeMap<Point, Double>();
    scalingFactorsMap     = new TreeMap<RealValued, Vector<Double>>();
    aggregateCache        = new TreeMap<Feature, Object>();
    pointDispersionCache  = new TreeMap<Feature, Map<BooleanValued, Double>>();
    failureModeCounts     = new TreeMap<Failure, Integer>();
    for (Feature f : app.getTemplate().getFeatures()) {
      if (f instanceof RealValued) {
        setScalingFactors((RealValued)f, app.getTemplate().getKnobSpace().unitScalingFactors());
      }
    }

    //    notes = new HashMap<String, String>();

  }

  public TuningKnobSearchProperties getProps()
  { return props; }
  public int getNumPointsTested()
  { return numPointsTested; }
  public Vector<Double> getScalingFactors(RealValued f)
  { return scalingFactorsMap.get(f); }

  public void setScalingFactors(RealValued f, Vector<Double> s)
  { scalingFactorsMap.put(f, s); }

  // We want to record what the predictions and values were at each step of the search
  public class TestRecord
  {
    public Point pt = null;
    public Map<Feature, Object> pred = null;
    public Map<Feature, Object> vals = null;
    public TestRecord(Point p, Map<Feature, Object> pr, Map<Feature, Object> v)
    {
      pt = p;
      pred = pr;
      vals = v;
    }
  }

  boolean ptInTestRecords(Iterable<TestRecord> recs, Point pt)
  {
    for (TestRecord rec : recs) {
      if (rec.pt.equals(pt)) {
        return true;
      }
    }
    return false;
  }

  Set<Point> extractPoints(Iterable<TestRecord> recs)
  {
    Set<Point> l = new TreeSet<Point>();
    for (TestRecord rec : recs) {
      l.add(rec.pt);
    }
    return l;
  }

  public Set<Point> getTestedPoints() { return extractPoints(tested); }
  public KnobSettingSpace getKnobSpace() { return app.getTemplate().getKnobSpace(); }

  public void testPoint (Point pt)
  {
    if (true) {l.info ("SingleSearch.testPoint: Starting. app("+
      app.getName()+") point("+pt+") id("+id+") num("+numPointsTested+") "+Thread.currentThread()+" "+app.lock);}

    long sleepTime = props.sleepTimeMillis;
    boolean gotLock = false;
    try { gotLock = app.lock.tryLock(sleepTime, java.util.concurrent.TimeUnit.MILLISECONDS); }
    catch (InterruptedException e) { gotLock = false; }
    while (!gotLock) {
      // java.util.concurrent.locks.ReentrantLock rl = (java.util.concurrent.locks.ReentrantLock)app.lock;
      // Thread owner = rl.getOwner();
      if (true) {l.info ("SingleSearch.testPoint: Waiting for lock. app("+app.getName()+") id("+id+") lock("+app.lock+")");}
      sleepTime *= 2;
      try { gotLock = app.lock.tryLock(sleepTime, java.util.concurrent.TimeUnit.MILLISECONDS); }
      catch (InterruptedException e) { gotLock = false; }
    }
    if (app.hasCompletedTesting(pt)) {
      if (true) {l.info ("SingleSearch.testPoint: Already Complete!!! "+
        app.getName()+" "+pt+" id("+id+") "+Thread.currentThread()+" "+app.lock);}
      if (app.getStatus(pt) != Point.Status.tested) {
        l.severe("SingleSearch.testPoint complete but not tested??? " + app.getStatus(pt));
        System.exit(1);
      }
    }
    else {
      if (false) {
        throw new IllegalStateException("Why????? " + numPointsTested + " " + pt + " " + app);
      }
      if (app.hasStartedTesting(pt)) {
        if (true) {l.info ("SingleSearch.testPoint: Waiting. app("+app.getName()+") point("+pt+") id("+id+")");}
        app.lock.unlock();
        while (true) {
          app.lock.lock();
          if (app.hasCompletedTesting(pt)) { break; }
          app.lock.unlock();
          try { Thread.sleep(props.sleepTimeMillis); }
          catch (InterruptedException e) { }
        }
        if (app.getStatus(pt) != Point.Status.tested) {
          l.severe("SingleSearch.testPoint complete but not tested (2)??? " + app.getStatus(pt));
          System.exit(1);
        }
        if (true)
          {l.info ("SingleSearch.testPoint: Complete after looping!!! "+
            app.getName()+" "+pt+" id("+id+") "+Thread.currentThread()+" "+app.lock);}
      }
      else {
        app.testPoint (pt);
        if (app.getStatus(pt) != Point.Status.tested) {
          l.severe("SingleSearch.testPoint tested but not tested??? " + app.getStatus(pt));
          System.exit(1);
        }
      }
    }
    // copy the info out
    assert (app.hasCompletedTesting(pt));
    app.lock.unlock();
    boolean foundPt = false;
    for (int idx = 0; idx < selected.size(); idx++) {
      TestRecord rec = selected.get(idx);
      if (rec.pt.equals(pt)) {
        selected.remove(idx);
        tested.add(rec);
        Map<Feature, Object> valueCache = valueCaches.get(pt);
        assert (valueCache != null);
        for (Feature f : app.getTemplate().getFeatures()) {
          if (!(f instanceof Aggregate || f instanceof Constant)) {
            Object value = f.getValue(this, pt);
            valueCache.put(f, value);
            rec.vals.put(f, value);
          }
        }
        
        numPointsTested++;
        foundPt = true;
        app.writeSearchToNewExcelFile (this, "seed"+id);
        break;
      }
    }
    if (!foundPt) {
      l.severe("Didn't find point after testing "+pt);
      for (int idx = 0; idx < selected.size(); idx++) {
        TestRecord rec = selected.get(idx);
        l.severe("idx: "+idx+" pt:"+rec.pt);
      }
      System.exit(1);
    }
    System.out.printf("  Finished testing point %s (#%d) for search id(%d), %s\n",
      pt, tested.size(), id, app.getName());

    if (false) {l.info ("Finished s.testPoint "+app.getName()+" "+pt+" id("+id+") "+Thread.currentThread()+" "+app.lock);}
    Thread.yield();
  }

  // public abstract boolean       hasStartedTesting       (Point point);
  // public abstract boolean       hasCompletedTesting     (Point point);
  // public abstract boolean       wasSuccessful           (Point point);
  // 
  // public abstract void          setSensorReading        (Point p, Sensor s, double value);
  public boolean isPredictionCached (Point pt, Feature f)
  {
    if (predictionCaches == null) {
      l.severe("null prediction cache!!!");
    }
    if (pt == null) {
      l.severe("null point!!!");
    }
    if (predictionCaches.containsKey(pt)) {
      return predictionCaches.get(pt).containsKey(f);
    }
    else { return false; }
  }
  public boolean isValueCached (Point pt, Feature f)
  {
    if (valueCaches.containsKey(pt)) {
      Map<Feature, Object> ptCache = valueCaches.get(pt);
      return ptCache.containsKey(f);
    }
    if (currentMirrorCache != null) {
      if (f.compareTo(currentMirrorCache) == 0) {
        return mirrorPointValuesCache.containsKey(pt);
      }
      else { return false; }
    }
    else { return false; }
  }
  public Object getCachedPrediction (Point pt, Feature f)
  {
    if (predictionCaches.containsKey(pt)) {
      return predictionCaches.get(pt).get(f);
    }
    else { return null; }
  }
  public Pair<Double, Double> getSensorPrediction (Point pt, Sensor s)
  {
    Pair<Double, Double> prediction = (Pair<Double, Double>)getCachedPrediction(pt, s);
    return prediction;
  }
  public Object getCachedValue (Point pt, Feature f)
  {
    if (valueCaches.containsKey(pt)) {
      return valueCaches.get(pt).get(f);
    }
    if (currentMirrorCache != null) {
      if (f.compareTo(currentMirrorCache) == 0 && mirrorPointValuesCache.containsKey(pt)) {
        return mirrorPointValuesCache.get(pt);
      }
      else { return null; }
    }
    else { return null; }
  }
  public int getFailureModeCount(Failure f)
  {
    return failureModeCounts.get(f);
  }

  public void updatePredictionCache (Point pt, Feature f, Object v)
  {
    Map<Feature, Object> fMap = null;
    if (predictionCaches.containsKey(pt)) {
      fMap = predictionCaches.get(pt);
    }
    else {
      fMap = new TreeMap<Feature, Object>();
      predictionCaches.put(pt, fMap);
    }
    fMap.put(f, v);
  }
  public void updateValueCache (Point pt, Feature f, Object v)
  {
    Map<Feature, Object> fMap = null;
    if (valueCaches.containsKey(pt)) {
      fMap = valueCaches.get(pt);
    }
    else {
      fMap = new TreeMap<Feature, Object>();
      valueCaches.put(pt, fMap);
    }
    fMap.put(f, v);
  }
  public void clearCaches ()
  {
    predictionCaches.clear();
    aggregateCache.clear();
    valueCaches.clear();
    heuristicScoreCache.clear();
    pointDispersionCache.clear();
  }

  // FIXME
  // public Point.Status getStatus(Point pt) { return null; }
  public void setHeuristicScore(Point pt, double score)
  {
    if (Double.isNaN(score)) {
      assert (false);
    }
    if (Double.isInfinite(score)) {
      assert (false);
    }
    heuristicScoreCache.put(pt, score);
  }
  public double getHeuristicScore(Point pt)
  {
    assert (heuristicScoreCache.containsKey(pt));
    return heuristicScoreCache.get(pt);
  }

  public Double getSensorReading (Point pt, Sensor s)
  {
    if (ptInTestRecords(tested, pt)) {
      return app.getSensorReading(pt, s);
    }
    else if (mirrorPointValuesCache.containsKey(pt)) {
      return mirrorPointValuesCache.get(pt);
    }
    else { return null; }
  }
  public Boolean isCauseOfFailure (Point pt, Failure f)
  {
    if (ptInTestRecords(tested, pt)) {
      return app.isCauseOfFailure(pt, f);
    }
    else { return null; }
  }

  Point genRandomPoint(Random rGen)
  {
    KnobSettingSpace space = app.getTemplate().getKnobSpace();
    Point c = new Point(space);
    int k = 0;
    for (TuningKnob knob : space) {
      Object v = knob.nextRandom(rGen);
      c.add(k, v);
      k++;
    }
    return c;
  }

  void doSearch()
  {
    AppTemplate template = app.getTemplate();
    KnobSettingSpace space = template.getKnobSpace();
    int numKnobs = space.size();
    tested = new LinkedList<TestRecord>();
    selected = new LinkedList<TestRecord>();
    numPointsTested = 0;
    
    
    Map<RealValued, Pair<Double, Double>> highestLowest = new TreeMap<RealValued, Pair<Double, Double>>();
    for (RealValued f : template.getPlottingFeatures()) {
      highestLowest.put(f, Tuple.from(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    }
    
    // // First just perform one purely pseudo-randomly generated test
    // {
    //   boolean needsTesting = false;
    //   Point p = genRandomPoint(rGen);
    //   if (!p.hasStartedTesting()) { needsTesting = true; }
    //   else if (p.hasCompletedTesting()) {
    //     tested.add(p);
    //     stats.pointsTested.add(p);
    //     numPointsTested++;
    //   }
    //   else {
    //     System.out.printf("Point currently being tested?\n");
    //     assert(false);
    //   }
    //   if (needsTesting) {
    //     Set<Point> s = new TreeSet<Point>();
    //     s.add(p);
    //     throw new TuningKnobSearch.MissingScore(s);
    //   }
    // }
    
    System.out.printf("props.totalPoints: %d\n", props.totalPoints);
    long time1 = System.currentTimeMillis();
    
    while (numPointsTested < props.totalPoints) {
      clearCaches();

      Point pToTest = null;
      switch (props.pointFinder) {
        case randomPoints:
        {
          pToTest = randomPointFinder(template);
          break;
        }
        case awesomePoints:
        case awesomePointsWithoutFailures:
        {
          pToTest = awesomePointFinder(template);
          break;
        }
      }
      // if (true) { System.out.printf("(cov: %f) pToTest: %s, obj: %s, fail: %s\n", cov, pToTest,
      //   objective.getFeature().getPrediction(this, pToTest),
      //   objective.getFailure().getPrediction(this, pToTest)); }
      if (true) {
        for (RealValued f : template.getPlottingFeatures()) {
          System.out.printf("%s %s    ", f, f.getPrediction(this, pToTest));
        }
        System.out.printf("\n");
      }
      
      Map<Feature, Object> valueCache = new TreeMap<Feature, Object>();
      for (Feature f : template.getFeatures()) {
        if (f instanceof Aggregate || f instanceof Constant) {
          valueCache.put(f, f.getValue(this, pToTest));
        }
      }
      valueCaches.put(pToTest, valueCache);
      if (false) {
        Map<Feature, Object> pc = predictionCaches.get(pToTest);
        Map<Feature, Object> vc = valueCaches.get(pToTest);
        for (Feature f : template.getFeatures()) {
          System.out.printf ("selected caches: f(%s), p(%s), v(%s)\n", f, pc.get(f), vc.get(f));
        }
      }
      selected.add(new TestRecord(pToTest, predictionCaches.get(pToTest), valueCaches.get(pToTest)));
      testPoint(pToTest);
      if (app.getStatus(pToTest) != Point.Status.tested) {
        l.severe("SingleSearch.doSearch status("+app.getStatus(pToTest)+") != \"tested\"!!! (" + Thread.currentThread() +")");
        System.exit(1);
      }
    
      // System.out.printf("picked %d\n", i);
      for (RealValued f : template.getPlottingFeatures()) {
        Double v = f.getValue(this, pToTest);
        if (v != null) {
          Pair<Double, Double> highLow = highestLowest.get(f);
          double high = Math.max(v, Tuple.get1(highLow));
          double low  = Math.min(v, Tuple.get2(highLow));
          highestLowest.put(f, Tuple.from(high, low));
        }
        Pair<Double, Double> highLow = highestLowest.get(f);
        // System.out.println(""+tested.size()+" best:" + best + " Point: " + pToTest + " exp value: " +
        //    pToTest.predicted.finalValue.doubleValue() + " value: " + getObjectiveReading(pToTest));
      }
      
      if (stats == null) {
        l.severe ("Why is stats null?");
        throw new NullPointerException();
      }
      if (stats.pointsTested == null) {
        l.severe ("Why is stats.pointsTested null?");
        throw new NullPointerException();
      }
      stats.pointsTested.add(pToTest);
      long time2 = System.currentTimeMillis();
      System.out.printf("Point %d %s took %dms\n", numPointsTested, pToTest, time2-time1);
      time1 = time2;
    
      // Let's display some visualizations
      if (props.doEveryPointViz) {
        if (numPointsTested > props.everyPointVizLimit) {
          stats.defaultTerminal(this, null);
        }
      }
    }
    
    if (props.displayPlotsAtEndOfSearch) {
      stats.defaultTerminal(this, null);
    }
    
  }

  /*
   *
   */
  protected Point randomPointFinder(AppTemplate template)
  {
    Point c;
    do { c = genRandomPoint(rGen); }
    while (ptInTestRecords(tested, c));
    // System.out.println("Candidate: " + c);
    return c;
  }

  protected Neighborhood makeNeighborhood(Feature f, Collection<Point> ps) {
    switch (props.neighborhoodMethod) {
      case neighborhoodMesh: return new Mesh(props, this, f, ps);
      case neighborhoodNearestK: return new NearestK(props, this, f, ps);
      default: System.err.printf("Whoa! neighborhood?????\n");
    }
    return null;
  }

  /*
   *
   */
  protected Point awesomePointFinder(AppTemplate template)
  {
    KnobSettingSpace space = template.getKnobSpace();

    // Randomly build a set of candidates
    candidates = new HashSet<Point>();
    for (int i = 0; i < props.numCandidates; i++) {
      Point c;
      do { c = genRandomPoint(rGen); }
      while (ptInTestRecords(tested, c));
      candidates.add(c);
      // System.out.println("Candidate: " + c);
    }

    // scale the dimensions
    if (props.doDimensionScaling) { scaleDimensions(); }

    Neighborhood foo = makeNeighborhood(null, extractPoints(tested));

    Map <RealValued, Double> covs = new TreeMap<RealValued, Double>();

    // Count the failures
    for (Feature genericFeature : template.getFeatures()) {
      if (genericFeature instanceof Failure) {
        Failure f = (Failure)genericFeature;
        int count = 0;
        for (TestRecord rec : tested) {
          Boolean val = (Boolean)rec.vals.get(f);
          if (false) { System.out.printf("hello? %s %s %s\n", f, rec.pt, val); }
          if (val != null && val) {
            count++;
          }
        }
        failureModeCounts.put(f, count);
      }
    }

    // For each sensor, try to make a prediction for each candidate
    for (Feature f : template.getFeatures()) {
      if (f instanceof Sensor || template.isFeatureForDirectPrediction(f)) {
        RealValued s = (RealValued)f;
        double cov = Stats.coefficientOfVariation(foo.getDistsBetweenNeighborPairs(getScalingFactors(s)));
        if (Double.isNaN(cov)) {
          // This can happen when we only have one point
          cov = 0.0;
        }
        if (true) { System.out.printf("Predicting for feature: %s cov:%s\n", s, cov); }
        covs.put(s, cov);
        makePredictionsForFeature(s, candidates, cov);
      }
    }
  
    // Now we've filled in all the sensor predictions that we can make.  Try to get
    // objective predictions.
    Boolean haveObjectivePredictions = null, haveFailurePredictions = null;
    Objective objective = template.getObjective();
    for (Point c : candidates) {
      Double objPrediction  = objective.getFeature().getPrediction(this, c);
      Double failPrediction = objective.getFailure().getPrediction(this, c);
      boolean objIsNull  = objPrediction  == null;
      boolean failIsNull = failPrediction == null;
      if (haveObjectivePredictions == null) {
        haveObjectivePredictions = !objIsNull;
      }
      if (haveFailurePredictions == null) {
        haveFailurePredictions = !failIsNull;
      }
      if (haveObjectivePredictions) {
        assert(!objIsNull);
        if (Double.isNaN(objPrediction)) {
          System.err.printf("\n\nEvil NaN objective\n\n");
          for (Feature f : template.getFeatures()) {
            if (f instanceof RealValued) {
              Pair<Double, Double> prd = ((RealValued)f).getPrediction(this, c);
              if (prd != null) {
                System.err.printf("feature=%s mean=%f s.d.=%f ", f.getName(), Tuple.get1(prd), Tuple.get2(prd));
              }
              else { System.out.printf("feature=%s prd=%s ", f.getName(), prd); }
            }
            else if (f instanceof BooleanValued) {
              Double prd = ((BooleanValued)f).getPrediction(this, c);
              System.err.printf("feature=%s prd=%s ", f.getName(), prd);
            }
          }
          System.err.printf("\n\nEvil NaN objective\n\n");
          assert(false);
        }
      }
      else {
        assert(objIsNull);
      }
      if (haveFailurePredictions) {
        assert(!failIsNull);
        if (Double.isNaN(failPrediction)) {
          System.err.printf("\n\nEvil NaN objective\n\n");
          for (Feature f : template.getFeatures()) {
            if (f instanceof RealValued) {
              Pair<Double, Double> prd = ((RealValued)f).getPrediction(this, c);
              if (prd != null) {
                System.err.printf("feature=%s mean=%f s.d.=%f ", f.getName(), Tuple.get1(prd), Tuple.get2(prd));
              }
              else { System.out.printf("feature=%s prd=%s ", f.getName(), prd); }
            }
            else if (f instanceof BooleanValued) {
              Double prd = ((BooleanValued)f).getPrediction(this, c);
              System.err.printf("feature=%s prd=%s ", f.getName(), prd);
            }
          }
          System.err.printf("\n\nEvil NaN objective\n\n");
          assert(false);
        }
      }
      else {
        assert(failIsNull);
      }
    }
  
    assert (haveObjectivePredictions != null);
    assert (haveFailurePredictions != null);
    if (haveObjectivePredictions) {
      if (true) { System.out.printf("Have objective predictions!\n"); }
      assert (haveFailurePredictions); // It doesn't make sense to have obj, but not fail
      for (Point c : candidates) {
        double pObj   = (Double)objective.getFeature().getPrediction(this, c);
        double pFail  = (Double)objective.getFailure().getPrediction(this, c);
        if (false) { // debugging stuff
          Feature goodness = null;
          for (Feature f : template.getFeatures()) {
            if (f.getName().equals("goodness")) { goodness = f; }
          }
          if (goodness != null) {
            Pair<Double, Double> gPred = (Pair<Double, Double>)goodness.getPrediction(this, c);
            System.out.printf("%f %f %f\n", Tuple.get1(gPred), Tuple.get2(gPred), pObj);
          }
        }
        double pSucc = 1.0 - pFail;
        boolean infiniteOrNaN = false;
        if (Double.isNaN(pObj) || Double.isInfinite(pObj)) {
          System.out.printf("pObj:%f feature:%s\n", pObj, objective.getFeature());
          infiniteOrNaN = true;
        }
        if (Double.isNaN(pFail) || Double.isInfinite(pFail)) {
          System.out.printf("pFail:%f feature:%s\n", pFail, objective.getFeature());
          infiniteOrNaN = true;
        }
        if (false || infiniteOrNaN) {
          System.out.printf("pt=%s fe=%f fa=%f ", c, objective.getFeature().getPrediction(this, c),
            objective.getFailure().getPrediction(this, c));
          for (RealValued f : template.getPlottingFeatures()) {
            Pair<Double, Double> prd = f.getPrediction(this, c);
            if (prd != null) {
              System.out.printf("feature=%s mean=%f s.d.=%f ", f.getName(), Tuple.get1(prd), Tuple.get2(prd));
            }
            else { System.out.printf("feature=%s prd=%s ", f.getName(), prd); }
          }
          if (infiniteOrNaN) {
            for (Feature f : template.getFeatures()) {
              if (f instanceof RealValued) {
                Pair<Double, Double> prd = ((RealValued)f).getPrediction(this, c);
                if (prd != null) {
                  System.out.printf("feature=%s mean=%f s.d.=%f ", f.getName(), Tuple.get1(prd), Tuple.get2(prd));
                }
                else { System.out.printf("feature=%s prd=%s ", f.getName(), prd); }
              }
              else if (f instanceof BooleanValued) {
                Double prd = ((BooleanValued)f).getPrediction(this, c);
                System.out.printf("feature=%s prd=%s ", f.getName(), prd);
              }
            }
          }
          System.out.printf("\n");
        }
        assert (!infiniteOrNaN);
        switch (props.pointFinder) {
          case randomPoints:
          { assert(false); break; }
          case awesomePoints: {
            if (true) {
              setHeuristicScore(c, pObj * pSucc);
            }
            else {
              setHeuristicScore(c, pObj * (Math.min(1.0, pSucc + 0.9)));
            }
            break;
          }
          case awesomePointsWithoutFailures:
          {
            setHeuristicScore(c, pObj);
            break;
          }
        }
      }
    }
    else {
      if (haveFailurePredictions && props.pointFinder != TuningKnobSearchProperties.PointFinder.awesomePointsWithoutFailures) {
        if (true) { System.out.printf("Have failure predictions!\n"); }
        for (Point c : candidates) {
          double pFail  = (Double)objective.getFailure().getPrediction(this, c);
          setHeuristicScore(c, 1.0 - pFail);
        }
      }
      else {
        if (true) { System.out.printf("Have no predictions!\n"); }
        for (Point c : candidates) {
          // We don't have anything deep to go on.  Just try to space out points well
          double minDist = Double.POSITIVE_INFINITY;
  
          Vector unitScaling = space.unitScalingFactors();
          for (TestRecord rec : tested) {
            double dist = space.distance(unitScaling,c,rec.pt);
            minDist = Math.min(minDist, dist);
          }

          // also stay away from the boundaries of the space
          Iterator knobSettings = c.iterator();
          Iterator <TuningKnob> knobs = template.getKnobSpace().iterator();
  
          while (knobSettings.hasNext() && knobs.hasNext()) {
            Object setting = knobSettings.next();
            TuningKnob knob = knobs.next();
  
            minDist = Math.min(minDist, knob.valueToCoordinate(setting) - knob.minCoordinateValue());
            minDist = Math.min(minDist, knob.maxCoordinateValue() - knob.valueToCoordinate(setting));
          }
          assert(!(knobSettings.hasNext() || knobs.hasNext()));
  
          setHeuristicScore(c, 1.0 / (1.0 + minDist));
        }
      }
    }

    Point pToTest = CandidateSelection.select(props, candidates, this, rGen);
    return pToTest;
  }

  protected void scaleDimensions()
  {
    AppTemplate template = app.getTemplate();
    KnobSettingSpace space = template.getKnobSpace();
    int numKnobs = space.size();
    // Scale each sensor independently
    for (Feature f : template.getFeatures()) {
      if (f instanceof RealValued) {
        RealValued s = (RealValued)f;
        // Find the points that have readings for this feature
        List <TestRecord> haveReadings = new LinkedList<TestRecord>();
        int count = 0;
        for (TestRecord test : tested) {
          Object val = test.vals.get(s);
          if (val != null) {
            haveReadings.add(test);
            count++;
          }
        }
        // There need to be enough points with readings to get anything reasonable
        if (count > numKnobs) {
          Vector<Double> oldScalingFactors = getScalingFactors(s);
          Vector<Collection<Double>> allAbsoluteSlopes = new Vector<Collection<Double>>();
          for (TuningKnob k : space) { allAbsoluteSlopes.add(new LinkedList<Double>()); }
          // Build a neighborhood for this feature, and compute "epsilon"
          Neighborhood nbhoodForFeature = makeNeighborhood(null, extractPoints(haveReadings));
          double scalingEpsilon = Double.POSITIVE_INFINITY;
          for (Map.Entry<Point, Set<Point>> entry : nbhoodForFeature.entrySet()) {
            Point pt1 = entry.getKey();
            for (Point pt2 : entry.getValue()) {
              scalingEpsilon = Math.min (scalingEpsilon, space.distance(oldScalingFactors, pt1, pt2));
            }
          }
          scalingEpsilon *= props.derivativeDistFactor;
          if (false) { System.out.printf("Scaling epsilon make sense? %4e\n", scalingEpsilon); }

          if (false) {System.out.printf("Slopes makes sense?\n  ");}
          for (TestRecord test : haveReadings) {
            double ptVal = (Double)test.vals.get(s);
            Set<Point> neighbors = nbhoodForFeature.get(test.pt);
            // build the +/- epsilon points in each dimension
            int dimIdx = -1;
            for (TuningKnob knob : space) {
              dimIdx++;
              Point ptNeg = new Point(space);
              Point ptPos = new Point(space);
              Iterator ptSettings = test.pt.iterator();
              Iterator <TuningKnob> knobs = space.iterator();
              Iterator<Double> oldScalingIter = oldScalingFactors.iterator();
              // build the +/- epsilon points for dimension "knob"
              while (ptSettings.hasNext() && knobs.hasNext() && oldScalingIter.hasNext()) {
                Object ptSetting = ptSettings.next();
                TuningKnob knob2 = knobs.next();
                double oldScalingFactor = oldScalingIter.next();
                if (knob.equals(knob2)) {
                  // Careful! Points are represented in unscaled units!
                  double orig = knob.valueToCoordinate(ptSetting);
                  double scaledEpsilon = scalingEpsilon / oldScalingFactor;
                  ptNeg.add(knob.coordinateToValue(orig - scaledEpsilon));
                  ptPos.add(knob.coordinateToValue(orig + scaledEpsilon));
                }
                else {
                  ptNeg.add(ptSetting);
                  ptPos.add(ptSetting);
                }
              }
              assert (!ptSettings.hasNext()); assert (!knobs.hasNext()); assert (!oldScalingIter.hasNext());
              // now get estimates for the points
              Set <Point> posNegNeighbors = new TreeSet<Point>();
              for (Point n : neighbors) { posNegNeighbors.add(n); }
              posNegNeighbors.add(test.pt);
              Collection<Point> ptNegNeighbors = nbhoodForFeature.wouldBeNeighbors(ptNeg, posNegNeighbors);
              if (ptNegNeighbors.size() > 2) {
                double ptNegVal = basePredictionNeighbors(ptNeg, s, ptNegNeighbors);
                double deriv = (ptVal - ptNegVal) / scalingEpsilon;
                if (false) {System.out.printf("%1e p%1e n%1e ", deriv, ptVal, ptNegVal);}
                if (true) {System.out.printf("%1e ", deriv);}
                allAbsoluteSlopes.get(dimIdx).add(Math.abs(deriv));
              }
              Collection<Point> ptPosNeighbors = nbhoodForFeature.wouldBeNeighbors(ptPos, posNegNeighbors);
              if (ptPosNeighbors.size() > 2) {
                double ptPosVal = basePredictionNeighbors(ptPos, s, ptPosNeighbors);
                double deriv = (ptVal - ptPosVal) / scalingEpsilon;
                if (false) {System.out.printf("%1e p%1e o%1e ", deriv, ptVal, ptPosVal);}
                if (true) {System.out.printf("%1e ", deriv);}
                allAbsoluteSlopes.get(dimIdx).add(Math.abs(deriv));
              }
            }
          }
          if (true) {System.out.printf("\n");}

          // We're "encoding" the case where some dimension has no slopes as -infinity
          double minSlope = Double.POSITIVE_INFINITY;
          Vector<Double> slopes = new Vector<Double>();
          for (Collection<Double> absoluteSlopes : allAbsoluteSlopes) {
            double avgSlope = Stats.arithmeticMean(absoluteSlopes);
            // double avgSlope = Stats.geometricMean(props.epsilon, absoluteSlopes);
            // double avgSlope = Stats.harmonicMean(props.epsilon, absoluteSlopes);
            if (true) { System.out.printf("avg %2e\n", avgSlope); }
            slopes.add(avgSlope);
            if (Double.isNaN(avgSlope)) {
              minSlope = Double.NEGATIVE_INFINITY;
            }
            else {
              minSlope = Math.min(minSlope, avgSlope);
            }
          }
          if (minSlope > 0.0) {
            double minScalingFactor = Double.POSITIVE_INFINITY;
            Vector<Double> newScalingFactors = new Vector();
            Iterator<Double> slopeIter = slopes.iterator();
            Iterator<Double> oldScalingIter = oldScalingFactors.iterator();
            while (slopeIter.hasNext() && oldScalingIter.hasNext()) {
              double slope = slopeIter.next();
              double oldScalingFactor = oldScalingIter.next();
              double newScalingFactor = slope * oldScalingFactor / minSlope;
              minScalingFactor = Math.min(minScalingFactor, newScalingFactor);
              newScalingFactors.add(newScalingFactor);
            }
            assert (!slopeIter.hasNext());
            assert (!oldScalingIter.hasNext());
            // renormalize, so that the min scaling factor is 1
            if (true) { System.out.printf("Factors for \"%s\": ", s); }
            for (int i = 0; i < space.size(); i++) {
              double foo = newScalingFactors.get(i) / minScalingFactor;
              newScalingFactors.set(i, foo);
              if (true) { System.out.printf("\"%s\" %4e -- ", space.get(i), foo); }
            }
            if (true) { System.out.printf("\n"); }
            setScalingFactors(s, newScalingFactors);
          }
        }
      }
    }
  }

  protected void makePredictionsForFeature(RealValued s, Collection<Point> candidates, double spreadCoef)
  {
    KnobSettingSpace space = app.getTemplate().getKnobSpace();
    int numKnobs = space.size();
    
    Vector<Double> scaling = getScalingFactors(s);
    int numSuccessfulPoints = 0;
    Set<Point> successfulPoints = new TreeSet<Point>();
    double maxVal = Double.NEGATIVE_INFINITY;
    for (TestRecord rec : tested) {
      Double val = s.getValue(this, rec.pt);
      if (val != null) {
        // System.out.printf("succeeded: %f\n", p.actualScore);
        numSuccessfulPoints++;
        successfulPoints.add(rec.pt);
        maxVal = Math.max(maxVal, val);
      }
    }
    
    // If we don't have enough readings from sensor "f", set all the candidates'
    // predictions to "null" -- i.e., can't predict anything for this sensor.
    if (numSuccessfulPoints < 1) {
      for (Point c : candidates) {
        updatePredictionCache(c, s, null);
      }
    }
    // Otherwise, do a real prediction
    else {
      Neighborhood nbhood = makeNeighborhood(null, successfulPoints);
      // System.out.printf("|tested| = %d |mesh connections| = %d  ratio = %f\n",
      //   tested.size(), mesh.numConnections(), (double)mesh.numConnections() / tested.size());

      Collection <Double> backgroundRates = neighborNeighborRates(s, nbhood);
      double globalAvgRate = Stats.genericMedian(backgroundRates, 0.5) * spreadCoef;
      double globalMaxRate = Stats.max(backgroundRates);
      if (Double.isNaN(globalMaxRate) || globalMaxRate < props.epsilon) {
        // If there are no slopes anywhere, just make one up to encourage searching away from points
        if (Math.abs(maxVal) < props.epsilon) {
          maxVal = 1.0;
        }
        globalAvgRate = Math.abs(maxVal) / space.maxDistance(scaling);
      }

      Set<Point> ptsWithMirrors = addMirrorPoints(s, nbhood, scaling);
      Neighborhood nbhoodWithMirrors = makeNeighborhood(null, ptsWithMirrors);

      for (Point c : candidates) {
        Pair<Double, Double> pred = predictSensor(c, s, ptsWithMirrors, nbhoodWithMirrors, globalAvgRate, spreadCoef, scaling);
        updatePredictionCache(c, s, pred);
      }
      // Map<Point, Collection<Pair<Double, Double>>> ratesMap = new TreeMap<Point, Collection<Pair<Double, Double>>>();
      // Map<Point, Double> predictionMap = new TreeMap<Point, Double>();
      // double rateTotal = 0.0;
      // int rateCount = 0;
      // Collection<Double> allRates = new LinkedList<Double>();
      // for (Point c : candidates) {
      //   Collection<Pair<Double, Double>> rates = new LinkedList<Pair<Double, Double>>();
      //   ratesMap.put(c, rates);
      //   Pair<Double, Double> pred = predictSensor(c, s, successfulPoints, nbhood, Double.NaN, spreadCoef, rates);
      //   predictionMap.put(c, Tuple.get1(pred));
      //   for (Pair<Double, Double> distRate : rates) {
      //     Double rate = Tuple.get2(distRate);
      //     if (!rate.isNaN()) {
      //       allRates.add(rate);
      //       rateTotal += rate;
      //       rateCount++;
      //     }
      //   }
      //   if (false) { System.out.printf("pt=%s mean=%f stddev%f rateCount=%d\n", c, Tuple.get1(pred), Tuple.get2(pred), rateCount); }
      //   // setSensorPrediction(c, (Sensor)f, pred);
      //   // System.out.printf("fv: %10f pt:%s\n", p.predicted.finalValue, p);
      // }
      // 
      // // double globalAvgRate = 1.0 * rateTotal / (spreadCoef * (double)rateCount);
      // double globalAvgRate = Stats.genericMedian(allRates, 0.5) / spreadCoef;
      // if (Double.isNaN(globalAvgRate)) {
      //   globalAvgRate = props.epsilon;
      // }
      // if (true) { System.out.printf("  %s %f\n", s.getName(), globalAvgRate); }
      // 
      // for (Map.Entry<Point, Collection<Pair<Double, Double>>> ratesMapEntry : ratesMap.entrySet()) {
      //   Point c = ratesMapEntry.getKey();
      //   double valDiffMin = Double.POSITIVE_INFINITY;
      //   double invValDiffTotal = 0.0;
      //   int count = 0;
      //   for (Pair<Double, Double> distRate : ratesMapEntry.getValue()) {
      //     double dist = Tuple.get1(distRate);
      //     double localRate = Tuple.get2(distRate);
      //     double effRate = (((Double)localRate).isNaN() || globalAvgRate > localRate) ? globalAvgRate : localRate;
      //     double effValDiff = dist * effRate;
      //     if (false) { System.out.printf("lr:%f gr:%f er:%f d:%f df:%f\n",
      //           localRate, globalAvgRate, effRate, dist, effValDiff); }
      //     valDiffMin = Math.min(valDiffMin, effValDiff);
      //     invValDiffTotal += 1.0 / effValDiff;
      //     count++;
      //   }
      //   double valDiffHarmonicMean = ((double)count) / invValDiffTotal;
      //   if (false) {System.out.printf(" min:%f harm:%f\n", valDiffMin, valDiffHarmonicMean);}
      //   Pair<Double, Double> pred = Tuple.from(predictionMap.get(c), valDiffMin);
      //   // Pair<Double, Double> pred = Tuple.from(predictionMap.get(c), valDiffHarmonicMean);
      //   if (false) { System.out.printf("pt=%s pred=%s\n", c, pred); }
      //   updatePredictionCache(c, s, pred);
      // }
    }
  }

  /* addMirrorPoints adds copies of the points at the "edges" of the tuning
   * space, reflecting across the boundary of the space. */
  Set<Point> addMirrorPoints(RealValued feature, Neighborhood nbhood, Vector<Double> scalingFactors)
  {
    KnobSettingSpace space = app.getTemplate().getKnobSpace();
    Set<Point> inputPts = nbhood.keySet();
    double epsilon = space.maxDistance(scalingFactors);
    for (Point pt : inputPts) {
      for (Point other : nbhood.get(pt)) {
        epsilon = Math.min(epsilon, space.distance(scalingFactors, pt, other));
      }
    }
    epsilon /= 2.0;
    Set<Point> pts = new TreeSet<Point>();
    mirrorPointValuesCache = new TreeMap<Point, Double>();
    currentMirrorCache = feature;
    for (Point pt : inputPts) {
      pts.add(pt);
      Set<Point> neighborNeighbors = nbhood.get(pt);
      neighborNeighbors.add(pt);
      Iterator<TuningKnob> knobIter = space.iterator();
      Iterator ptSettingIter = pt.iterator();
      while (knobIter.hasNext() && ptSettingIter.hasNext()) {
        TuningKnob knob = knobIter.next();
        double ptSetting = (Double)ptSettingIter.next();
        if (knob instanceof NumericalTuningKnob) {
          // "1.0" is a hack
          double minBound = ((NumericalTuningKnob)knob).minCoordinateValue() - 1.0;
          double maxBound = ((NumericalTuningKnob)knob).maxCoordinateValue() + 1.0;
          Point ptAtMinBound = new Point(pt, knob, minBound);
          Point ptAtMaxBound = new Point(pt, knob, maxBound);
          double ptMinDist = space.distance(scalingFactors, pt, ptAtMinBound);
          double ptMaxDist = space.distance(scalingFactors, pt, ptAtMaxBound);
          boolean closerMinExists = false, closerMaxExists = false;
          // Do closer points exist?
          for (Point ptOther : inputPts) {
            if (!pt.equals(ptOther)) {
              double otherMinDist = space.distance(scalingFactors, ptOther, ptAtMinBound);
              double otherMaxDist = space.distance(scalingFactors, ptOther, ptAtMaxBound);
              closerMinExists |= otherMinDist < ptMinDist;
              closerMaxExists |= otherMaxDist < ptMaxDist;
              // small run time optimization
              if (closerMinExists && closerMaxExists) { break; }
            }
          }
          double val = feature.getValue(this, pt);
          if (false && feature.getName().equals("invGoodness")) {
            System.out.printf("Mirror %s %f\n", pt, val);
          }
          if (!closerMinExists) {
            Point epsilonPt = new Point(pt, knob, ptSetting + epsilon);
            Collection<Point> derivNeighbors = nbhood.wouldBeNeighbors(epsilonPt, neighborNeighbors);
            double epsilonVal = basePredictionNeighbors(epsilonPt, feature, derivNeighbors);
            double deriv = (epsilonVal - val) / epsilon;
            double distFromBound = ptSetting - minBound;
            Point pt1 = new Point(pt, knob, minBound - distFromBound);
            double derivWeight = 1.0 - (distFromBound / (maxBound - minBound));
            double avgVal1 = (val + derivWeight * (val - 2.0 * deriv * distFromBound)) / (1.0 + derivWeight);
            mirrorPointValuesCache.put(pt1, avgVal1);
            pts.add(pt1);
            Point pt2 = new Point(pt, knob, minBound - (2.0*distFromBound));
            double avgVal2 = (val + derivWeight * (val - 3.0 * deriv * distFromBound)) / (1.0 + derivWeight);
            mirrorPointValuesCache.put(pt2, avgVal2);
            pts.add(pt2);
          }
          if (!closerMaxExists) {
            Point epsilonPt = new Point(pt, knob, ptSetting - epsilon);
            Collection<Point> derivNeighbors = nbhood.wouldBeNeighbors(epsilonPt, neighborNeighbors);
            double epsilonVal = basePredictionNeighbors(epsilonPt, feature, derivNeighbors);
            double deriv = (epsilonVal - val) / epsilon;
            double distFromBound = maxBound - ptSetting;
            Point pt1 = new Point(pt, knob, maxBound + distFromBound);
            double derivWeight = 1.0 - (distFromBound / (maxBound - minBound));
            double avgVal1 = (val + derivWeight * (val - 2.0 * deriv * distFromBound)) / (1.0 + derivWeight);
            mirrorPointValuesCache.put(pt1, avgVal1);
            pts.add(pt1);
            Point pt2 = new Point(pt, knob, maxBound + (2.0*distFromBound));
            double avgVal2 = (val + derivWeight * (val - 3.0 * deriv * distFromBound)) / (1.0 + derivWeight);
            mirrorPointValuesCache.put(pt2, avgVal2);
            pts.add(pt2);
            // System.out.printf("Adding mirror point: pt:%s p1:%s\n");
          }
        }
        else {
          l.severe("Don't have non-numerical knobs yet");
          assert (false);
        }
      }
      assert (!knobIter.hasNext());
      assert (!ptSettingIter.hasNext());
    }
    return pts;
  }

  public Pair<Double, Double> predictSensor(Point pt, RealValued feature, Collection<Point> pts, Neighborhood nbhood,
      double globalErrorRate, double cov, Vector<Double> scaling)
  {
    KnobSettingSpace space = app.getTemplate().getKnobSpace();
    // I can't remeber why I thought I needed to make a new neighborhood
    Collection<Point> neighbors = nbhood.wouldBeNeighbors(pt);
    double p = basePredictionNeighbors(pt, feature, neighbors);
    double valDiffTotal = 0.0;
    Collection<Pair<Double, Double>> neighborValues = new LinkedList<Pair<Double, Double>>();
    Collection<Pair<Double, Double>> neighborProjections = new LinkedList<Pair<Double, Double>>();
    double totalProjectionWeight = 0.0;
    double smallestDist = Double.POSITIVE_INFINITY;
    for (Point n : neighbors) {
      double dist = space.distance(scaling, pt, n);
      if (dist > props.epsilon) {
        neighborValues.add(Tuple.from(1.0/dist, feature.getValue(this, n)));
        if (false && feature.getName().equals("invGoodness")) {
          System.out.printf("Neighbor: pt:%s w:%f v:%f\n", n, 1.0/dist, feature.getValue(this, n));
        }
      }
      else { /* weird */ }
      smallestDist = Math.min(smallestDist, dist);
      // get the vector from the neighbor to the candidate
      double distanceSum = 0.0;
      LinkedList<Double> vector = new LinkedList<Double>();
      {
        Iterator candidateSettings  = pt.iterator();
        Iterator neighborSettings   = n.iterator();
        Iterator <TuningKnob> knobs = app.getTemplate().getKnobSpace().iterator();
        while (candidateSettings.hasNext() && neighborSettings.hasNext() && knobs.hasNext()) {
          Object candidateSetting = candidateSettings.next();
          Object neighborSetting  = neighborSettings.next();
          TuningKnob knob  = knobs.next();
          double diff = knob.valueToCoordinate(candidateSetting) - knob.valueToCoordinate(neighborSetting);
          vector.addLast(diff);
          distanceSum += diff * diff;
        }
        assert(!candidateSettings.hasNext());
        assert(!neighborSettings.hasNext());
        assert(!knobs.hasNext());
      }
  
      // build a point that's close to the neighbor, in the opposite direction from the candidate
      double distance = Math.sqrt(distanceSum);
      Point derivPt = new Point(space);
      {
        int kIdx = 0;
        Iterator neighborSettings = n.iterator();
        Iterator <TuningKnob> knobs = app.getTemplate().getKnobSpace().iterator();
        Iterator<Double> vectorComponents = vector.iterator();
        while (neighborSettings.hasNext() && vectorComponents.hasNext() && knobs.hasNext()) {
          Object neighborSetting  = neighborSettings.next();
          TuningKnob knob = knobs.next();
          double v = vectorComponents.next();
          // FIXME: replace 0.5 with a better distance
          derivPt.add(kIdx, knob.valueToCoordinate(neighborSetting) - (0.5 * v / distance));
          kIdx++;
        }
        assert(!neighborSettings.hasNext());
        assert(!vectorComponents.hasNext());
        assert(!knobs.hasNext());
      }
  
      Set<Point> neighborNeighbors = nbhood.get(n);
      neighborNeighbors.add(n);
  
      Collection<Point> derivNeighbors = nbhood.wouldBeNeighbors(derivPt, neighborNeighbors);
      double nVal = Double.NaN, extrapolatedCandidateVal = Double.NaN, derivVal = Double.NaN;
      if (derivNeighbors.size() > 1) {
        derivVal = basePredictionNeighbors(derivPt, feature, derivNeighbors);
        nVal = feature.getValue(this, n);
        // FIXME: 0.5 thing again
        double deriv = (nVal - derivVal) / 0.5;
        extrapolatedCandidateVal = nVal + (deriv * distance);
        if (dist > props.epsilon) {
          double weight = 1.0/(dist*dist);
          neighborProjections.add(Tuple.from(weight, extrapolatedCandidateVal));
          totalProjectionWeight += weight;
        }
        else { /* weird */ }
      }
      // if (false) { System.out.printf("#dn: %d  c: (%f - %f = %f) %s  n: (%f) %s  d: (%f) %s\n", derivNeighbors.size(), 
      //   p, extrapolatedCandidateVal, p - extrapolatedCandidateVal, this, nVal, n, derivVal, derivPt); }
  
      // double effectiveErrorRate = Math.max (n.errorRate, globalErrorRate);
      // minError = Math.min (minError, effectiveErrorRate * distance(n));
    }
    // double avgValDiff = valDiffTotal / ((double)neighborsThatMatter);

    double basicInterpolation = Stats.weightedArithmeticMean(neighborValues);
    neighborProjections.add(Tuple.from(totalProjectionWeight, basicInterpolation));
    if (false && feature.getName().equals("invGoodness")) {
      System.out.printf("Projections: ");
      for (Pair<Double, Double> weightedProjection : neighborProjections) {
        System.out.printf("w:%f v:%f ", Tuple.get1(weightedProjection), Tuple.get2(weightedProjection));
      }
      System.out.printf("\n");
    }
    double mean = Stats.weightedArithmeticMean(neighborProjections);
    double rawStdDev = Stats.weightedStandardDeviation(mean, neighborProjections);
    double stdDev = rawStdDev;
    if (rawStdDev < props.epsilon) {
      // Kind of a hack to get some kind of distribution when there is
      // no variation in the raw data
      stdDev = rawStdDev + (smallestDist * globalErrorRate);
      if (false) { System.out.printf("Foo dev: %f rdev: %f dist: %f ger: %f\n", stdDev, rawStdDev, smallestDist, globalErrorRate); }
    }
    // double floor = smallestDist * globalErrorRate;
    if (true /* feature.getName().equals("goodness")*/) {
      if (false) {System.out.printf("mean: %e  rawStdDev: %e  stdDev: %e  smallestDist: %e  globalErrorRate: %e\n", 
          mean, rawStdDev, stdDev, smallestDist, globalErrorRate);}
    //   if (stdDev < floor) {
    //     if (false) { System.out.printf("F %e %e\n", stdDev, floor);}
    //     stdDev = floor;
    //   }
    //   else { if (false) {System.out.printf("S\n");} }
    }
    return Tuple.from(mean, stdDev);
  }

  double basePredictionNeighbors(Point pt, RealValued feature, Collection<Point> neighbors)
  {
    KnobSettingSpace space = app.getTemplate().getKnobSpace();
    Vector<Double> scaling = getScalingFactors(feature);
    double scores = 0.0;
    double invDists = 0.0;
    for (Point neighbor : neighbors) {
      Double score = feature.getValue(this, neighbor);
      if (score == null) {
        l.severe("Neighbor \""+neighbor+"\" of point \""+pt+"\" has no value for \""+feature+"\"");
        assert(false);
      }
      double invDist = 1.0 / space.distance(scaling, pt, neighbor);
      scores   += invDist * score;
      invDists += invDist;
    }
    return scores / invDists;
  }

  Collection<Double> neighborNeighborRates(RealValued s, Neighborhood nbhood)
  {
    Collection <Double> rates = new LinkedList<Double>();
    Vector<Double> scaling = getScalingFactors(s);
    KnobSettingSpace space = getKnobSpace();
    for (Map.Entry<Point, Set<Point>> entry : nbhood.entrySet()) {
      Point pt1 = entry.getKey();
      double pt1Val = s.getValue(this, pt1);
      for (Point pt2 : entry.getValue()) {
        double absDiff = Math.abs(pt1Val - s.getValue(this, pt2));
        double dist = space.distance(scaling, pt1, pt2);
        if (dist > props.epsilon) {
          rates.add(absDiff/dist);
        }
        else { /* weird */ }
      }
    }
    return rates;
  }

  public Double avgConfidenceInterval(AvgConfidenceInterval main, RealValued feat, RealValued wFeat, BooleanValued filter)
  {
    if (aggregateCache == null) {
      aggregateCache = new TreeMap<Feature, Object>();
    }
    if (aggregateCache.containsKey(main)) {
      return (Double)aggregateCache.get(main);
    }
    // need to compute it
    // Collection <Pair<Double, Double>> intervals = new LinkedList<Pair<Double, Double>>();
    Collection <Double> intervals = new LinkedList<Double>();
    for (Point c : candidates) {
      boolean includePt = true;
      if (filter != null) {
        Double probability = filter.getPrediction(this, c);
        if (probability == null) {
          includePt = false;
        }
        else {
          includePt = probability > (1.0 - props.epsilon);
        }
      }
      if (includePt) {
        // double weight = 1.0;
        // if (wFeat != null) {
        //   Pair<Double, Double> wPred = wFeat.getPrediction(this, c);
        //   if (wPred !=null) {
        //     Double mean = Tuple.get1(wPred);
        //     if (mean != null) {
        //       weight = mean;
        //     }
        //   }
        // }
        Pair<Double, Double> pred = feat.getPrediction(this, c);
        if (pred != null) {
          Double interval = Tuple.get2(pred);
          assert (interval != null);
          // intervals.add(Tuple.from(weight, interval));
          intervals.add(interval);
        }
      }
    }
    if (intervals.size() < 1) {
      return null;
    }
    else {
      // double avg = Stats.weightedArithmeticMean(intervals);
      double avg = Stats.genericMedian(intervals, 0.25);
      return Math.max(avg, props.epsilon);
    }
  }

  public Double getPointDispersion(Feature s, BooleanValued filter)
  {
    KnobSettingSpace space = app.getTemplate().getKnobSpace();
    Map <BooleanValued, Double> cache = pointDispersionCache.get(s);
    if (cache == null) {
      cache = new TreeMap<BooleanValued, Double>();
      pointDispersionCache.put(s, cache);
    }
    if (cache.containsKey(filter)) {
      return cache.get(filter);
    }
    // need to compute it
    Collection<Point> pts = new LinkedList<Point>();
    for (Point pt : extractPoints(tested)) {
      boolean passedFilter = true;
      if (filter != null) {
        Boolean val = filter.getValue(this, pt);
        if (val == null) { passedFilter = false; }
        else { passedFilter = val; }
      }
      if (passedFilter) {
        Object val = s.getValue(this, pt);
        if (val != null) {
          pts.add(pt);
        }
      }
    }

    if (false) { System.out.printf("disp %d\n", pts.size()); }

    Double dispersion = null;
    if (pts.size() > 1) {
      Neighborhood m = makeNeighborhood(null, pts);
      Vector<Double> scalingFactors =
        (s instanceof RealValued) ? getScalingFactors((RealValued)s) : space.unitScalingFactors();
      dispersion = Stats.coefficientOfVariation(m.getDistsBetweenNeighborPairs(scalingFactors));
    }
    cache.put(filter, dispersion);
    return dispersion;
  }
}
