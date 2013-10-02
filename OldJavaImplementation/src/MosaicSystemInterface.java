/*
 *
 */

import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
// import java.util.StringTokenizer;

import java.io.*;
import java.util.logging.Logger;

import com.mallardsoft.tuple.*;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.usermodel.*;

import java.util.concurrent.locks.*;

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

public class MosaicSystemInterface implements TuningKnobSystemInterface
{
  public Map<String, MosaicApp> apps;
  TuningKnobSearchProperties props = null;
  HSSFWorkbook wb = null;
  public Collection<Application> getApps() { return new LinkedList<Application>(apps.values()); }
  Logger l = null;
  protected Lock systemLock = new ReentrantLock();
  PrintStream execLogStream = null;
  protected Set<String> directoriesWhereJobsAreRunning = null;
  File submitJobsScript       = new File(new File(new File("scripts"), "common"), "submitSpecificKindsOfJobs.pl");
  File makeJobsScript         = new File(new File(new File("scripts"), "common"), "mkDAGjobs.pl");
  File allJobsCompleteScript  = new File(new File(new File("scripts"), "common"), "allJobsAreComplete.pl");
  File listRunDirsScript      = new File(new File(new File("scripts"), "common"), "listRunDirectoriesOfRequestedJobs.pl");

  // for now assume the "outside world" will arrange for this program to run in
  // the appropriate experiment directory
  // public static File expDir = new File(MiscUtils.readLineFromCommand("experiment_dir"));
  public static File expFilesDir = new File("ExpSettingsFiles");

  public MosaicSystemInterface(TuningKnobSearchProperties p, Set<MosaicApp> a) {
    //  public FileScoreSource(String f, boolean doFindNewScores) {
    if (true) { System.out.printf("MosaicSystemInterface constructor\n"); }
    assert (a != null);
    assert (p != null);
    props = p;
    l = Logger.getLogger(props.defaultLoggerName);
    if (props.execLogFilename != null) {
      try { execLogStream = new PrintStream(props.execLogFilename); }
      catch (java.io.FileNotFoundException e) {
        l.warning ("Unable to open file "+props.execLogFilename+" for logging");
        execLogStream = null;
      }
    }
    apps = new HashMap<String, MosaicApp>();
    directoriesWhereJobsAreRunning = new HashSet<String>();
    for (MosaicApp app : a) {
      apps.put(app.getName(), app);
    }
    // from somewhere else ...
    //    // boolean doDumpGraphs = true;
    //    // if (doDumpGraphs) {
    //    //   System.out.printf("Assuming \"%s\" is the name of an Excel file\n", args[0]);
    //    //   FileScoreSource s = new FileScoreSource(args[0], false);
    //    //   s.plotGraphs();
    //    //   System.exit(0);
    //    // }
    //    // else {
    //    //   System.out.printf("Assuming \"%s\" is the name of an Excel file\n", args[0]);
    //    //   sSource = new FileScoreSource(args[0], true);
    //    // }

    readRunResultsFromExcelFile();

    // if (doFindNewScores) {
    //   findNewScores();
    // }
    // 
    // if (false) {
    //   for (Application app : apps.values()) {
    //     System.out.println(""+app);
    //   }
    // }
  }

  public void addApp(Application app)
  {
    if (app instanceof MosaicApp) {
      MosaicApp m = (MosaicApp)app;
      apps.put(m.getName(), m);
    }
    else {
      assert (false);
    }
  }
// 
//   //public static class MosaicApp implements App
//   //{
//   //  public String name = "";
//   //  public Architecture arch = null;
//   //  // columns in the tuning knobs experiments spreadsheet where data goes
//   //  public int objectiveCol = -1, failureModeCol = -1, expSettingsCol = -1, scriptNotesCol = -1;
//   //  public int failFactorCol = -1, failModeCol = -1, iterCountCol = -1, initIntervalCol = -1;
//   //  public Map<Point, Double> scores = null, failureFactors = null, estimates = null, newEstimates = null;
//   //  public Map<Point, String> expSettingsFiles = null;
//   //  public Map<Point, Integer> rowIdx;
//   //  public Map<String, String> globalSettings;
//   //  public KnobSettingSpace space = null;
//   //  public int sheetIdx;
//   //  public int lastRowIdx;
//   //  boolean maximizeObjective;
//   //  Sensor objectiveSensor;
//   //  Map<String, Sensor> sensorTable;
//   //
//   //  public MosaicApp(String n, Map<String, String> s, Architecture a)
//   //  {
//   //    name = n;
//   //    globalSettings = s;
//   //    arch = a;
//   //    space = new KnobSettingSpace(0);
//   //    scores = new HashMap<Point, Double>();
//   //    failureFactors = new HashMap<Point, Double>();
//   //    estimates = new HashMap<Point, Double>();
//   //    newEstimates = new HashMap<Point, Double>();
//   //    rowIdx = new HashMap<Point, Integer>();
//   //    expSettingsFiles = new HashMap<Point, String>();
//   //    sensorTable = new HashMap<String, Sensor>();
//   //  }
//   //
//   //  public String getName() { return name; }
//   //  public String getFullName() { return name + "_" + arch.getName(); }
//   //  public boolean getMaximizeObjective() { return maximizeObjective; }
//   //  public void setMaximizeObjective(boolean m) { maximizeObjective = m; }
//   //  public Sensor getObjectiveSensor() { return objectiveSensor; }
//   //  public void setObjectiveSensor(Sensor s) { objectiveSensor = s; }
//   //  public Sensor lookupSensor(String name) { return sensorTable.get(name); }
//   //  public void addSensor(String name, Sensor s) { sensorTable.put(name, s); }
//   //  public Collection<Sensor> getSensors() { return sensorTable.values(); }
//   //
//   //  public boolean equals(Object o)
//   //  {
//   //    if (o == null)
//   //      return false;
//   //    if (o instanceof MosaicApp) {
//   //      MosaicApp other = (MosaicApp)o;
//   //      return name.equals(other.name) && arch.equals(other.arch);
//   //    }
//   //    else { return false; }
//   //  }
//   //
//   //  // if you re-implement equals you have to re-implement hashCode to get hash-
//   //  // based data structures to work correctly.
//   //  public int hashCode()
//   //  { return name.hashCode() * 31 + arch.hashCode(); }
//   //
//   //  public String toString()
//   //  {
//   //    StringBuffer b = new StringBuffer();
//   //    b.append(String.format("Application: %s  Cluster Name: %s  Number of Clusters: %d\n  knobs:\n",
//   //      name, arch.clusterName, arch.numberOfClusters));
//   //    for (TuningKnob k : space) {
//   //      b.append(String.format("    %s\n", k));
//   //    }
//   //    b.append("  Estimates ("+estimates.size()+"):\n");
//   //    for (Map.Entry<Point, Double> e : estimates.entrySet()) {
//   //      b.append(String.format("    %f (%s)\n", e.getValue(), e.getKey()));
//   //    }
//   //    b.append("  Scores ("+scores.size()+"):\n");
//   //    for (Map.Entry<Point, Double> e : scores.entrySet()) {
//   //      b.append(String.format("    %f (%s)\n", e.getValue(), e.getKey()));
//   //    }
//   //    b.append("  FailureFactors ("+failureFactors.size()+"):\n");
//   //    for (Map.Entry<Point, Double> e : failureFactors.entrySet()) {
//   //      b.append(String.format("    %f (%s)\n", e.getValue(), e.getKey()));
//   //    }
//   //    return b.toString();
//   //  }
//   //}
//   //
//  
//  
//  /*
//   * Raw "sensor readings" that we might care about:
//   * pre-optimization number of vertices
//   * "                        " nodes
//   * "                        " nets
//   * "                        " edges
//   * pre-SPR number of ops
//   * pre-SPR number of nets
//   * pre-SPR number of array ops
//   * ...
//   * initial II
//   * final II
//   * iteration count
//   */

  /*
   * findNewScores should go through all the applications, find rows (a.k.a.
   * points) that don't have scores filled in, and try to get scores from actual
   * application runs.  It works by invoking a bunch of script fu.
   */
  // private void findNewSensorAndFailureReadings()
  // {
  //   for (Map.Entry<String, MosaicApp> appEntry : apps.entrySet()) {
  //     MosaicApp app = appEntry.getValue();
  //     for (Point pt : app.getAllPoints()) {
  //       if (pt.status == Point.Status.testing) {
  //         findNewSensorAndFailureReading(app, pt);
  //       }
  //     }
  //   }
  // }

