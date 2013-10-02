/*
 *
 */

// import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
// import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
// import java.util.StringTokenizer;
// import java.util.Collection;
import com.mallardsoft.tuple.*;

import java.util.concurrent.locks.*;

import java.io.*;
import java.util.logging.Logger;

// import com.panayotis.gnuplot.JavaPlot;
// import com.panayotis.gnuplot.layout.StripeLayout;
// import com.panayotis.gnuplot.layout.AutoGraphLayout;
// import com.panayotis.iodebug.Debug;
// import com.panayotis.gnuplot.plot.AbstractPlot;
// import com.panayotis.gnuplot.plot.DataSetPlot;
// import com.panayotis.gnuplot.plot.DataSetLinePlot;
// import com.panayotis.gnuplot.style.NamedPlotColor;
// import com.panayotis.gnuplot.style.PlotStyle;
// import com.panayotis.gnuplot.style.Style;
// import com.panayotis.gnuplot.terminal.PostscriptTerminal;
// 
// 
// 
// //public class FileScoreSource implements ScoreSource
// //{
// //  public Map<Integer, Integer> lastRowIdxOnSheet;
// //  public Collection<App> getApps() { return apps.values(); }
// //
// //  private static final String templateFilename = "expSet_TKTemplate";
// //
// //  public static File expDir = new File(MiscUtils.readLineFromCommand("experiment_dir"));
// //  public static File expFilesDir = new File(expDir, "ExpSettingsFiles");
// //
// //
// //  public void requestMissingScore(App app, Point point, double est)
// //  {
// //    Double score  = ((FileApp)app).scores.get(point);
// //    Double oldEst = ((FileApp)app).estimates.get(point);
// //    // if we already have a score or estimate, ignore
// //    if (score != null || oldEst != null)
// //      return;
// //    ((FileApp)app).newEstimates.put(point, est);
// //  }
// //

public class MosaicApp extends Application
{
  protected MosaicSystemInterface       sys                     = null;
  protected String                      name                    = null;
  protected Architecture                arch                    = null;
  // protected Map<TuningKnob, Integer>    knobColumnMap           = null;
  // protected Map<Feature, Integer>       featureColumnMap        = null;
  // protected Map<BooleanValued, Integer> probColumnMap           = null;
  // protected Map<RealValued, Integer>    meanColumnMap           = null;
  // protected Map<RealValued, Integer>    stdDevColumnMap         = null;
  // protected Map<Failure, Integer>       failureColumnMap        = null;
  // protected Map<Point, Integer>         pointRowMap             = null;
  // protected Map<Point, String>          expSettingsFilenames    = null;
  protected Map<String, Integer>        notesCols               = null;

  protected Map<Point, Map<Sensor, Double>> sensorReadings      = null;
  protected Map<Point, Map<Feature, Object>> valueCaches        = null;
  protected Map<Point, Map<Failure, Boolean>> failureCaches     = null;

  // columns in the tuning knobs experiments spreadsheet where data goes
  // public Integer statusCol = null;
  // public Integer expSettingsCol = null;
  // public Integer objectiveCol = null;
  //   public Map<Point, Double> failureFactors = null, estimates = null, newEstimates = null;
  //   public Map<Sensor, Map<Point, Double>> sensorReadings = null;
  //   public Map<String, String> globalSettings;
  //   public int sheetIdx;
  // public int lastRowIdx;
  //   Map<String, Sensor> sensorTable;

  public MosaicApp(MosaicApp app) {
    props               = app.props;
    l                   = Logger.getLogger(props.defaultLoggerName);
    sys                 = app.sys;
    name                = new String(app.name);
    template            = app.template;
    arch                = app.arch;
    // knobColumnMap       = app.knobColumnMap;
    // featureColumnMap    = app.featureColumnMap;
    // probColumnMap       = app.probColumnMap;
    // meanColumnMap       = app.meanColumnMap;
    // stdDevColumnMap     = app.stdDevColumnMap;
    // failureColumnMap    = app.failureColumnMap;
    // pointRowMap         = app.pointRowMap;
    // expSettingsFilenames= app.expSettingsFilenames;
    notesCols           = app.notesCols;
    sensorReadings      = new TreeMap<Point, Map<Sensor, Double>>();
    valueCaches         = new TreeMap<Point, Map<Feature, Object>>();
    failureCaches       = new TreeMap<Point, Map<Failure, Boolean>>();
    tested              = new TreeSet<Point>();
    testing             = new TreeSet<Point>();
  }