  public void findNewSensorAndFailureReading(MosaicApp app, Point pt, String expFilename)
  {
    AppTemplate template = app.getTemplate();
    Architecture arch = app.getArchitecture();

    systemLock.lock();

    // File collectScript = new File(new File (new File(expDir, "scripts"), "common"),
    // "collectTuningKnobResults.pl");
    // File collectScript = new File(new File(expDir, "scripts"), "dumb.sh");
    File collectScript = new File(new File("scripts"), "dumb.sh");

    assert (expFilesDir != null);
    assert (expFilename != null);

    File expFile = new File(expFilesDir, expFilename);
    // String scriptCmd = collectScript.getPath() + " " + expFile.getPath();
    String cmdArg[] = new String[]{collectScript.getPath(), expFile.getPath()};
    if (false) { System.out.printf("Executing  %s\n", Arrays.toString(cmdArg)); }
    int collectExitCode = MiscUtils.execAndWait(cmdArg, null, null, execLogStream);
    // System.out.printf("Return code: %d\n", cmdLineScriptRet);
    String strippedFilename = expFilename.lastIndexOf(".") < 0 ? expFilename :
      expFilename.substring(0, expFilename.lastIndexOf("."));
    String resultsFilename = "tk_" + strippedFilename + ".txt";
    File resultsFilePath = new File("RunTkSpr", resultsFilename);
    BufferedReaderIterable resultsFile = null;
    try { resultsFile = new BufferedReaderIterable(new FileReader(resultsFilePath)); }
    catch (FileNotFoundException e) {
      l.severe("Unable to open results file '"+resultsFilename+"'");
      // File fs[] = newCWD.listFiles();
      // for (File f : fs) {
      //   System.out.printf("? %s ?   ", f.getName());
      // }
      throw new IllegalArgumentException();
    }
    
    assert (resultsFile != null);

    CSVReader results = new CSVReader(props, resultsFile);
    Map<Sensor, Integer>  sensorColumnIdxs  = new TreeMap<Sensor, Integer>();
    Map<Failure, Integer> failureColumnIdxs = new TreeMap<Failure, Integer>();
    for (Feature f : template.getFeatures()) {
      if (f instanceof Sensor) {
        Sensor s = (Sensor)f;
        Integer colIdx = results.getHeadingColumnIdx(s.getKey());
        if (colIdx == null) {
          l.severe("Missing results column for sensor "+s);
          throw new IllegalArgumentException();
        }
        sensorColumnIdxs.put(s, colIdx);
      }
      if (f instanceof Failure) {
        Failure fa = (Failure)f;
        Integer colIdx = results.getHeadingColumnIdx(fa.getKey());
        if (colIdx == null) {
          l.severe("Missing results column for "+fa);
          throw new IllegalArgumentException();
        }
        failureColumnIdxs.put(fa, colIdx);
      }
    }
    Map<String, Integer> noteColumnIdxs = new HashMap<String, Integer>();
    for (String noteKey : app.getNoteKeys()) {
      Integer colIdx = results.getHeadingColumnIdx(noteKey);
      if (colIdx == null) {
        l.warning("Can't find note key "+noteKey);
      }
      else {
        noteColumnIdxs.put(noteKey, colIdx);
      }
    }
    int macahSettingsColumnIdx  = results.getHeadingColumnIdx("macahSettings");
    int appColumnIdx            = results.getHeadingColumnIdx("app");
    int archColumnIdx           = results.getHeadingColumnIdx("arch");
    int numPesColumnIdx         = results.getHeadingColumnIdx("FINAL NUM PES");
    int clustNameColumnIdx      = results.getHeadingColumnIdx("Cluster Name");



    // protected Map<TuningKnob, Integer>    knobColumnMap           = null;
    // protected Map<String, Integer>        notesCols               = null;
    // public Integer statusCol = null;
    // public Integer expSettingsCol = null;
    // public Integer objectiveCol = null;



    Integer ptRowIdx = null;
    for (int rowIdx = 0; rowIdx < results.getNumberOfRows(); rowIdx++) {
      String appName = results.getCell(rowIdx, appColumnIdx);
      if (!template.getName().equals(appName)) {
        continue;
      }

      int numPEs = -1;
      try { numPEs = Integer.parseInt(results.getCell(rowIdx, numPesColumnIdx)); }
      catch (NumberFormatException e) {
        l.severe("Can't parse number of PEs "+results.getCell(rowIdx, numPesColumnIdx));
        throw new IllegalArgumentException();
      }
      if (numPEs != arch.numberOfClusters) {
        continue;
      }

      if (true) {
        String clusterName = results.getCell(rowIdx, clustNameColumnIdx);
        if (!arch.clusterName.equals(clusterName)) {
          if (true) { System.out.printf("arch shit %s %s\n", arch.clusterName, clusterName); }
          continue;
        }
      }

      String macahSettingsRaw = results.getCell(rowIdx, macahSettingsColumnIdx);
      String macahSettingsArr[] = macahSettingsRaw.split(":");
      Map<String, String> macahSettings = new HashMap<String, String>();
      for (String setting : macahSettingsArr) {
        String splitSetting[] = setting.split("=");
        if (splitSetting.length != 2) {
          l.severe("bad setting "+setting);
          throw new IllegalArgumentException();
        }
        macahSettings.put(splitSetting[0].trim(), splitSetting[1].trim());
      }

      Point resultsPt = new Point(template.getKnobSpace());
      // Try to find settings for each of the knobs
      for (TuningKnob knob : template.getKnobSpace()) {
        String settingStr = macahSettings.remove(knob.getName());
        Object setting = knob.stringToValue(settingStr);
        resultsPt.add(setting);
        if (false) { System.out.printf("'%s' '%s' '%s' ", knob.getName(), settingStr, setting); }
      }
      if (false) { System.out.printf("points: %s %s\n", pt, resultsPt); }
      if (pt.compareTo(resultsPt) != 0) {
        continue;
      }
      // now that we've removed the tuning knobs, check the settings
      if (!macahSettings.entrySet().equals(template.getSettings())) {
        l.warning("Settings differ");
        for (Map.Entry<String, String> entry : macahSettings.entrySet()) {
          System.out.printf("Hello1 %s\n", entry);
        }
        for (Map.Entry<String, String> entry : template.getSettings()) {
          System.out.printf("Hello2 %s\n", entry);
        }
        continue;
      }
      ptRowIdx = rowIdx;
      break;
    }

    if (ptRowIdx != null) {
      // Point "pt" has not been inserted into the spreadsheet yet
      
      for (Map.Entry<Sensor, Integer> entry : sensorColumnIdxs.entrySet()) {
        Sensor s = entry.getKey();
        int idx = entry.getValue();
        String textValue = results.getCell(ptRowIdx, idx).trim();
        if (false) { System.out.printf("  sensor: %s[%d]  text=\"%s\"", s.getName(), idx, textValue); }
        Object value = null;
        try {
          double numberValue = Double.parseDouble(textValue);
          if (false) { System.out.printf("  double=\"%f\"\n", numberValue); }
          app.setSensorReading(pt, s, numberValue);
          value = numberValue;
        }
        catch (NumberFormatException e) {
          if (false) { System.out.printf("  NaN\n"); }
          value = "No Info";
        }
      }
      for (Map.Entry<Failure, Integer> entry : failureColumnIdxs.entrySet()) {
        Failure f = entry.getKey();
        int idx = entry.getValue();
        String textValue = results.getCell(ptRowIdx, idx).trim().toUpperCase();
        Boolean value = null;
        if (false) { System.out.printf("Failure result pt(%s), fa(%s), rs(%s)\n", pt, f, textValue); }
        if (textValue.equals("FAILED")) {
          value = true;
        }
        else if (textValue.equals("PASSED")) {
          value = false;
        }
        else if (textValue.equals("UNKNOWN")) {
          value = null;
        }
        else {
          l.severe("Failed to determine failure "+f+" "+textValue);
          throw new IllegalArgumentException();
        }
        app.setFailureCause(pt, f, value);
      }
      for (Map.Entry<String, Integer> entry : noteColumnIdxs.entrySet()) {
        String key = entry.getKey();
        int idx = entry.getValue();
        String textValue = results.getCell(ptRowIdx, idx).trim();
        app.setNote(pt, key, textValue);
      }

      // Fill in values for computed features
      for (Feature f : template.getFeatures()) {
        if (f instanceof Algebraic) {
          Object value = f.getValue(app, pt);
          // cache it, I guess
        }
      }
    }
    else {
      l.warning("Not able to find results for point "+pt);
    }

    // It might be unreasonable to dump an updated spreadsheet every time we get
    // a new point.  We'll see.
    writeAllAppsToNewExcelFile();
     //    String columnSpec = null;
     //    // indexes into the CSV file produced by the collection script
     //    int execStatusIdx = -1, fsimStatusIdx = -1;
     //    int sprSuccIdx = -1, sprRunIdx = -1, sprFailIdx = -1, sprResultIdx = -1, sprTimedOutIdx = -1;
     //    int maxColIdx = -1;
     //    
     //    for (String line : resultsFile) {
     //      // System.out.printf("\nWHYWHY\n%s\n%s\n\n\n", resultsFilePath, line);
     //      if (columnSpec == null) {
     //        columnSpec = new String(line);
     //        // System.out.printf("Got column spec '%s'\n", columnSpec);
     //
     //        String columns[] = columnSpec.split(",");
     //        int colIdx = 0;
     //        for (String column : columns) {
     //          // System.out.printf("column %d = %s\n", colIdx, column);
     //          String c = column.trim().toUpperCase();
     //
     //          if      (c.equals("EXEC_STATUS"))   { execStatusIdx   = colIdx; }
     //          else if (c.equals("FSIM_STATUS"))   { fsimStatusIdx   = colIdx; }
     //          else if (c.equals("SPRSUCCESS"))    { sprSuccIdx      = colIdx; }
     //          else if (c.equals("SPRRUNNING"))    { sprRunIdx       = colIdx; }
     //          else if (c.equals("SPRFAILURE"))    { sprFailIdx      = colIdx; }
     //          else if (c.equals("SPR_TIMEDOUT"))  { sprTimedOutIdx  = colIdx; }
     //          else if (c.equals("SPRRESULT"))     { sprResultIdx    = colIdx; }
     //
     //          colIdx++;
     //        }
     //      }
     //      else {
     //        String columns[] = line.split(",");
     //        int colIdx = 0;
     //
     //        // build up the full app name for this result
     //        else {
     //          if (columns[execStatusIdx].trim().equals("+")) {
     //            // now we have an app and a point, see if we were waiting for this one
     //            for (PointAppPair pointAndApp : pointsOfInterest) {
     //              MosaicApp newApp = pointAndApp.a;
     //              Point newPt = pointAndApp.p;
     //              if (newApp.getFullName().equals(app.getFullName()) &&
     //                  newPt.equals(pt)) {
     //                boolean sprTimedOut = columns[sprTimedOutIdx].trim().toUpperCase().equals("YES");
     //
     //                // sprRouterCongestionFailure
     //                boolean fsimSucc = columns[fsimStatusIdx].trim().toUpperCase().equals("SUCCESS");
     //                boolean sprSucc = columns[sprSuccIdx].trim().toUpperCase().equals("YES");
     //                boolean sprFail = columns[sprFailIdx].trim().toUpperCase().equals("YES");
     //                boolean sprRun  = columns[sprRunIdx].trim().toUpperCase().equals("YES");
     //                
     //                System.out.printf("  Happy? %b   timed out? %b\n", sprSucc, sprTimedOut);
     //                if (sprTimedOut) {
     //                  newApp.setStatus(newPt, MosaicApp.STAT_SPR_TIMED_OUT);
     //                }
     //                if (sprSucc) {
     //                  if (fsimSucc) {
     //                    newApp.setStatus(newPt, MosaicApp.STAT_SUCCESS);
     //                  }
     //                  else {
     //                    // FIXME: do something here
     //                  }
     //                }
     //                else if (sprFail) {
     //                  String sprResult = columns[sprResultIdx].trim();
     //                  Integer status = newApp.lookupStatus(sprResult);
     //                  if (status != null) {
     //                    newApp.setStatus(newPt, status);
     //                  }
     //                //   // At some point, try to understand the failure better
     //                //   // int , plcmtFailIdx = -1, routeFailIdx = -1;
     //                //   newApp.scores.put(newPt, 0.0);
     //                //   // FIXME: The next line is important
     //                //   // setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scoreCol, 0.0);
     //                //   setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "SPR failed");
     //                }
     //                else if (sprRun) {
     //                  // okay, it's still running, whatever
     //                  setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "SPR running (maybe)");
     //                }
     //                else {
     //                  // System.out.printf("TK_WARNING: SPR in strange state.  app (%s) point (%s)\n",
     //                  //   newApp.getFullName(), newPt);
     //                  setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "SPR weird state");
     //                }
     //                if (newApp.getStatus(newPt) > 3) {
     //                  setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.statusCol,
     //                    MosaicApp.statusToString(newApp.getStatus(newPt)));
     //                }
     //              }
     //            }
     //          }
     //          else if (columns[execStatusIdx].trim().equals("?")) {
     //            // The scripts seem to have barfed in some way.  Assume 0 and issue a warning
     //            for (PointAppPair pointAndApp : pointsOfInterest) {
     //              MosaicApp newApp = pointAndApp.a;
     //              Point newPt = pointAndApp.p;
     //              if (newApp.getFullName().equals(app.getFullName()) &&
     //                  newPt.equals(pt)) {
     //                System.out.printf("TK_WARNING: \"success\"=\"?\"  app(%s) point(%s)\n",
     //                      newApp.getFullName(), newPt);
     //                setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "SPR not run?");
     //              }
     //            }
     //          }
     //        }
     //      }
     //    }
     //  }
     //  // System.setProperty("user.dir", oldCWD);
    systemLock.unlock();
  }

  void setValueInCell(HSSFCell c, Object value)
  {
    // if (false) {System.out.printf("Putting value in workbook: [%d, %d] val:%s\n", rowIdx, colIdx, value);}
    assert (c != null);
    if (value == null) {
      c.setCellType(HSSFCell.CELL_TYPE_STRING);
      c.setCellValue(new HSSFRichTextString("null"));
    }
    else {
      if (value instanceof Double) {
        c.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
        c.setCellValue(((Double)value));
      }
      else if (value instanceof String) {
        c.setCellType(HSSFCell.CELL_TYPE_STRING);
        c.setCellValue(new HSSFRichTextString((String)value));
      }
      else {
        c.setCellType(HSSFCell.CELL_TYPE_STRING);
        c.setCellValue(new HSSFRichTextString("?type? "+value.toString()));
      }
    }
  }

  void setValueInWorkbook(HSSFRow row, int colIdx, Object value)
  {
    HSSFCell c = row.getCell(colIdx);
    if (c == null) {
      c = row.createCell(colIdx);
    }
    setValueInCell(c, value);
  }

  void setValueInWorkbook(int rowIdx, int colIdx, Object value)
  {
    if (false) {System.out.printf("Putting value in workbook: [%d, %d] val:%s\n", rowIdx, colIdx, value);}
    HSSFSheet sheet = wb.getSheetAt(0);
    HSSFRow row = sheet.getRow(rowIdx);
    setValueInWorkbook(row, colIdx, value);
  }

  Double getValAsDouble(HSSFCell cell, HSSFFormulaEvaluator evaluator)
  {
    int cellType = cell.getCellType();
    if (cellType == HSSFCell.CELL_TYPE_NUMERIC) {
      return cell.getNumericCellValue();
    }
    else if (cellType == HSSFCell.CELL_TYPE_FORMULA) {
      HSSFFormulaEvaluator.CellValue cellVal = evaluator.evaluate(cell);
      if (cellVal.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
        return cellVal.getNumberValue();
      }
      else { return null; }
    }
    else { return null; }
  }
  
  String getValAsString(HSSFCell cell, HSSFFormulaEvaluator evaluator)
  {
    int cellType = cell.getCellType();
    if (cellType == HSSFCell.CELL_TYPE_STRING) {
      return cell.getRichStringCellValue().getString();
    }
    else if (cellType == HSSFCell.CELL_TYPE_FORMULA) {
      HSSFFormulaEvaluator.CellValue cellVal = evaluator.evaluate(cell);
      if (cellVal.getCellType() == HSSFCell.CELL_TYPE_STRING) {
        return cellVal.getStringValue();
      }
      else { return null; }
    }
    else { return null; }
  }

  /*
   * Precondition: systemLock held.
   */
  protected Set<String> lockRunDirs (File expFile, String[] stages)
  {
    List<String> runDirsList = new LinkedList<String>();
    String cmd[] = new String[stages.length + 2];
    cmd[0] = listRunDirsScript.getPath();
    cmd[1] = expFile.getPath();
    int idx = 2;
    for (String stage : stages) {
      cmd[idx++] = stage;
    }
    int runDirsExitCode = MiscUtils.readLinesFromCommand(cmd, runDirsList);
    assert (runDirsExitCode == 0);
    Set <String> runDirs = new HashSet<String>();
    for (String runDir : runDirsList) {
      if (runDir != null) {
        if (!runDir.trim().equals("")) {
          runDirs.add(runDir.trim());
        }
      }
    }
    while (true) {
      boolean noneRunning = true;
      for (String runDir : runDirs) {
        if (directoriesWhereJobsAreRunning.contains(runDir)) {
          noneRunning = false;
          break;
        }
      }
      if (noneRunning) { break; }
      systemLock.unlock();
      try { Thread.sleep(props.submitSleepTimeMillis); }
      catch (InterruptedException e) { }
      systemLock.lock();
    }

    for (String runDir : runDirs) {
      assert (!directoriesWhereJobsAreRunning.contains(runDir));
      directoriesWhereJobsAreRunning.add(runDir);
    }

    return runDirs;
  }

  protected void unlockRunDirs (Set<String> runDirs)
  {
    for (String runDir : runDirs) {
      assert (directoriesWhereJobsAreRunning.contains(runDir));
      directoriesWhereJobsAreRunning.remove(runDir);
    }
    if (false) {
      System.out.printf("Locked run directories:\n");
      for (String dir : directoriesWhereJobsAreRunning) {
        System.out.printf("  %s\n", dir);
      }
    }
  }

  protected void waitForJobs (File expFile, String[] stages)
  {
    while (true) {
      int allJobsCompleteScriptExitCode = -1;
      String cmd[] = new String[stages.length + 2];
      cmd[0] = allJobsCompleteScript.getPath();
      cmd[1] = expFile.getPath();
      int idx = 2;
      for (String stage : stages) {
        cmd[idx++] = stage;
      }
      allJobsCompleteScriptExitCode = MiscUtils.execAndWait(cmd, null, null, execLogStream);
      if (allJobsCompleteScriptExitCode == 0) { break; }
      else if (allJobsCompleteScriptExitCode == 2) {
        System.out.printf("Waiting for %s jobs from %s.  Sleep %fs\n",
          Arrays.toString(stages), expFile, props.submitSleepTimeMillis/1000.0);
        systemLock.unlock();
        try { Thread.sleep(props.submitSleepTimeMillis); }
        catch (InterruptedException e) { }
        systemLock.lock();
      }
      else {
        l.severe(Arrays.toString(cmd)+" returned exit code "+allJobsCompleteScriptExitCode);
        System.exit(1);
      }
    }
  }