  public MosaicApp(TuningKnobSearchProperties p, String n, AppTemplate t, Architecture a)
  {
    assert (p != null);
    assert (n != null);
    assert (t != null);
    assert (a != null);
    props               = p;
    l                   = Logger.getLogger(props.defaultLoggerName);
    name                = new String(n);
    template            = t;
    arch                = a;
    // knobColumnMap       = new TreeMap<TuningKnob, Integer>();
    // featureColumnMap    = new TreeMap<Feature, Integer>();
    // probColumnMap       = new TreeMap<BooleanValued, Integer>();
    // meanColumnMap       = new TreeMap<RealValued, Integer>();
    // stdDevColumnMap     = new TreeMap<RealValued, Integer>();
    // failureColumnMap    = new TreeMap<Failure, Integer>();
    // pointRowMap         = new TreeMap<Point, Integer>();
    // expSettingsFilenames= new TreeMap<Point, String>();
    notesCols           = new HashMap<String, Integer>();
    tested              = new TreeSet<Point>();
    testing             = new TreeSet<Point>();
    sensorReadings      = new TreeMap<Point, Map<Sensor, Double>>();
    valueCaches         = new TreeMap<Point, Map<Feature, Object>>();
    failureCaches       = new TreeMap<Point, Map<Failure, Boolean>>();
//     globalSettings = s;
//     sensorTable = new HashMap<String, Sensor>();
  }

  public void setSystemInterface(MosaicSystemInterface s)
  {
    assert (s != null);
    sys = s;
  }

  public Map<String, String> getSettings()
  {
    Map<String, String> s = new HashMap<String, String>();
    for (Map.Entry<String, String> templateSetting : template.getSettings()) {
      s.put(templateSetting.getKey(), templateSetting.getValue());
    }
    s.put("arch/numberOfClusters", ""+arch.numberOfClusters);
    s.put("arch/numberOfMems", ""+arch.numberOfMems);
    s.put("arch/clusterName", arch.clusterName);
    return s;
  }


  public KnobSettingSpace getKnobSpace() { return getTemplate().getKnobSpace(); }
  Architecture getArchitecture  ()                { return arch; }
  // Integer getKnobColumnIdx      (TuningKnob k)    { return knobColumnMap.get(k); }
  // Integer getFeatureColumnIdx   (Feature f)       { return featureColumnMap.get(f); }
  // Integer getProbColumnIdx      (BooleanValued f) { return probColumnMap.get(f); }
  // Integer getMeanColumnIdx      (RealValued f)    { return meanColumnMap.get(f); }
  // Integer getStdDevColumnIdx    (RealValued f)    { return stdDevColumnMap.get(f); }
  // Integer getFailureColumnIdx   (Failure f)       { return failureColumnMap.get(f); }
  // Integer getNoteColumnIdx      (String n)        { return notesCols.get(n); }
  // Integer getPointRowIdx        (Point pt)        { return pointRowMap.get(pt); }
  // String  getExpSettingsFilename(Point pt)        { return expSettingsFilenames.get(pt); }
  // Set<Map.Entry<String, Integer>> getNoteCols ()  { return notesCols.entrySet(); }
  Set<String> getNoteKeys       ()                { return notesCols.keySet(); }

  public Point.Status getStatus (Point pt)
  {
    if (tested.contains(pt)) {
      return Point.Status.tested;
    }
    else if (testing.contains(pt)) {
      return Point.Status.testing;
    }
    else {
      return Point.Status.candidate;
    }
  }
  public void setStatus (Point pt, Point.Status status)
  {
    switch (status) {
      case candidate:
      {
        l.severe ("blarg1");
        throw new IllegalArgumentException();
      }
      case testing:
      {
        testing.add(pt);
        break;
      }
      case tested:
      {
        testing.remove(pt);
        tested.add(pt);
        break;
      }
      default:
      {
        l.severe ("Unknown status");
        throw new IllegalArgumentException();
      }
    }
  }

  // void setKnobColumnIdx(TuningKnob knob, Integer idx)
  // {
  //   assert (knob != null);
  //   assert (idx != null);
  //   boolean found = false;
  //   for (TuningKnob k2 : template.getKnobSpace()) {
  //     if (knob.compareTo(k2) == 0) {
  //       found = true;
  //       break;
  //     }
  //   }
  //   assert (found);
  //   assert (!knobColumnMap.containsKey(knob));
  //   knobColumnMap.put(knob, idx);
  // }