  public void writeNewEstimates()
  {
    //  // phase 1: build up a big map from archs to maps from apps to sets of points
    //  // phase 2: partition the big map to restrict the number of jobs per
    //  //          settings file
    //  // phase 3: update spreadsheet and dump out settings files
    //
    //  // build up the big map with no partitioning
    //  Map<Architecture, Map<MosaicApp, Set<Point>>> rawPointsPerArch = 
    //      new HashMap<Architecture, Map<MosaicApp, Set<Point>>>();
    //  for (Map.Entry<String, Application> appEntry : apps.entrySet()) {
    //    MosaicApp app = (MosaicApp)appEntry.getValue();
    //    System.out.printf("Adding new ests. app=%s |pts|=%d\n", app.getFullName(), app.newEstimates.size());
    //    for (Map.Entry<Point, Double> estEntry : app.newEstimates.entrySet()) {
    //      Point pt = estEntry.getKey();
    //      double est = estEntry.getValue();
    //
    //      // add this point to the appropriate set to be put in a new expSettings
    //      Map<MosaicApp, Set<Point>> ptsPerApp = rawPointsPerArch.get(app.arch);
    //      if (ptsPerApp == null) {
    //        ptsPerApp = new HashMap<MosaicApp, Set<Point>>();
    //        rawPointsPerArch.put(app.arch, ptsPerApp);
    //      }
    //      Set<Point> pts = ptsPerApp.get(app);
    //      if (pts == null) {
    //        pts = new HashSet<Point>();
    //        ptsPerApp.put(app, pts);
    //      }
    //      pts.add(pt);
    //    }
    //  }
    //
    //  // now partition the map into bite-size chunks
    //  int biteSize = 100;
    //  long timeStamp = System.currentTimeMillis();
    //  // This rather gnarly type can be read like so:
    //  // "mapping from architectures to sets of `point collections', where each
    //  // `point collection' is a pair of expSettings file name and mapping from
    //  // apps to points"
    //  Map<Architecture, Set<Pair<String,ComparableMap<MosaicApp, Set<Point>>>>> chunkedPointsPerArch =
    //    new HashMap<Architecture, Set<Pair<String,ComparableMap<MosaicApp, Set<Point>>>>>();
    //  for (Map.Entry<Architecture, Map<MosaicApp,Set<Point>>> archEntry : rawPointsPerArch.entrySet()) {
    //    Architecture arch = archEntry.getKey();
    //    Map<MosaicApp, Set<Point>> ptsPerApp = archEntry.getValue();
    //    
    //    int totalNumPoints = 0;
    //    for (Set<Point> points : ptsPerApp.values()) {
    //      totalNumPoints += points.size();
    //    }
    //    if (totalNumPoints > 0) {
    //      int numSets = ((totalNumPoints - 1) / biteSize) + 1;
    //      int setNumber = 1;
    //      
    //      Set<Pair<String, ComparableMap<MosaicApp, Set<Point>>>> ptsPerAppSet =
    //        new HashSet<Pair<String, ComparableMap<MosaicApp, Set<Point>>>>();
    //      chunkedPointsPerArch.put(arch, ptsPerAppSet);
    //
    //      Iterator<Map.Entry<MosaicApp,Set<Point>>> ptsPerAppIter = ptsPerApp.entrySet().iterator();
    //      Map.Entry<MosaicApp,Set<Point>> ptsPerAppCurr = null;
    //      ComparableMap<MosaicApp,Set<Point>> ptsPerAppTarg = new ComparableHashMap<MosaicApp, Set<Point>>();
    //      String expSettingsFilename = String.format("expSet_TKAuto_%s_%d_%dof%d.pl",
    //        arch.getName(), timeStamp, setNumber, numSets);
    //      // expSettingsFilenames.put(app.arch, expSettingsFilename);
    //      ptsPerAppSet.add(Tuple.from(expSettingsFilename, ptsPerAppTarg));
    //
    //      if (ptsPerAppIter.hasNext()) {
    //        ptsPerAppCurr = ptsPerAppIter.next();
    //      }
    //      else { break; }
    //
    //      MosaicApp appCurr = ptsPerAppCurr.getKey();
    //      Iterator<Point> pointsCurrIter = ptsPerAppCurr.getValue().iterator();
    //      Set<Point> pointsTarg = new HashSet<Point>();
    //      ptsPerAppTarg.put(appCurr, pointsTarg);
    //
    //      while (true) {
    //        int i = 0;
    //        while (i < biteSize) {
    //          if (pointsCurrIter.hasNext()) {
    //            Point p = pointsCurrIter.next();
    //            pointsTarg.add(p);
    //            i++;
    //          }
    //          else { // no more points in the current app
    //            if (ptsPerAppIter.hasNext()) {
    //              ptsPerAppCurr = ptsPerAppIter.next();
    //              appCurr = ptsPerAppCurr.getKey();
    //              pointsCurrIter = ptsPerAppCurr.getValue().iterator();
    //              pointsTarg = new HashSet<Point>();
    //              ptsPerAppTarg.put(appCurr, pointsTarg);
    //            }
    //            else { // no more applications left
    //              break;
    //            }
    //          }
    //        }
    //        if ((!pointsCurrIter.hasNext()) && (!ptsPerAppIter.hasNext())) { break; }
    //        else {
    //          ptsPerAppTarg = new ComparableHashMap<MosaicApp, Set<Point>>();
    //          setNumber++;
    //          expSettingsFilename = String.format("expSet_TKAuto_%s_%d_%dof%d.pl",
    //            arch.getName(), timeStamp, setNumber, numSets);
    //          ptsPerAppSet.add(Tuple.from(expSettingsFilename, ptsPerAppTarg));
    //          pointsTarg = new HashSet<Point>();
    //          ptsPerAppTarg.put(appCurr, pointsTarg);
    //        }
    //      }
    //    }
    //  }
    //
    //  
    //  HSSFSheet sheet = wb.getSheetAt(0);
    //  for (Map.Entry<Architecture, Set<Pair<String,ComparableMap<MosaicApp, Set<Point>>>>> archEntry :
    //          chunkedPointsPerArch.entrySet()) {
    //    Architecture arch = archEntry.getKey();
    //
    //    for (Pair<String,ComparableMap<MosaicApp, Set<Point>>> chunk : archEntry.getValue()) {
    //      String settingsFilename = Tuple.get1(chunk);
    //      Map<MosaicApp, Set<Point>> ptsPerApp = Tuple.get2(chunk);
    //
    //      // update the spreadsheet in memory
    //      for (Map.Entry<MosaicApp, Set<Point>> appEntry : ptsPerApp.entrySet()) {
    //        MosaicApp app = appEntry.getKey();
    //        for (Point pt : appEntry.getValue()) {
    //          double est = app.newEstimates.get(pt);
    //  REMOVED STUFF FOR ADDING ROWS TO THE SPREADSHEET
    //        }
    //      }
    //
    //      // dump a settings file
    //      File templateFilePath = new File(expDir, props.expSettingsTemplateFilename);
    //      BufferedReaderIterable templateFile = null;
    //      try {
    //        templateFile = new BufferedReaderIterable(new FileReader(templateFilePath));
    //      }
    //      catch (FileNotFoundException e) {
    //        System.err.printf("Unable to open template file '%s'\n", props.expSettingsTemplateFilename);
    //        System.exit(1);
    //      }
    //
    //      File expFilePath = new File(expFilesDir, settingsFilename);
    //      PrintWriter expFile = null;
    //      try {
    //        expFile = new PrintWriter(expFilePath);
    //      }
    //      catch (FileNotFoundException e) {
    //        System.err.printf("Unable to open exp settings file '%s'\n", settingsFilename);
    //        System.exit(1);
    //      }
    //
    //      System.out.printf("Writing settings file %s\n", settingsFilename);
    //
    //      for (String templateLine : templateFile) {
    //        int appNamesIdx     = templateLine.indexOf("<APPLICATION NAMES>");
    //        int appSetsIdx      = templateLine.indexOf("<APPLICATION SETTINGS>");
    //        int appSweepsIdx    = templateLine.indexOf("<APPLICATION SWEEPS>");
    //        if (appNamesIdx != -1) {
    //          // our @appList     = (<APPLICATION NAMES>);
    //          StringBuffer apps = new StringBuffer("");
    //          for (Application app : ptsPerApp.keySet()) {
    //            apps.append("\"" + app.getName() + "\", ");
    //          }
    //          String outStr = templateLine.replaceAll("<APPLICATION NAMES>", apps.toString());
    //          expFile.printf("%s\n", outStr);
    //        }
    //        else if (appSetsIdx != -1) {
    //          // our %appSettings = ( 
    //          // <APPLICATION SETTINGS>
    //          // );
    //          for (Application app : ptsPerApp.keySet()) {
    //            expFile.printf("  \"%s\"=> {},\n", app.getName());
    //          }
    //        }
    //        else if (appSweepsIdx != -1) {
    //          // our %appSweeps = ( 
    //          // <APPLICATION SWEEPS>
    //          // );
    //          for (Map.Entry<MosaicApp,Set<Point>> ptsAppEntry : ptsPerApp.entrySet()) {
    //            MosaicApp app = ptsAppEntry.getKey();
    //            Set<Point> pts = ptsAppEntry.getValue();
    //
    //            expFile.printf("  \"%s\"=> {\n", app.getName());
    //            for (Map.Entry<String, String> settingEntry : app.globalSettings.entrySet()) {
    //              String settingName = settingEntry.getKey();
    //              String settingValue = settingEntry.getValue();
    //              expFile.printf("    \"%s\"=>[", settingName);
    //              for (Point pt : pts) { expFile.printf("\"%s\", ", settingValue); }
    //              expFile.printf("],\n");
    //            }
    //            int dimIdx = 0;
    //            for (TuningKnob knob : app.space) {
    //              expFile.printf("    \"%s\"=>[", knob.getName());
    //              for (Point pt : pts) {
    //                String setting = knob.valueToString(pt.get(dimIdx));
    //                expFile.printf("\"%s\", ", setting);
    //              }
    //              expFile.printf("],\n");
    //              dimIdx++;
    //            }
    //            expFile.printf("  },\n");
    //          }
    //        }
    //        else {
    //          expFile.printf("%s\n", templateLine);
    //        }
    //      }
    //
    //      expFile.close();
    //
    //      // makeSettingsFile( archEntry.getKey(), archEntry.getValue(),
    //      //                   expSettingsFilenames.get(archEntry.getKey()));
    //
    //    }
    //  }
    //
    //  // dump the updated spreadsheet
    //  try {
    //    FileOutputStream fileOut = new FileOutputStream(excelFilename);
    //    // HSSFSheet sheet = wb.getSheetAt(0);
    //    // HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
    //    wb.write(fileOut);
    //    fileOut.close();
    //  }
    //  catch (FileNotFoundException e) {
    //    System.err.printf("FNF when trying to write new estimates\n");
    //    System.exit(1);
    //  }
    //  catch (IOException e) {
    //    System.err.printf("IO when trying to write new estimates\n");
    //    System.exit(1);
    //  }
  }

  public void testPoint(MosaicApp app, String expSettingsFilename)
  {
    l.info ("MosaicSystemInterface.testPoint: Starting "+app.getName()+" "+expSettingsFilename+" "+Thread.currentThread());
    systemLock.lock();

    String cmd[] = null;
    File expFile = new File(expFilesDir, expSettingsFilename);
    {
      // File runJobsScript = new File(new File(new File(expDir, "scripts"), "common"), "runAllJobsToCompletion.pl");
      // String scriptCmd = collectScript.getPath() + " " + expFile.getPath();
      cmd = new String[]{ makeJobsScript.getPath(), expFile.getPath() };
      if (false) { System.out.printf("Executing  %s\n", Arrays.toString(cmd)); }
      int makeJobsScriptExitCode = MiscUtils.execAndWait(cmd, null, null, execLogStream);
      if (makeJobsScriptExitCode != 0) {
        l.severe("mkDAGjobs.pl "+expFile.getPath()+" returned exit code "+makeJobsScriptExitCode);
        throw new IllegalArgumentException();
      }
    }
    {
      Set<String> archRunDirs   = lockRunDirs(expFile, new String[]{"arch"});
      Set<String> macahRunDirs  = lockRunDirs(expFile, new String[]{"macah"});
      cmd = new String[]{ submitJobsScript.getPath(), expFile.getPath(), "arch", "macah" };
      if (false) { System.out.printf("Executing  %s\n", Arrays.toString(cmd)); }
      int submitJobsScriptExitCode = MiscUtils.execAndWait(cmd, null, null, execLogStream);
      if (submitJobsScriptExitCode != 0) {
        l.severe("submitSpecificKindsOfJobs.pl "+expFile.getPath()+" arch macah returned exit code "+submitJobsScriptExitCode);
        throw new IllegalArgumentException();
      }
      waitForJobs(expFile, new String[]{"arch"});
      unlockRunDirs(archRunDirs);
      waitForJobs(expFile, new String[]{"macah"});
      unlockRunDirs(macahRunDirs);
    }
    {
      Set<String> runDirs = lockRunDirs(expFile, new String[]{"spr", "fsim"});
      cmd = new String[]{ submitJobsScript.getPath(), expFile.getPath(), "spr", "fsim" };
      if (false) { System.out.printf("Executing  %s\n", Arrays.toString(cmd)); }
      int submitJobsScriptExitCode = MiscUtils.execAndWait(cmd, null, null, execLogStream);
      if (submitJobsScriptExitCode != 0) {
        l.severe("submit command bad exit code "+submitJobsScriptExitCode);
        throw new IllegalArgumentException();
      }
      while (true) {
        int allJobsCompleteScriptExitCode = -1;
        cmd = new String[]{ allJobsCompleteScript.getPath(), expFile.getPath(), "fsim" };
        if (false) { System.out.printf("Executing  %s\n", Arrays.toString(cmd)); }
        allJobsCompleteScriptExitCode = MiscUtils.execAndWait(cmd, null, null, execLogStream);
        if (allJobsCompleteScriptExitCode == 0) { break; }
        else if (allJobsCompleteScriptExitCode == 2) {
          System.out.printf("Waiting for fsim jobs from %s.  Sleep %fs\n", expFile, props.submitSleepTimeMillis/1000.0);
          systemLock.unlock();
          try { Thread.sleep(props.submitSleepTimeMillis); }
          catch (InterruptedException e) { }
          systemLock.lock();
        }
        else if (allJobsCompleteScriptExitCode == 1) {
          l.warning("Very weird.  FSim failed");
          break;
        }
        else {
          l.severe(Arrays.toString(cmd)+" exit code:"+allJobsCompleteScriptExitCode);
          System.exit(1);
        }
      }
      boolean printFirst = true;
      while (true) {
        int allJobsCompleteScriptExitCode = -1;
        cmd = new String[] { allJobsCompleteScript.getPath(), expFile.getPath(), "spr" };
        if (false) { System.out.printf("Executing  %s\n", Arrays.toString(cmd)); }
        allJobsCompleteScriptExitCode = MiscUtils.execAndWait(cmd, null, null, execLogStream);
        if (allJobsCompleteScriptExitCode == 0) { break; }
        else if (allJobsCompleteScriptExitCode == 2) {
          if (printFirst)
            { System.out.printf("Waiting for spr jobs from %s.  Sleep %fs\n", 
                expFile, props.submitSleepTimeMillis/1000.0);
              printFirst = false; }
          System.out.printf("... ");
          systemLock.unlock();
          try { Thread.sleep(props.submitSleepTimeMillis); }
          catch (InterruptedException e) { }
          systemLock.lock();
        }
        else if (allJobsCompleteScriptExitCode == 1) {
          l.info("SPR failed.  Not so surprising. "+expFile.getPath());
          break;
        }
        else {
          l.severe("Bad exit code "+allJobsCompleteScriptExitCode);
          System.exit(1);
        }
      }
      unlockRunDirs(runDirs);
    }
    
    systemLock.unlock();

    if (false) {
      while (true) {
        try { Thread.sleep(props.jobWaitSleepTimeMillis); System.out.printf("Still all jobs completed!! %s\n", expFile); }
        catch (InterruptedException e) { }
      }
    }
    l.info ("Finished testPoint "+app.getName()+" "+expSettingsFilename+" "+Thread.currentThread());
  }