  // void setFeatureValueColumnIdx(Feature f, Integer idx)
  // {
  //   assert (f != null);
  //   assert (idx != null);
  //   boolean found = false;
  //   for (Feature f2 : template.getFeatures()) {
  //     if (f.compareTo(f2) == 0) {
  //       found = true;
  //       break;
  //     }
  //   }
  //   assert (found);
  //   assert (!featureColumnMap.containsKey(f));
  //   featureColumnMap.put(f, idx);
  // }

  // void setProbColumnIdx(BooleanValued f, Integer idx)
  // {
  //   assert (f != null);
  //   assert (idx != null);
  //   boolean found = false;
  //   for (Feature f2 : template.getFeatures()) {
  //     if (f.compareTo(f2) == 0) {
  //       found = true;
  //       break;
  //     }
  //   }
  //   assert (found);
  //   assert (!probColumnMap.containsKey(f));
  //   probColumnMap.put(f, idx);
  // }

  // void setMeanColumnIdx(RealValued f, Integer idx)
  // {
  //   assert (f != null);
  //   assert (idx != null);
  //   boolean found = false;
  //   for (Feature f2 : template.getFeatures()) {
  //     if (f.compareTo(f2) == 0) {
  //       found = true;
  //       break;
  //     }
  //   }
  //   assert (found);
  //   assert (!meanColumnMap.containsKey(f));
  //   meanColumnMap.put(f, idx);
  // }

  // void setStdDevColumnIdx(RealValued f, Integer idx)
  // {
  //   assert (f != null);
  //   assert (idx != null);
  //   boolean found = false;
  //   for (Feature f2 : template.getFeatures()) {
  //     if (f.compareTo(f2) == 0) {
  //       found = true;
  //       break;
  //     }
  //   }
  //   assert (found);
  //   assert (!stdDevColumnMap.containsKey(f));
  //   stdDevColumnMap.put(f, idx);
  // }

  // void setFailureColumnIdx(Failure f, Integer idx)
  // {
  //   assert (f != null);
  //   assert (idx != null);
  //   boolean found = false;
  //   for (Feature f2 : template.getFeatures()) {
  //     if (f.compareTo(f2) == 0) {
  //       found = true;
  //       break;
  //     }
  //   }
  //   assert (found);
  //   assert (!failureColumnMap.containsKey(f));
  //   failureColumnMap.put(f, idx);
  // }

  // void setNoteColumnIdx(String n, Integer idx)
  // {
  //   assert (n != null);
  //   assert (idx != null);
  //   // maybe insist on finding notes at some point
  //   // boolean found = false;
  //   // for (Feature f2 : template.getFeatures()) {
  //   //   if (f.compareTo(f2) == 0) {
  //   //     found = true;
  //   //     break;
  //   //   }
  //   // }
  //   // assert (found);
  //   assert (!notesCols.containsKey(n));
  //   notesCols.put(n, idx);
  // }

  // void setPointRowIdx(Point pt, Integer idx)
  // {
  //   assert (pt != null);
  //   assert (idx != null);
  //   assert (!pointRowMap.containsKey(pt));
  //   pointRowMap.put(pt, idx);
  // }

  // void setExpSettingsFilename(Point pt, String filename)
  // {
  //   assert (pt != null);
  //   assert (filename != null);
  //   assert (!expSettingsFilenames.containsKey(pt));
  //   expSettingsFilenames.put(pt, filename);
  // }

//   // FIX


  public void clearCaches()
  {
    valueCaches.clear();
  }
  // public boolean isPredictionCached(Point p, Feature f) { assert(false); return false; }
  public boolean isValueCached(Point pt, Feature f)
  {
    Map<Feature, Object> valuesForPt = valueCaches.get(pt);
    if (valuesForPt == null) { return false; }
    return valuesForPt.containsKey(f);
  }
  // public Object getCachedPrediction(Point p, Feature f) { return null; }
  public Object getCachedValue(Point pt, Feature f)
  {
    Map<Feature, Object> valuesForPt = valueCaches.get(pt);
    if (valuesForPt == null) { return null; }
    return valuesForPt.get(f);
  }
  // public void updatePredictionCache(Point p, Feature f, Object v) {}
  public void updateValueCache(Point pt, Feature f, Object v)
  {
    Map<Feature, Object> valuesForPt = valueCaches.get(pt);
    if (valuesForPt == null) {
      valuesForPt = new TreeMap<Feature, Object>();
      valueCaches.put(pt, valuesForPt);
    }
    valuesForPt.put(f, v);
  }