  public String makeSettingsFile(MosaicApp app, Point pt)
  {
    long timeStamp = System.currentTimeMillis();
    AppTemplate appTemplate = app.getTemplate();
    Architecture arch = app.getArchitecture();
    String expSettingsFilename =
      String.format("expSet_TKAuto_%s_%s_%d.pl", app.getFullName(), pt.toFilenameString(), timeStamp);
    Logger l = Logger.getLogger(props.defaultLoggerName);
    // File templateFilePath = new File(expDir, props.expSettingsTemplateFilename);
    File templateFilePath = new File(props.expSettingsTemplateFilename);
    BufferedReaderIterable templateFile = null;
    try { templateFile = new BufferedReaderIterable(new FileReader(templateFilePath)); }
    catch (FileNotFoundException e) {
      l.severe("Unable to open template file \""+props.expSettingsTemplateFilename+"\"");
      throw new IllegalArgumentException();
    }

    File expFilePath = new File(expFilesDir, expSettingsFilename);
    PrintWriter expFile = null;
    try { expFile = new PrintWriter(expFilePath); }
    catch (FileNotFoundException e) {
      l.severe("Unable to open exp settings file \""+expFilePath+"\"");
      throw new IllegalArgumentException();
    }

    boolean foundAppNames = false, foundAppSettings = false, foundAppSweeps = false,
      foundNumberOfClusters = false, foundClusterName = false;
    for (String templateLine : templateFile) {
      int appNamesIdx     = templateLine.indexOf("<APPLICATION NAMES>");
      int appSetsIdx      = templateLine.indexOf("<APPLICATION SETTINGS>");
      int appSweepsIdx    = templateLine.indexOf("<APPLICATION SWEEPS>");
      int numClustersIdx  = templateLine.indexOf("<NUMBER OF CLUSTERS>");
      int clusterNameIdx  = templateLine.indexOf("<CLUSTER NAME>");
      if (appNamesIdx != -1) {
        // our @appList     = (<APPLICATION NAMES>);
        String outStr = templateLine.replaceAll("<APPLICATION NAMES>", "\""+appTemplate.getName()+"\"");
        expFile.printf("%s\n", outStr);
        foundAppNames = true;
      }
      else if (appSetsIdx != -1) {
        // our %appSettings = ( 
        // <APPLICATION SETTINGS>
        // );
        String outStr = templateLine.replaceAll("<APPLICATION SETTINGS>", "\""+appTemplate.getName()+"\" => ()");
        expFile.printf("%s\n", outStr);
        foundAppSettings = true;
      }
      else if (appSweepsIdx != -1) {
        // our %appSweeps = ( 
        // <APPLICATION SWEEPS>
        // );
        AppTemplate template = app.getTemplate();
        
        expFile.printf("  \"%s\"=> {\n", appTemplate.getName());
        for (Map.Entry<String, String> settingEntry : template.getSettings()) {
          String settingName = settingEntry.getKey();
          String settingValue = settingEntry.getValue();
          expFile.printf("    \"%s\"=>[\"%s\", ],\n", settingName, settingValue);
        }
        int dimIdx = 0;
        for (TuningKnob knob : template.getKnobSpace()) {
          expFile.printf("    \"%s\"=>[\"%s\", ],\n", knob.getName(), knob.valueToString(pt.get(dimIdx)));
          dimIdx++;
        }
        expFile.printf("  },\n");
        foundAppSweeps = true;
      }
      else if (numClustersIdx != -1) {
        String outStr = templateLine.replaceAll("<NUMBER OF CLUSTERS>", "\""+arch.numberOfClusters+"\"");
        expFile.printf("%s\n", outStr);
        foundNumberOfClusters = true;
      }
      else if (clusterNameIdx != -1) {
        String outStr = templateLine.replaceAll("<CLUSTER NAME>", arch.clusterName);
        expFile.printf("%s\n", outStr);
        for (int m = 0; m < arch.numberOfMems; m++) {
          expFile.printf("    cells:memory_r1w1s1,\n");
        }
        foundClusterName = true;
      }
      else {
        expFile.printf("%s\n", templateLine);
      }

    }
    expFile.close();
    assert (foundAppNames);
    assert (foundAppSettings);
    assert (foundAppSweeps);
    assert (foundNumberOfClusters);
    assert (foundClusterName);
    
    return expSettingsFilename;
  }

//  public void plotGraphs()
//  {
//    for (Map.Entry<String, Application> appEntry : apps.entrySet()) {
//      MosaicApp app = (MosaicApp)appEntry.getValue();
//      if (app.space.size() != 2) {
//        System.out.printf("plotGraphs can only handle apps with 2 knobs, not %d\n", app.space.size());
//      }
//      else {
//        TuningKnob k0 = app.space.get(0);
//        TuningKnob k1 = app.space.get(1);
//  
//        int numGoodValues = 0;
//        for (Point pt : app.getAllPoints()) {
//          // System.out.printf("point: %s\n", pt);
//          if (app.getObjectiveReading(pt) != null && app.wasSuccessful(pt)) {
//            // System.out.printf("score: %f\n", score);
//            numGoodValues++;
//          }
//        }
//        if (numGoodValues > 0) {
//          double plot[][] = new double[numGoodValues][4];
//  
//          int i = 0;
//          double max = 0.0;
//          for (Point pt : app.getAllPoints()) {
//            Double score = app.getObjectiveReading(pt);
//            if (score != null && app.wasSuccessful(pt)) {
//              // if (score < 10) {
//                Object c0 = pt.get(0);
//                Object c1 = pt.get(1);
//                plot[i][0] = k0.valueToCoordinate(c0);
//                plot[i][1] = k1.valueToCoordinate(c1);
//                plot[i][2] = score;
//                plot[i][3] = score;
//                max = Math.max(max, score);
//                i++;
//              // }
//            }
//          }
//  
//          String gnuplotpath = MiscUtils.readLineFromCommand("which gnuplot");
//          if (gnuplotpath == null) {
//            System.out.printf("Failed to get gnuplot path\n");
//            System.exit(1);
//          }
//  
//          JavaPlot p = new JavaPlot(gnuplotpath);
//          JavaPlot.getDebugger().setLevel(Debug.ERROR);
//          // JavaPlot.getDebugger().setLevel(Debug.VERBOSE);
//          // p.setTerminal(new PostscriptTerminal("foo.ps"));
//  
//          p.setTitle(app.getFullName());
//          p.setKey(JavaPlot.Key.TOP_RIGHT);
//          // p.newGraph();
//          p.newGraph3D();
//          p.getAxis("x").setLabel(""+k0.getName());
//          p.getAxis("y").setLabel(""+k1.getName());
//  
//          //// p.set("surface", "");
//          //// p.set("contour", "both");
//          //// p.set("cntrparam", "bspline");
//          //// p.set("hidden3d", "");
//  
//          p.getAxis("x").setBoundaries(k0.minCoordinateValue(), k0.maxCoordinateValue());
//          p.getAxis("y").setBoundaries(k1.minCoordinateValue(), k1.maxCoordinateValue());
//          p.getAxis("z").setBoundaries(0, max);
//          p.addPlot(plot);
//          p.getAxis("x").setBoundaries(k0.minCoordinateValue(), k0.maxCoordinateValue());
//          p.getAxis("y").setBoundaries(k1.minCoordinateValue(), k1.maxCoordinateValue());
//          p.getAxis("z").setBoundaries(0, max);
//  
//          PlotStyle stl2 = ((AbstractPlot) p.getPlots().get(0)).getPlotStyle();
//          // stl2.setStyle(Style.LINES);
//          stl2.setStyle(Style.POINTS);
//          stl2.set("linetype", "palette");
//          stl2.setPointType(4);
//  
//          // If terminal is not set to x11, it will open up a "AquaTerm" window on
//          // some OS-X boxes.
//          // p.set("mouse", "");
//          p.set("terminal", "x11");
//          // p.set("terminal", "pdf");
//          // p.setMultiTitle("All the pretty pictures");
//          // AutoGraphLayout lo = new AutoGraphLayout();
//          // lo.setColumns(2);
//          // lo.setRows(2);
//          // p.getPage().setLayout(lo);
//          p.plot();
//        }
//      }
//    }
//  }
// 