  public Boolean isCauseOfFailure (Point pt, Failure f)
  {
    Map<Failure, Boolean> failuresForPt = failureCaches.get(pt);
    if (failuresForPt == null) { return null; }
    return failuresForPt.get(f);
  }
  public void setFailureCause (Point pt, Failure f, Boolean isCause)
  {
    Map<Failure, Boolean> failuresForPt = failureCaches.get(pt);
    if (failuresForPt == null) {
      failuresForPt = new TreeMap<Failure, Boolean>();
      failureCaches.put(pt, failuresForPt);
    }
    failuresForPt.put(f, isCause);
  }

  public void setSensorReading(Point pt, Sensor sensor, double v)
  {
    Map<Sensor, Double> readingsForPt = sensorReadings.get(pt);
    if (readingsForPt == null) {
      readingsForPt = new TreeMap<Sensor, Double>();
      sensorReadings.put(pt, readingsForPt);
    }
    readingsForPt.put(sensor, v);
  }
  public Double getSensorReading(Point pt, Sensor sensor)
  {
    // System.out.printf("s: %s  pt: %s\n", sensor, point);
    Map<Sensor, Double> readingsForPt = sensorReadings.get(pt);
    if (readingsForPt == null) {
      return null;
    }
    return readingsForPt.get(sensor);
  }
  public void requestMissingScore(Point point, double est)
  {
    // Double score  = 0.0;//scores.get(point);
    // Double oldEst = estimates.get(point);
    // // if we already have a score or estimate, ignore
    // if (score != null || oldEst != null)
    //   return;
    // newEstimates.put(point, est);
  }
  public boolean hasCompletedTesting (Point pt) { return getStatus(pt) == Point.Status.tested; }
  public boolean hasStartedTesting (Point pt)
  {
    return getStatus(pt) == Point.Status.tested || getStatus(pt) == Point.Status.testing;
  }
  public String getName() { return name; }
  public String getFullName() { return name + "_" + arch.getName(); }
  public int compareTo (Application other)
  {
    return getFullName().compareTo(other.getFullName());
  }
  /* Precondition: lock for the app is held. */
  public void testPoint(Point pt)
  {
    l.info ("MosaicApp.testPoint: Starting "+getName()+" "+pt+" "+Thread.currentThread()+" "+lock);
    if (getStatus(pt) == Point.Status.tested) {
      l.info ("MosaicApp.testPoint: Finished--Already Tested!!! "+getName()+" "+pt+" "+Thread.currentThread()+" "+lock);
      return;
    }
    String expSettingsFilename = sys.makeSettingsFile(this, pt);
    setStatus(pt, Point.Status.testing);
    lock.unlock();
    sys.testPoint(this, expSettingsFilename);
    lock.lock();
    sys.findNewSensorAndFailureReading(this, pt, expSettingsFilename);
    setStatus(pt, Point.Status.tested);
    l.info ("MosaicApp.testPoint: Finished "+getName()+" "+pt+" "+Thread.currentThread()+" "+lock);
  }

  void writeSearchToNewExcelFile (SingleSearch search, String moreInfo)
  {
    sys.writeSearchToNewExcelFile (search, moreInfo);
  }

//   public Sensor lookupSensor(String name) { return sensorTable.get(name); }
//   public void addSensor(String name, Sensor s)
//   {
//     sensorTable.put(name, s);
//     Map<Point, Double> readings = new HashMap<Point, Double>();
//     sensorReadings.put(s, readings);
//   }
//   public Collection<Sensor> getSensors() { return sensorTable.values(); }
// 
//   public boolean isFinishedTesting(Point point)
//   { 
//     Double score = getObjectiveReading(point);
//     Double failureFactor = getFailureFactor(point);
//     return score != null || failureFactor != null;
//   }
//   
//   public boolean isSuccessful(Point point)
//   {
//     Double score = getObjectiveReading(point);
//     return score != null;
//   }
//   
//   public Collection<Point> getAllPoints()
//   {
//     return rowIdx.keySet();
//   }
// 
//   public static final int STAT_RUNNING          = 3;
//   public static final int STAT_SUCCESS          = 4;
//   public static final int STAT_SPR_TIMED_OUT    = 5;
//   public static final int STAT_ROUTER_CONGEST   = 6;
//   public static final int MAX_ITERATIONS_PLACER = 7;
//   public static String statusToString(int stat) {
//     if (stat < 2)   return "";
//     if (stat == 2)  return "AddedToSheet";
//     if (stat == 3)  return "Running";
//     if (stat == 4)  return "Success";
//     if (stat == 5)  return "SPRTimedOut";
//     if (stat == 6)  return "RouterCongestion";
//     if (stat == 7)  return "MAX_ITERATIONS_PLACER_FAILURE";
//     return "MysteryErrorStatus";
//   }
//   public boolean wasFailure           (Point p)
//     { return getStatus(p) > 4; }
//   public Double getObjectiveReading(Point point)
//   {
//     return getSensorReading (point, objectiveSensor);
//   }
//   public Double getRawSensorReading(Sensor sensor, Point point)
//   {
//     try { return sensorReadings.get(sensor).get(point); }
//     catch (NullPointerException e) { System.err.printf("! Table missing for sensor %s\n", sensor); return null; }
//   }
// 
//   public Double getFailureFactor(Point point) { return failureFactors.get(point); }
//   public Double getEstimate(Point point) { return estimates.get(point); }
//   public Map<Point, Double> getEstimates() { return estimates; }
// 
// 
//   public boolean equals(Object o)
//   {
//     if (o == null)
//       return false;
//     if (o instanceof FileApp) {
//       FileApp other = (FileApp)o;
//       return name.equals(other.name) && arch.equals(other.arch);
//     }
//     else { return false; }
//   }
// 
//   // if you re-implement equals you have to re-implement hashCode to get hash-
//   // based data structures to work correctly.
//   public int hashCode()
//   { return name.hashCode() * 31 + arch.hashCode(); }
// 

  private boolean infoMissing (String m)
  {
    l.severe("Missing necessary information ("+m+") in app "+toString());
    return false;
  }

  public boolean validate()
  {
    if (name == null)           { return infoMissing("name"); }
    if (arch == null)           { return infoMissing("architecture"); }
    if (template == null)       { return infoMissing("application template"); }
    // if (statusCol == null)      { return infoMissing("status column"); }
    // if (expSettingsCol == null) { return infoMissing("expSettings filename column"); }
    // if (objectiveCol == null)   { return infoMissing("objective column"); }
    // for (TuningKnob knob : template.getKnobSpace()) {
    //   Integer col = knobColumnMap.get(knob);
    //   if (col == null)   { return infoMissing("knob "+knob.toString()+" column"); }
    // }
    // for (Feature f : template.getFeatures()) {
    //   if (f instanceof Sensor) {
    //     Integer fCol = featureColumnMap.get(f);
    //     Integer mCol = meanColumnMap.get(f);
    //     Integer sCol = stdDevColumnMap.get(f);
    //     if (fCol == null)   { return infoMissing(f.toString()+" column"); }
    //     if (mCol == null)   { return infoMissing(f.toString()+" mean column"); }
    //     if (sCol == null)   { return infoMissing(f.toString()+" std dev column"); }
    //   }
    //   else if (f instanceof Failure) {
    //     Integer fCol = failureColumnMap.get(f);
    //     if (fCol == null)   { return infoMissing(f.toString()+" column"); }
    //   }
    // }
    return true;
  }

  public String toString()
  {
    return name + " " + template + " " + arch;
    
    // StringBuffer b = new StringBuffer();
    // b.append(String.format("Application: %s  Cluster Name: %s  Number of Clusters: %d\n  knobs:\n",
    //   name, arch.clusterName, arch.numberOfClusters));
    // for (TuningKnob k : space) {
    //   b.append(String.format("    %s\n", k));
    // }
    // b.append("  Estimates ("+estimates.size()+"):\n");
    // for (Map.Entry<Point, Double> e : estimates.entrySet()) {
    //   b.append(String.format("    %f (%s)\n", e.getValue(), e.getKey()));
    // }
    // b.append("  Scores ("+/*scores.size()*/"foo"+"):\n");
    // // for (Map.Entry<Point, Double> e : scores.entrySet()) {
    // //   b.append(String.format("    %f (%s)\n", e.getValue(), e.getKey()));
    // // }
    // b.append("  FailureFactors ("+failureFactors.size()+"):\n");
    // for (Map.Entry<Point, Double> e : failureFactors.entrySet()) {
    //   b.append(String.format("    %f (%s)\n", e.getValue(), e.getKey()));
    // }
    // return b.toString();
  }
}