  public static void fooTest(String args[])
  {
    boolean totaljunk = false;
    if (totaljunk) {
      BufferedReaderIterable templateFile = null;
      try {
        int i = 0;
        templateFile = new BufferedReaderIterable(new FileReader(new File("/Users/ben8/.profile")));
        for (String line : templateFile) {
          System.out.printf("%3d: %s\n", i, line);
          i++;
        }
      }
      catch (FileNotFoundException e) {
        System.err.printf("Unable to open template file whatever\n");
        System.exit(1);
      }
      
    }
    else {
      try {
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(args[0]));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
        sheet.shiftRows(3,5,2);
    
        HSSFRow row = sheet.getRow(3);
        //HSSFRow row = sheet.createRow(5);
        HSSFCell cell = row.getCell(3);
        if (cell == null)
          cell = row.createCell(3);
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        cell.setCellValue(new HSSFRichTextString("a test"));
    
        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(args[0]);
        wb.write(fileOut);
        fileOut.close();
      }
      catch (FileNotFoundException e) {
        System.out.printf("FNF\n");
        System.exit(1);
      }
      catch (IOException e) {
        System.out.printf("IO\n");
        System.exit(1);
      }
    }
  }

  public void readRunResultsFromExcelFile()
  {
    try {
      File spreadsheetDir = new File (props.outputSpreadsheetDirname);
      String[] spreadsheets = spreadsheetDir.list();
      if (spreadsheets == null) {
        throw new FileNotFoundException("Missing spreadsheet directory "+props.outputSpreadsheetDirname);
      }

      String mostRecentFilename = null;
      for (String spreadsheetFileName : spreadsheets) {
        if (spreadsheetFileName.startsWith(props.allResultsFilenamePrefix) &&
            (mostRecentFilename == null || (spreadsheetFileName.compareTo(mostRecentFilename) > 0))) {
          mostRecentFilename = spreadsheetFileName;
        }
      }
      if (mostRecentFilename == null) {
        throw new FileNotFoundException("Can't find any "+props.allResultsFilenamePrefix+"* files");
      }

      POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(new File(spreadsheetDir, mostRecentFilename)));
      wb = new HSSFWorkbook(fs);
      HSSFSheet sheet = wb.getSheetAt(0);
      HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
  
      Map<TuningKnob, Integer> knobColumnMap = null;
      Map<Feature, Integer> featureColumnMap = null;
      Map<Failure, Integer> failureColumnMap = null;
      // protected Map<String, Integer>        notesCols               = null;
      // columns in the tuning knobs experiments spreadsheet where data goes
      // public Integer statusCol = null;
      // public Integer objectiveCol = null;

      MosaicApp currentApp = null;
      AppTemplate currTemplate = null;
      boolean justStartedNewApp = false;

      for (HSSFRow row : new MiscUtils.IAdaptor<HSSFRow>(sheet.rowIterator())) {
        MiscUtils.IterAdaptor<HSSFCell> cells = new MiscUtils.IterAdaptor<HSSFCell>(row.cellIterator());
        // Try to parse an application spec
        if (justStartedNewApp) {
          justStartedNewApp = false;
          knobColumnMap = new TreeMap<TuningKnob, Integer>();
          featureColumnMap = new TreeMap<Feature, Integer>();
          failureColumnMap = new TreeMap<Failure, Integer>();
          while (cells.hasNext()) {
            HSSFCell cell = cells.next();
            if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
              continue;
            }
            else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
              String s = cell.getRichStringCellValue().getString();
              String ss[] = s.split(" ");
              if (s.endsWith("knob")) {
                TuningKnob knob = currTemplate.getTuningKnob(ss[1]);
                if (knob == null) {
                  int i = 0;
                  for (String s2 : ss) {
                    System.out.printf("%2d: %s\n", i, s2);
                    i++;
                  }
                  assert (false);
                }
                if (false) { System.out.printf("Found knob %s %d\n", knob, cell.getColumnIndex()); }
                // for (MosaicApp a : currentApps) { a.setKnobColumnIdx(knob, cell.getColumnIndex()); }
                knobColumnMap.put(knob, cell.getColumnIndex());
              }
              if (s.endsWith("sensor")) {
                Feature f = currTemplate.getFeature(ss[0]);
                if (f == null) {
                  l.severe("Feature "+ss[0]+" from spreadsheet is not in template");
                  assert (false);
                }
                if (false) { System.out.printf("Found feature value %s %d\n", f, cell.getColumnIndex()); }
                featureColumnMap.put(f, cell.getColumnIndex());
              }
              else if (s.endsWith("failure")) {
                Failure f = (Failure)currTemplate.getFeature(ss[0]);
                assert (f != null);
                if (false) { System.out.printf("Found failure %s %d\n", f, cell.getColumnIndex()); }
                failureColumnMap.put(f, cell.getColumnIndex());
              }
              // else if (ss.length == 1 && ss[0].toUpperCase().equals("OBJECTIVE")) {
              //   // assert (currentApp.objectiveCol == null);
              //   // if (true) { System.out.printf("Found objective %d\n", cell.getColumnIndex()); }
              //   // currentApp.objectiveCol = cell.getColumnIndex();
              // }
              // else if (ss.length == 2 && ss[1].toUpperCase().equals("NOTE")) {
              //   // currentApp.setNoteColumnIdx(ss[1].trim().toUpperCase(), cell.getColumnIndex());
              // }
              // else if (s.toUpperCase().equals("STATUS")) {
              //   // assert (currentApp.statusCol == null);
              //   // currentApp.statusCol = cell.getColumnIndex();
              // }
              // else if (s.toUpperCase().equals("EXPSETTINGS FILENAME")) {
              //   l.warning("We don't really care about persistent experiment settings filenames");
              //   // assert (currentApp.expSettingsCol == null);
              //   // currentApp.expSettingsCol = cell.getColumnIndex();
              // }
              else {
                if (false) System.out.printf("%s didn't parse as anything\n", s);
              }
            }
            else {
              l.severe ("What is this non-string cell??? \""+cell+"\" "+cell.getCellType());
              assert(false);
            }
          }
          assert (currentApp.validate());
        }
        else if (cells.hasNext()) {
          String s = cells.next().toString();
          // look for new applications starting
          if (s.endsWith(props.applicationNameSuffix)) {
            String appName = new String(s.split(" ")[0]);
            currentApp = apps.get(appName);
            l.info("Just started parsing app "+appName+" on row "+row.getRowNum());
            if (currentApp == null) {
              l.severe("Application "+appName+" missing from descriptor");
              // I think "continue" should move to the next line and look for more apps
              continue;
            }
            currTemplate = currentApp.getTemplate();
            if (false) {
              System.out.printf("Put app:%s %s in \n", appName, currentApp);
            }
            justStartedNewApp = true;
          }
          // look for the end of an application
          else if (s.equalsIgnoreCase("end application")) {
            currentApp = null;
          }

          // try to parse a data point
          else if (currentApp != null) {
            boolean dumpStuff = false;
            if (dumpStuff) {
              System.err.printf("%s -- point: [", currentApp);
            }
            Point p = new Point(currTemplate.getKnobSpace());
            for (TuningKnob k : currTemplate.getKnobSpace()) {
              HSSFCell c = row.getCell(knobColumnMap.get(k));
              if (c == null) { p = null; break; }
              Object v = k.stringToValue(c.toString());
              if (v == null) { p = null; break; }
              p.add(v);
              if (dumpStuff) {
                System.err.printf("(%s, %s)", k, v);
              }
            }
            if (dumpStuff) { System.err.printf("]");}
            if (p != null) {
              currentApp.setStatus(p, Point.Status.tested);
              // HSSFCell c = null;
              // status ???
              // c = row.getCell(currentApp.statusCol);
              // assert (c != null);
              // String statusName = getValAsString(c, evaluator);
              // currentApp.setStatus(p, statusName);
              for (Feature f : currTemplate.getFeatures()) {
                if (f instanceof Sensor) {
                  Sensor sensor = (Sensor)f;
                  HSSFCell valCell = row.getCell(featureColumnMap.get(f));
                  if (valCell != null) {
                    Double score = getValAsDouble(valCell, evaluator);
                    if (score != null) {
                      currentApp.setSensorReading(p, sensor, score);
                    }
                    if (dumpStuff) {
                      System.err.printf("(%s -- %1.1f)", sensor, score);
                    }
                  }
                  else {
                    System.err.printf("%s???\n", sensor);
                  }
                }
                else if (f instanceof Failure) {
                  Failure failure = (Failure)f;
                  HSSFCell valCell = row.getCell(failureColumnMap.get(failure));
                  if (valCell != null) {
                    String yesNo = getValAsString(valCell, evaluator);
                    if (yesNo != null) {
                      Boolean value = null;
                      if (yesNo.equalsIgnoreCase("failed")) {
                        value = true;
                      }
                      else if (yesNo.equalsIgnoreCase("passed")) {
                        value = false;
                      }
                      currentApp.setFailureCause(p, failure, value);
                      if (dumpStuff) {
                        System.err.printf("(%s -- %s)", failure, value);
                      }
                    }
                  }
                  else {
                    System.err.printf("%s %s???\n", failureColumnMap.get(failure), failure);
                  }
                }
                else if (f instanceof Aggregate) {
                  
                }
                else if (f instanceof Constant) {
                  
                }
                else if (f instanceof RealValued) {
                  
                }
                else if (f instanceof BooleanValued) {
                  
                }
              }

              // notes???
              // for (Map.Entry<String, Integer> entry : currentApp.getNoteCols()) {
              //   String key = entry.getKey();
              //   int colIdx = entry.getValue();
              //   c = row.getCell(colIdx);
              //   assert (c != null);
              //   String note = getValAsString(c, evaluator);
              //   assert (note != null);
              //   currentApp.setNote(p, key, note);
              // }
            }
            if (dumpStuff) { System.err.printf("\n"); }
          }
        }
      }
    }
    catch (java.io.FileNotFoundException e) {
      l.severe("Trying to read excel file.  Caught "+e);
    }
    catch (IOException e) {
      e.printStackTrace();
      System.out.printf("Caught and I/O exception while attempting to read an Excel file\n");
      System.exit(1);
    }
    // assert(false);
  }
  
  void valueToCell(HSSFCell cell, Feature f, Object genericValue)
  {
    if (genericValue == null) {
      setValueInCell(cell, "no info");
    }
    else {
      if (f instanceof RealValued) {
        Double value = (Double)genericValue;
        setValueInCell(cell, value);
      }
      else if (f instanceof BooleanValued) {
        Boolean value = (Boolean)genericValue;
        if (f instanceof Failure) {
          setValueInCell(cell, value ? "failed" : "passed");
        }
        else {
          setValueInCell(cell, value ? "true" : "false");
        }
      }
      else {
        l.severe("Unpossible!!!");
        System.exit(1);
      }
    }
  }

  int valueToWorkbook(HSSFRow row, int colIdx, Feature f, ValueSource source, Point pt)
  {
    HSSFCell cell = row.createCell(colIdx++);
    Object genericValue = f.getValue(source, pt);
    valueToCell(cell, f, genericValue);
    return colIdx;
  }

  int valueAndPredictionToWorkbook(HSSFRow row, int colIdx, Feature f, SingleSearch search, SingleSearch.TestRecord rec)
  {
    HSSFCell cell = row.createCell(colIdx++);
    Object genericValue = rec.vals.get(f);
    valueToCell(cell, f, genericValue);
    Object genericPrediction = rec.pred.get(f);
    if (genericPrediction == null) {
      setValueInWorkbook(row, colIdx++, "no prediction");
      if (f instanceof RealValued) {
        setValueInWorkbook(row, colIdx++, "no prediction");
      }
    }
    else {
      if (f instanceof RealValued) {
        Pair<Double, Double> prediction = (Pair<Double, Double>)genericPrediction;
        setValueInWorkbook(row, colIdx++, Tuple.get1(prediction));
        setValueInWorkbook(row, colIdx++, Tuple.get2(prediction));
      }
      else if (f instanceof BooleanValued) {
        Double prediction = (Double)genericPrediction;
        setValueInWorkbook(row, colIdx++, prediction);
      }
      else {
        l.severe("Unpossible!!!");
        System.exit(1);
      }
    }
    return colIdx;
  }

  int writeHeaders(boolean forSearch, HSSFSheet sheet, int rowIdx, MosaicApp app, String moreInfo, 
    List<Sensor> sensorsInOrder, List<Failure> failuresInOrder, List<Feature> otherFeaturesInOrder)
  {
    AppTemplate template = app.getTemplate();
    KnobSettingSpace space = template.getKnobSpace();

    for (Feature f : template.getFeatures()) {
      if (f instanceof Sensor) { sensorsInOrder.add((Sensor)f); }
      else if (f instanceof Failure)  { failuresInOrder.add((Failure)f); }
      else { otherFeaturesInOrder.add(f); }
    }

    int colIdx = -1;
    HSSFRow row = null;
    HSSFCell cell = null;
    { // output the basic app info
      colIdx = 0;
      row = sheet.createRow(rowIdx++);
      setValueInWorkbook(row, colIdx++, app.getName() + " " + props.applicationNameSuffix);
      for (Map.Entry<String, String> setting : app.getSettings().entrySet()) {
        setValueInWorkbook(row, colIdx++, setting.getKey() + " => " + setting.getValue() + " setting");
      }
      setValueInWorkbook(row, colIdx++, moreInfo);
    }

    { // output the column headers
      row = sheet.createRow(rowIdx++);
      colIdx = 0;
      if (forSearch) {
        setValueInWorkbook(row, colIdx++, "order");
      }
      for (TuningKnob knob : space) {
        setValueInWorkbook(row, colIdx++, knob.toString() + " knob");
      }
      for (Sensor s : sensorsInOrder) {
        setValueInWorkbook(row, colIdx++, s.getName() + " sensor");
        if (forSearch) {
          setValueInWorkbook(row, colIdx++, s.getName() + " mean");
          setValueInWorkbook(row, colIdx++, s.getName() + " stddev");
        }
      }
      for (Failure f : failuresInOrder) {
        setValueInWorkbook(row, colIdx++, f.getName() + " failure");
        if (forSearch) {
          setValueInWorkbook(row, colIdx++, f.getName() + " probability");
        }
      }
      for (Feature f : otherFeaturesInOrder) {
        setValueInWorkbook(row, colIdx++, f.getName() + " calculated");
        if (forSearch) {
          if (f instanceof BooleanValued) {
            setValueInWorkbook(row, colIdx++, f.getName() + " probability");
          }
          else if (f instanceof RealValued) {
            setValueInWorkbook(row, colIdx++, f.getName() + " mean");
            setValueInWorkbook(row, colIdx++, f.getName() + " stddev");
          }
          else {
            l.severe ("Unpossible!!!");
            System.exit(1);
          }
        }
      }
    }
    return rowIdx;
  }

  void writeWorkbookToFile(HSSFWorkbook w, String namePrefix)
  { // Do the actual file output
    long timeStamp = System.currentTimeMillis();

    File spreadsheetDir = new File (props.outputSpreadsheetDirname);
    String relativeFilename = namePrefix + "_" + timeStamp + ".xls";
    File outFilename = new File(spreadsheetDir, relativeFilename);
    try {
      FileOutputStream fileOut = new FileOutputStream(outFilename);
      try { w.write(fileOut); }
      catch (java.io.IOException e) {
        e.printStackTrace();
        l.warning ("I/O exception while trying to output spreadsheet "+outFilename);
      }
    }
    catch (java.io.FileNotFoundException e) {
      e.printStackTrace();
      l.warning ("File not found exception while trying to output spreadsheet "+outFilename);
    }

    if (props.deleteOlderSpreadsheets) {
      String prefixUnder = namePrefix + "_";
      for (String filename : spreadsheetDir.list()) {
        if (filename.startsWith(prefixUnder)) {
          if (!filename.equals(relativeFilename)) {
            File fileToDelete = new File(spreadsheetDir, filename);
            if (!fileToDelete.exists()) {
              l.severe("\""+fileToDelete+"\" doesn't exists???");
              continue;
            }
            if (!fileToDelete.canWrite()) {
              l.severe("Can't write to \""+fileToDelete+"\"???");
              continue;
            }
            if (fileToDelete.isDirectory()) {
              l.severe("\""+fileToDelete+"\" is a directory???");
              continue;
            }
            // Do the actual delete
            l.info("Deleting \""+fileToDelete+"\" \""+filename+"\" \""+outFilename+"\"");
            if (!fileToDelete.delete()) {
              l.severe("Failed to delete \""+fileToDelete+"\"???");
            }
          }
        }
      }
    }
  }    

  void writeAllAppsToNewExcelFile ()
  {
    systemLock.lock();
    int rowIdx = 0;

    HSSFWorkbook wbOutput = new HSSFWorkbook();
    HSSFSheet sheet = wbOutput.createSheet();

    for (MosaicApp app : apps.values()) {
      rowIdx++;
      AppTemplate template = app.getTemplate();
      KnobSettingSpace space = template.getKnobSpace();
      List <Sensor>   sensorsInOrder        = new LinkedList<Sensor>();
      List <Failure>  failuresInOrder       = new LinkedList<Failure>();
      List <Feature>  otherFeaturesInOrder  = new LinkedList<Feature>();

      rowIdx = writeHeaders(false, sheet, rowIdx, app, "all", sensorsInOrder, failuresInOrder, otherFeaturesInOrder);
      int colIdx = -1;

      HSSFRow row = null;
      HSSFCell cell = null;
      for (Point pt : app.getTestedPoints()) {
        row = sheet.createRow(rowIdx++);
        colIdx = 0;
        Iterator coords = pt.iterator();
        Iterator<TuningKnob> knobs = space.iterator();
        while (coords.hasNext() && knobs.hasNext()) {
          Object      coord = coords.next();
          TuningKnob  knob  = knobs.next();
          String valueStr = knob.valueToString(coord);
          try {
            double value = Double.parseDouble(valueStr);
            setValueInWorkbook(row, colIdx++, value);
          }
          catch (NumberFormatException e) {
            setValueInWorkbook(row, colIdx++, valueStr);
          }
        }
        assert(!coords.hasNext());
        assert(!knobs.hasNext());

        for (Sensor s : sensorsInOrder) {
          colIdx = valueToWorkbook(row, colIdx, s, app, pt);
        }

        for (Failure f : failuresInOrder) {
          colIdx = valueToWorkbook(row, colIdx, f, app, pt);
        }

        for (Feature f : otherFeaturesInOrder) {
          colIdx = valueToWorkbook(row, colIdx, f, app, pt);
        }

        // notes???
        // for (Map.Entry<String, Integer> entry : noteColumnIdxs.entrySet()) {
        //   String key = entry.getKey();
        //   int idx = entry.getValue();
        //   String textValue = results.getCell(ptRowIdx, idx).trim();
        //   app.setNote(pt, key, textValue);
        //   Integer spreadsheetCol = app.getNoteColumnIdx(key);
        //   if (spreadsheetCol == null) {
        //     l.warning ("huh 3?");
        //   }
        //   else {
        //     setValueInWorkbook(app.getPointRowIdx(pt), spreadsheetCol, textValue);
        //   }
        // }
      }
    }

    writeWorkbookToFile(wbOutput, props.allResultsFilenamePrefix);
    systemLock.unlock();
  }

  void writeSearchToNewExcelFile (SingleSearch search, String moreInfo)
  {
    MosaicApp app = (MosaicApp)search.app;
    AppTemplate template = app.getTemplate();
    KnobSettingSpace space = template.getKnobSpace();
    List <Sensor>   sensorsInOrder        = new LinkedList<Sensor>();
    List <Failure>  failuresInOrder       = new LinkedList<Failure>();
    List <Feature>  otherFeaturesInOrder  = new LinkedList<Feature>();

    systemLock.lock();

    HSSFWorkbook wbOutput = new HSSFWorkbook();
    HSSFSheet sheet = wbOutput.createSheet();
    int rowIdx = 1;
    rowIdx = writeHeaders(true, sheet, rowIdx, app, moreInfo, sensorsInOrder, failuresInOrder, otherFeaturesInOrder);
    int colIdx = -1;

    HSSFRow row = null;
    HSSFCell cell = null;
    Integer order = 1;
    for (SingleSearch.TestRecord rec : search.tested) {
      row = sheet.createRow(rowIdx++);
      colIdx = 0;
      setValueInWorkbook(row, colIdx++, ((double)order));
      order = order + 1;
      Iterator coords = rec.pt.iterator();
      Iterator<TuningKnob> knobs = space.iterator();
      while (coords.hasNext() && knobs.hasNext()) {
        Object      coord = coords.next();
        TuningKnob  knob  = knobs.next();
        String valueStr = knob.valueToString(coord);
        try {
          double value = Double.parseDouble(valueStr);
          setValueInWorkbook(row, colIdx++, value);
        }
        catch (NumberFormatException e) {
          setValueInWorkbook(row, colIdx++, valueStr);
        }
      }
      assert(!coords.hasNext());
      assert(!knobs.hasNext());

      for (Sensor s : sensorsInOrder) {
        colIdx = valueAndPredictionToWorkbook(row, colIdx, s, search, rec);
      }

      for (Failure f : failuresInOrder) {
        colIdx = valueAndPredictionToWorkbook(row, colIdx, f, search, rec);
      }

      for (Feature f : otherFeaturesInOrder) {
        colIdx = valueAndPredictionToWorkbook(row, colIdx, f, search, rec);
      }

      // notes???
      // for (Map.Entry<String, Integer> entry : noteColumnIdxs.entrySet()) {
      //   String key = entry.getKey();
      //   int idx = entry.getValue();
      //   String textValue = results.getCell(ptRowIdx, idx).trim();
      //   app.setNote(pt, key, textValue);
      //   Integer spreadsheetCol = app.getNoteColumnIdx(key);
      //   if (spreadsheetCol == null) {
      //     l.warning ("huh 3?");
      //   }
      //   else {
      //     setValueInWorkbook(app.getPointRowIdx(pt), spreadsheetCol, textValue);
      //   }
      // }
    }

    writeWorkbookToFile(wbOutput, app.getName() + "_" + moreInfo);
    systemLock.unlock();
  }
}
