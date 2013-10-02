/*
 *
 */

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Collection;
import com.mallardsoft.tuple.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileReader;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;


import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.layout.StripeLayout;
import com.panayotis.gnuplot.layout.AutoGraphLayout;
import com.panayotis.iodebug.Debug;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.plot.DataSetLinePlot;
import com.panayotis.gnuplot.style.NamedPlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;



// public class FileScoreSource implements ScoreSource
// {
//   public Map<String, App> apps;
//   public Map<Integer, Integer> lastRowIdxOnSheet;
//   HSSFWorkbook wb = null;
//   public Collection<App> getApps() { return apps.values(); }
// 
//   private String excelFilename;
//   private static final String templateFilename = "expSet_TKTemplate";
// 
//   public static File expDir = new File(MiscUtils.readLineFromCommand("experiment_dir"));
//   public static File expFilesDir = new File(expDir, "ExpSettingsFiles");
// 
//   public KnobSettingSpace getKnobSpace(App app)
//   {
//     if (app == null) return null;
//     return ((FileApp)app).space;
//   }
// 
//   public boolean isFinishedTesting(App app, Point point)
//   {
//     if (app == null) return false;
//     Double score = getScore(app, point);
//     Double failureFactor = getFailureFactor(app, point);
//     return score != null || failureFactor != null;
//   }
// 
//   public boolean isSuccessful(App app, Point point)
//   {
//     if (app == null) return false;
//     Double score = getScore(app, point);
//     return score != null;
//   }
// 
//   public Double getScore(App app, Point point)
//   {
//     if (app == null) return null;
//     return ((FileApp)app).scores.get(point);
//   }
// 
//   public Double getFailureFactor(App app, Point point)
//   {
//     if (app == null) return null;
//     return ((FileApp)app).failureFactors.get(point);
//   }
// 
//   public Double getEstimate(App app, Point point)
//   {
//     if (app == null) return null;
//     return ((FileApp)app).estimates.get(point);
//   }
// 
//   public Map<Point, Double> getEstimates(App app)
//   {
//     if (app == null) return null;
//     return ((FileApp)app).estimates;
//   }
// 
//   public void requestMissingScore(App app, Point point, double est)
//   {
//     Double score  = ((FileApp)app).scores.get(point);
//     Double oldEst = ((FileApp)app).estimates.get(point);
//     // if we already have a score or estimate, ignore
//     if (score != null || oldEst != null)
//       return;
//     ((FileApp)app).newEstimates.put(point, est);
//   }
// 

public class Architecture
{
  public String clusterName = "";
  public int numberOfClusters = -1;
  public int numberOfMems = -1;
  public Architecture(String cn, int nc, int nm) {
    clusterName = cn;
    numberOfClusters = nc;
    numberOfMems = nm;
  }
  public String getName() { return clusterName + "_" + numberOfClusters; }

  // sometimes I hate Java
  public int hashCode() {
    return clusterName.hashCode() + (numberOfClusters * 31);
  }

  public boolean equals(Object o)
  {
    if (o == null)
      return false;
    if (o instanceof Architecture) {
      Architecture other = (Architecture)o;
      boolean cond1 = clusterName.equals(other.clusterName);
      boolean cond2 = numberOfClusters == other.numberOfClusters;
      return cond1 && cond2;
    }
    else { return false; }
  }

  public String toString()
  {
    return clusterName + " nc:" + numberOfClusters + " nm:" + numberOfMems;
  }
}

//   public static class FileApp implements App
//   {
//     public String name = "";
//     public Arch arch = null;
//     // columns in the tuning knobs experiments spreadsheet where data goes
//     public int objectiveCol = -1, failureModeCol = -1, estimateCol = -1, expSettingsCol = -1, scriptNotesCol = -1;
//     public int failFactorCol = -1, failModeCol = -1, iterCountCol = -1, initIntervalCol = -1;
//     public Map<Point, Double> scores = null, failureFactors = null, estimates = null, newEstimates = null;
//     public Map<Point, String> expSettingsFiles = null;
//     public Map<Point, Integer> rowIdx;
//     public Map<String, String> globalSettings;
//     public KnobSettingSpace space = null;
//     public int sheetIdx;
//     public int lastRowIdx;
//     boolean maximizeObjective;
//     Sensor objectiveSensor;
//     Map<String, Sensor> sensorTable;
// 
//     public FileApp(String n, Map<String, String> s, Arch a)
//     {
//       name = n;
//       globalSettings = s;
//       arch = a;
//       space = new KnobSettingSpace(0);
//       scores = new HashMap<Point, Double>();
//       failureFactors = new HashMap<Point, Double>();
//       estimates = new HashMap<Point, Double>();
//       newEstimates = new HashMap<Point, Double>();
//       rowIdx = new HashMap<Point, Integer>();
//       expSettingsFiles = new HashMap<Point, String>();
//       sensorTable = new HashMap<String, Sensor>();
//     }
// 
//     public String getName() { return name; }
//     public String getFullName() { return name + "_" + arch.getName(); }
//     public boolean getMaximizeObjective() { return maximizeObjective; }
//     public void setMaximizeObjective(boolean m) { maximizeObjective = m; }
//     public Sensor getObjectiveSensor() { return objectiveSensor; }
//     public void setObjectiveSensor(Sensor s) { objectiveSensor = s; }
//     public Sensor lookupSensor(String name) { return sensorTable.get(name); }
//     public void addSensor(String name, Sensor s) { sensorTable.put(name, s); }
//     public Collection<Sensor> getSensors() { return sensorTable.values(); }
// 
//     public boolean equals(Object o)
//     {
//       if (o == null)
//         return false;
//       if (o instanceof FileApp) {
//         FileApp other = (FileApp)o;
//         return name.equals(other.name) && arch.equals(other.arch);
//       }
//       else { return false; }
//     }
// 
//     // if you re-implement equals you have to re-implement hashCode to get hash-
//     // based data structures to work correctly.
//     public int hashCode()
//     { return name.hashCode() * 31 + arch.hashCode(); }
// 
//     public String toString()
//     {
//       StringBuffer b = new StringBuffer();
//       b.append(String.format("Application: %s  Cluster Name: %s  Number of Clusters: %d\n  knobs:\n",
//         name, arch.clusterName, arch.numberOfClusters));
//       for (TuningKnob k : space) {
//         b.append(String.format("    %s\n", k));
//       }
//       b.append("  Estimates ("+estimates.size()+"):\n");
//       for (Map.Entry<Point, Double> e : estimates.entrySet()) {
//         b.append(String.format("    %f (%s)\n", e.getValue(), e.getKey()));
//       }
//       b.append("  Scores ("+scores.size()+"):\n");
//       for (Map.Entry<Point, Double> e : scores.entrySet()) {
//         b.append(String.format("    %f (%s)\n", e.getValue(), e.getKey()));
//       }
//       b.append("  FailureFactors ("+failureFactors.size()+"):\n");
//       for (Map.Entry<Point, Double> e : failureFactors.entrySet()) {
//         b.append(String.format("    %f (%s)\n", e.getValue(), e.getKey()));
//       }
//       return b.toString();
//     }
//   }
// 
//   private void readExcelFile()
//   {
//     try {
//       apps = new HashMap<String, App>();
//       POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(excelFilename));
//       wb = new HSSFWorkbook(fs);
//       HSSFSheet sheet = wb.getSheetAt(0);
//       HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
// 
//       FileApp currentApp = null;
//       boolean justStartedNewApp = false;
// 
//       int lastRow = -1;
//       for (HSSFRow row : new MiscUtils.IAdaptor<HSSFRow>(sheet.rowIterator())) {
//         lastRow = Math.max(lastRow, row.getRowNum());
//         MiscUtils.IterAdaptor<HSSFCell> cells = new MiscUtils.IterAdaptor<HSSFCell>(row.cellIterator());
//         // Try to parse an application spec
//         if (justStartedNewApp) {
//           justStartedNewApp = false;
//           while (cells.hasNext()) {
//             HSSFCell cell = cells.next();
//             if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
//               String s = cell.getRichStringCellValue().getString();
//               String ss[] = s.split(" ");
//               if (ss.length > 3 && ss[0].toUpperCase().equals("KNOB:")) {
//                 if (ss[2].toUpperCase().equals("INTRANGE")) {
//                   int min = Integer.decode(ss[3]);
//                   int max = Integer.decode(ss[4]);
//                   TuningKnob k = new IntTuningKnob(ss[1], min, max, cell.getColumnIndex());
//                   System.out.printf("Found knob %s [%d..%d] %d\n",
//                     ss[1], min, max, cell.getColumnIndex());
//                   currentApp.space.add(k);
//                 }
//                 else {
//                   System.err.printf("Don't know about knob type %s", ss[2]);
//                   System.exit(1);
//                 }
//               }
//               else if (ss[0].toUpperCase().equals("SENSOR:")) {
//                 Sensor sensor = new RawSensor(ss[1], cell.getColumnIndex());
//               }
//               else if (ss[0].toUpperCase().equals("DERIVEDSENSOR:")) {
//                 String spec[] = s.split(":")[1].split("-");
//                 String name = spec[0].trim();
//                 String formula = spec[1].trim();
//                 // TIMES(Iterations, InitiationInterval)
//               }
//               else if (ss[0].toUpperCase().equals("OBJECTIVE:")) {
//                 if (ss[1].toUpperCase().equals("MAXIMIZE")) {
//                   currentApp.setMaximizeObjective(true);
//                 }
//                 else if (ss[1].toUpperCase().equals("MINIMIZE")) {
//                   currentApp.setMaximizeObjective(false);
//                 }
//                 else {
//                   System.err.println("We can only minimize or maximze objectives for now");
//                   System.exit(1);
//                 }
//                 currentApp.setObjectiveSensor(currentApp.sensorTable.get(ss[2]));
//                 currentApp.objectiveCol = cell.getColumnIndex();
//               }
//               else if (s.toUpperCase().equals("FAILUREMODE")) {
//                 currentApp.failureModeCol = cell.getColumnIndex();
//               }
//               else if (s.toUpperCase().equals("EXPERIMENT SETTINGS FILE")) {
//                 currentApp.expSettingsCol = cell.getColumnIndex();
//               }
//               else if (s.toUpperCase().equals("SCRIPT NOTES")) {
//                 currentApp.scriptNotesCol = cell.getColumnIndex();
//               }
//               else {
//                 // System.out.printf("%s didn't parse as anything\n", s);
//               }
//             }
//           }
//           if (currentApp.space.size() < 1) {
//             System.err.printf("It doesn't make sense for an application to have zero knobs\n");
//             System.exit(1);
//           }
//         }
//         else if (cells.hasNext()) {
//           HSSFCell cell = cells.next();
//           String s = cell.toString();
//           String ss[] = s.split(" ");
//           // look for new applications starting
//           if (ss.length > 1 && ss[0].toUpperCase().equals("APPLICATION:")) {
//             String appName = new String(ss[1]);
// 
//             cell = cells.next();
//             Map<String, String> settings = null;
//             if (!cell.toString().trim().toUpperCase().startsWith("SETTINGS:")) {
//               System.err.printf("Expecting \"Settings:\", found \"%s\"", cell.toString()); System.exit(1);
//             }
//             else {
//               String split1[] = cell.toString().split(":");
//               String split2[] = split1[1].split(",");
//               settings = new HashMap<String, String>();
//               for (String setting : split2) {
//                 String split3[] = setting.split("=>");
//                 if (split3.length == 2) {
//                   settings.put(split3[0].trim(), split3[1].trim());
//                 }
//               }
//             }
// 
//             cell = cells.next();
//             String clusterName = null;
//             if (!cell.toString().trim().toUpperCase().startsWith("CLUSTERNAME:")) {
//               System.err.printf("Expecting ClusterName:"); System.exit(1);
//             }
//             else {
//               String split1[] = cell.toString().split(":");
//               clusterName = new String(split1[1].trim());
//             }
// 
//             cell = cells.next();
//             int numberOfClusters = -1;
//             if (!cell.toString().trim().toUpperCase().startsWith("NUMBEROFCLUSTERS:")) {
//               System.err.printf("Expecting NumberOfClusters:"); System.exit(1);
//             }
//             else {
//               String split1[] = cell.toString().split(":");
//               numberOfClusters = Integer.parseInt(split1[1].trim());
//             }
// 
//             currentApp = new FileApp(appName, settings, new Arch(clusterName, numberOfClusters));
//             apps.put(currentApp.getFullName(), currentApp);
//             currentApp.lastRowIdx = row.getRowNum() + 1;
//             System.out.printf("Put app:%s cluster:%s num clusters:%d in apps\n",
//               appName, clusterName, numberOfClusters);
//             justStartedNewApp = true;
//           }
//           // look for the end of an application
//           else if (s.toUpperCase().equals("END APPLICATION")) {
//             currentApp = null;
//           }
// 
//           // try to parse a data point
//           else if (currentApp != null) {
//             Point p = new Point(currentApp.space);
//             for (TuningKnob k : currentApp.space) {
//               HSSFCell c = row.getCell(k.getColumnIdx());
//               if (c == null) { p = null; break; }
//               Object v = k.stringToValue(c.toString());
//               if (v == null) { p = null; break; }
//               p.add(v);
//             }
//             if (p != null) {
//               // System.out.printf("Found a point %s\n", p);
//               currentApp.rowIdx.put(p, row.getRowNum());
//               currentApp.lastRowIdx = Math.max(currentApp.lastRowIdx, row.getRowNum());
//               HSSFCell c = null;
//               for (Sensor sensor : currentApp.getSensors()) {
//                 c = row.getCell(sensor.spreadsheetCol);
//                 
//               }
//               boolean gotScore = false;
//               if (c != null) {
//                 Double score = getValAsDouble(c, evaluator);
//                 if (score != null) {
//                   currentApp.scores.put(p, score);
//                   gotScore = true;
//                 }
//               }
//               if (!gotScore) {
//                 c = row.getCell(currentApp.estimateCol);
//                 if (c != null) {
//                   Double est = getValAsDouble(c, evaluator);
//                   if (est != null) {
//                     currentApp.estimates.put(p, est);
//                   }
//                 }
//                 c = row.getCell(currentApp.expSettingsCol);
//                 if (c != null) {
//                   String exp = getValAsString(c, evaluator);
//                   if (exp != null) {
//                     currentApp.expSettingsFiles.put(p, exp);
//                   }
//                 }
//               }
//             }
//           }
//         }
//       }
//       lastRowIdxOnSheet.put(0, lastRow);
//     }
//     catch (java.io.FileNotFoundException e) {
//       System.out.printf("Unable to open file %s", excelFilename);
//     }
//     catch (IOException e) { System.out.printf(" fooff "); }
//   }
// 
//   private class PointAppPair
//   {
//     public Point p = null;
//     public FileApp a = null;
// 
//     public PointAppPair(Point cp, FileApp ca) {
//       p = cp; a = ca;
//     }
//   }
// 
//   /*
//    * Raw "sensor readings" that we might care about:
//    * pre-optimization number of vertices
//    * "                        " nodes
//    * "                        " nets
//    * "                        " edges
//    * pre-SPR number of ops
//    * pre-SPR number of nets
//    * pre-SPR number of array ops
//    * ...
//    * initial II
//    * final II
//    * iteration count
//    */
// 
//   /*
//    * findNewScores should go through all the applications, find rows (a.k.a.
//    * points) that don't have scores filled in, and try to get scores from actual
//    * application runs.  It works by invoking a bunch of script fu.
//    */
//   private void findNewScores()
//   {
//     Map<String, Set<PointAppPair>> ptsAssociatedWithExpFiles = new HashMap<String, Set<PointAppPair>>();
//     for (App genericApp : apps.values()) {
//       FileApp app = (FileApp)genericApp;
//       for (Map.Entry<Point, String> expEntry : app.expSettingsFiles.entrySet()) {
//         Point pt = expEntry.getKey();
//         // double make sure we only get the points that don't already have scores
//         if (app.scores.get(pt) == null) {
//           String fname = expEntry.getValue();
//           Set<PointAppPair> pts = ptsAssociatedWithExpFiles.get(fname);
//           if (pts == null) {
//             pts = new HashSet<PointAppPair>();
//             ptsAssociatedWithExpFiles.put(fname, pts);
//           }
//           pts.add(new PointAppPair(pt, app));
//         }
//       }
//     }
// 
//     File newCWD = new File(expDir, "RunSPR");
//     // String oldCWD = System.setProperty("user.dir", newCWD.getPath());
// 
//     // File collectScript = new File(new File (new File(expDir, "scripts"), "common"),
//     // "collectTuningKnobResults.pl");
//     File collectScript = new File(new File(expDir, "scripts"), "dumb.sh");
// 
//     for (Map.Entry<String, Set<PointAppPair>> fileAndPointsEntry : ptsAssociatedWithExpFiles.entrySet()) {
//       String expFilename = fileAndPointsEntry.getKey();
//       Set<PointAppPair> pointsOfInterest = fileAndPointsEntry.getValue();
//       File expFile = new File(expFilesDir, expFilename);
//       // String scriptCmd = collectScript.getPath() + " " + expFile.getPath();
//       String cmdArg[] = new String[2];
//       cmdArg[0] = collectScript.getPath(); cmdArg[1] = expFile.getPath();
//       System.out.printf("Executing collection command ...\n  %s %s\nfrom dir\n  %s\n",
//         cmdArg[0], cmdArg[1], newCWD.getPath());
//       int cmdLineScriptRet = MiscUtils.execAndWait(cmdArg, null, newCWD);
//       // System.out.printf("Return code: %d\n", cmdLineScriptRet);
//       String strippedFilename =
//         expFilename.lastIndexOf(".") < 0 ? expFilename :
//           expFilename.substring(0, expFilename.lastIndexOf("."));
//       String resultsFilename = "tk_" + strippedFilename + ".txt";
//       File resultsFilePath = new File(newCWD, resultsFilename);
//       BufferedReaderIterable resultsFile = null;
//       try {
//         resultsFile = new BufferedReaderIterable(new FileReader(resultsFilePath));
//       }
//       catch (FileNotFoundException e) {
//         System.err.printf("Unable to open results file '%s'\n", resultsFilename);
//         File fs[] = newCWD.listFiles();
//         for (File f : fs) {
//           System.out.printf("? %s ?   ", f.getName());
//         }
//         System.exit(1);
//       }
//       String columnSpec = null;
//       // indexes into the CSV file produced by the collection script
//       int nameIdx = -1, appIdx = -1, adpcIdx = -1, archIdx = -1, execStatusIdx = -1;
//       int latencyIdx = -1, numPesIdx = -1, peNameIdx = -1;
//       int sprSuccIdx = -1, sprRunIdx = -1, sprFailIdx = -1, plcmtFailIdx = -1, routeFailIdx = -1;
//       int simTimeSimIdx = -1, simTimeRealIdx;
//       int simSuccIdx = -1, simRunIdx = -1, simFailIdx = -1, maxColIdx = -1;
//       
//       for (String line : resultsFile) {
//         // System.out.printf("\nWHYWHY\n%s\n%s\n\n\n", resultsFilePath, line);
//         if (columnSpec == null) {
//           columnSpec = new String(line);
//           // System.out.printf("Got column spec '%s'\n", columnSpec);
// 
//           String columns[] = columnSpec.split(",");
//           int colIdx = 0;
//           for (String column : columns) {
//             // System.out.printf("column %d = %s\n", colIdx, column);
//             String c = column.trim().toUpperCase();
//             if      (c.equals("#NAME"))         { nameIdx         = colIdx; }
//             else if (c.equals("EXEC_STATUS"))   { execStatusIdx   = colIdx; }
//             else if (c.equals("APP"))           { appIdx          = colIdx; }
//             else if (c.equals("ADPC'S"))        { adpcIdx         = colIdx; }
//             else if (c.equals("ARCH"))          { archIdx         = colIdx; }
//             else if (c.equals("LATENCY"))       { latencyIdx      = colIdx; }
//             else if (c.equals("FINAL NUM PES")) { numPesIdx       = colIdx; }
//             else if (c.equals("PE NAME"))       { peNameIdx       = colIdx; }
//             else if (c.equals("SPRSUCCESS"))    { sprSuccIdx      = colIdx; }
//             else if (c.equals("SPRRUNNING"))    { sprRunIdx       = colIdx; }
//             else if (c.equals("SPRFAILURE"))    { sprFailIdx      = colIdx; }
//             else if (c.equals("PLCMTFAIL"))     { plcmtFailIdx    = colIdx; }
//             else if (c.equals("ROUTEFAIL"))     { routeFailIdx    = colIdx; }
//             else if (c.equals("POSTSIMSUCCESS")){ simSuccIdx      = colIdx; }
//             else if (c.equals("POSTSIMRUNNING")){ simRunIdx       = colIdx; }
//             else if (c.equals("POSTSIMFAILURE")){ simFailIdx      = colIdx; }
//             else if (c.equals("SIMULATEDTIME")) { simTimeSimIdx   = colIdx; }
//             else if (c.equals("SIMULATORTIME")) { simTimeRealIdx  = colIdx; }
// 
//             // System.out.printf("App col : %d\n", appIdx);
//             // System.out.printf("adpc col : %d\n", adpcIdx);
//             // System.out.printf("arch col : %d\n", archIdx);
//             // System.out.printf("latency col : %d\n", latencyIdx);
//             // System.out.printf("pes col : %d\n", numPesIdx);
//             // System.out.printf("pes col : %d\n", peNameIdx);
//             colIdx++;
//           }
//         }
//         else {
//           String columns[] = line.split(",");
//           int colIdx = 0;
// 
//           // build up the full app name for this result
//           String appName = columns[appIdx].trim();
//           String clusterName = columns[peNameIdx].trim();
//           int numberOfClusters = Integer.parseInt(columns[numPesIdx].trim());
//           String fullAppName = appName + "_" + clusterName + "_" + numberOfClusters;
//           FileApp app = (FileApp)apps.get(fullAppName);
//           if (app.space == null) {
//             System.out.printf("Missing space for %s\n", fullAppName);
//             System.exit(1);
//           }
//           String knobSettings[] = columns[adpcIdx].trim().split("_");
//           // System.out.printf("knob settings: '%s'\n", columns[adpcIdx].trim());
//           Point pt = new Point(app.space);
//           // Try to find settings for each of the knobs
//           for (TuningKnob knob : app.space) {
//             String knobName = knob.getName();
//             for (String knobSettingStr : knobSettings) {
//               if (knobSettingStr.startsWith(knobName)) {
//                 String settingStr = knobSettingStr.substring(knobName.length());
//                 Object setting = knob.stringToValue(settingStr);
//                 pt.add(setting);
//                 // System.out.printf("'%s' '%s' '%s' ", knobName, settingStr, setting);
//                 break;
//               }
//             }
//           }
//           // System.out.printf("\n");
//           
//           if (columns[execStatusIdx].trim().equals("+")) {
//             // now we have an app and a point, see if we were waiting for this one
//             for (PointAppPair pointAndApp : pointsOfInterest) {
//               FileApp newApp = pointAndApp.a;
//               Point newPt = pointAndApp.p;
//               if (newApp.getFullName().equals(app.getFullName()) &&
//                   newPt.equals(pt)) {
//                 boolean sprSucc = columns[sprSuccIdx].trim().toUpperCase().equals("YES");
//                 boolean sprFail = columns[sprFailIdx].trim().toUpperCase().equals("YES");
//                 boolean sprRun  = columns[sprRunIdx].trim().toUpperCase().equals("YES");
//                 if (sprSucc) {
//                   boolean simSucc = columns[simSuccIdx].trim().toUpperCase().equals("YES");
//                   boolean simFail = columns[simFailIdx].trim().toUpperCase().equals("YES");
//                   boolean simRun  = columns[simRunIdx].trim().toUpperCase().equals("YES");
//                   if (simSucc) {
//                     // match!
//                     String appRuntimeStr = columns[simTimeSimIdx].trim();
//                     // knock off the three least significant digits to avoid overflow problems
//                     String appRuntimeStr2 = appRuntimeStr.substring(0, appRuntimeStr.length()-3);
//                     try {
//                       double appRuntime = Double.parseDouble(appRuntimeStr2);
//                       // if (newApp.getName().indexOf("dmm") != -1) {
//                       //   System.out.printf("\n\nThese can't all be the same %s %s %f\n\n\n",
//                       //     appRuntimeStr, appRuntimeStr2, appRuntime);
//                       // }
//                       for (String column : columns) {
//                         System.out.printf("(%d '%s')", colIdx, column.trim());
//                         colIdx++;
//                       }
//                       double score = 10000.0 / appRuntime;
//                       newApp.scores.put(newPt, score);
//                       // FIXME: The next line is important
//                       // setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scoreCol, score);
//                       setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "success");
//                       // could record something about simTimeRealIdx
//                     }
//                     catch (NumberFormatException e) {
//                       // System.out.printf("TK_WARNING: No simulated runtime. app (%s) point(%s)\n",
//                       //                   newApp.getFullName(), newPt);
//                       String m = "unable to parse sim time ("+appRuntimeStr+")";
//                       setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, m);
//                     }
//                   }
//                   else if (simFail) {
//                     // System.out.printf("TK_WARNING: simulation failed for app (%s) point(%s)\n", 
//                     //                   newApp.getFullName(), newPt);
//                     newApp.scores.put(newPt, 0.0);
//                     // FIXME: The next line is important
//                     // setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scoreCol, 0.0);
//                     setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "post-SPR sim failed");
//                   }
//                   else if (simRun) {
//                     // okay, it's still running, whatever
//                     setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "post-SPR sim running (maybe)");
//                   }
//                   else {
//                     // System.out.printf("TK_WARNING: post-SPR sim in strange state. app (%s) point (%s)\n",
//                     //   newApp.getFullName(), newPt);
//                     setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "post-SPR sim weird state");
//                   }
//                 }
//                 else if (sprFail) {
//                   // At some point, try to understand the failure better
//                   // int , plcmtFailIdx = -1, routeFailIdx = -1;
//                   newApp.scores.put(newPt, 0.0);
//                   // FIXME: The next line is important
//                   // setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scoreCol, 0.0);
//                   setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "SPR failed");
//                 }
//                 else if (sprRun) {
//                   // okay, it's still running, whatever
//                   setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "SPR running (maybe)");
//                 }
//                 else {
//                   // System.out.printf("TK_WARNING: SPR in strange state.  app (%s) point (%s)\n",
//                   //   newApp.getFullName(), newPt);
//                   setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "SPR weird state");
//                 }
//               }
//             }
//           }
//           else if (columns[execStatusIdx].trim().equals("?")) {
//             // The scripts seem to have barfed in some way.  Assume 0 and issue a warning
//             for (PointAppPair pointAndApp : pointsOfInterest) {
//               FileApp newApp = pointAndApp.a;
//               Point newPt = pointAndApp.p;
//               if (newApp.getFullName().equals(app.getFullName()) &&
//                   newPt.equals(pt)) {
//                 System.out.printf("TK_WARNING: \"success\"=\"?\"  app(%s) point(%s)\n",
//                   newApp.getFullName(), newPt);
//                 setValueInWorkbook(newApp.rowIdx.get(newPt), newApp.scriptNotesCol, "SPR not run?");
//               }
//             }
//           }
//         }
//       }
//     }
//     // System.setProperty("user.dir", oldCWD);
//   }
// 
//   void setValueInWorkbook(int rowIdx, int colIdx, Object value)
//   {
//     System.out.printf("Setting [%d, %d] val:%s\n", rowIdx, colIdx, value);
//     HSSFSheet sheet = wb.getSheetAt(0);
//     HSSFRow row = sheet.getRow(rowIdx);
//     HSSFCell c = row.getCell(colIdx);
//     if (c == null) {
//       c = row.createCell(colIdx);
//     }
//     if (value instanceof Double) {
//       c.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
//       c.setCellValue(((Double)value));
//     }
//     else if (value instanceof String) {
//       c.setCellType(HSSFCell.CELL_TYPE_STRING);
//       c.setCellValue(((String)value));
//     }
//     else {
//       System.out.printf("TK_WARNING: setValueInWorkbook: [%d, %d] unknown value type: %s\n",
//         rowIdx, colIdx, value);
//     }
//   }
// 
//   public FileScoreSource(String f, boolean doFindNewScores) {
//     lastRowIdxOnSheet = new HashMap<Integer, Integer>();
//     excelFilename = f;
//     readExcelFile();
// 
//     if (doFindNewScores) {
//       findNewScores();
//     }
// 
//     if (false) {
//       for (App app : apps.values()) {
//         System.out.println(""+app);
//       }
//     }
//   }
// 
//   Double getValAsDouble(HSSFCell cell, HSSFFormulaEvaluator evaluator)
//   {
//     int cellType = cell.getCellType();
//     if (cellType == HSSFCell.CELL_TYPE_NUMERIC) {
//       return cell.getNumericCellValue();
//     }
//     else if (cellType == HSSFCell.CELL_TYPE_FORMULA) {
//       HSSFFormulaEvaluator.CellValue cellVal = evaluator.evaluate(cell);
//       if (cellVal.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
//         return cellVal.getNumberValue();
//       }
//       else { return null; }
//     }
//     else { return null; }
//   }
// 
//   String getValAsString(HSSFCell cell, HSSFFormulaEvaluator evaluator)
//   {
//     int cellType = cell.getCellType();
//     if (cellType == HSSFCell.CELL_TYPE_STRING) {
//       return cell.getRichStringCellValue().getString();
//     }
//     else if (cellType == HSSFCell.CELL_TYPE_FORMULA) {
//       HSSFFormulaEvaluator.CellValue cellVal = evaluator.evaluate(cell);
//       if (cellVal.getCellType() == HSSFCell.CELL_TYPE_STRING) {
//         return cellVal.getStringValue();
//       }
//       else { return null; }
//     }
//     else { return null; }
//   }
// 
//   public void writeNewEstimates()
//   {
//     // phase 1: build up a big map from archs to maps from apps to sets of points
//     // phase 2: partition the big map to restrict the number of jobs per
//     //          settings file
//     // phase 3: update spreadsheet and dump out settings files
// 
//     // build up the big map with no partitioning
//     Map<Arch, Map<FileApp, Set<Point>>> rawPointsPerArch = new HashMap<Arch, Map<FileApp, Set<Point>>>();
//     for (Map.Entry<String, App> appEntry : apps.entrySet()) {
//       FileApp app = (FileApp)appEntry.getValue();
//       System.out.printf("Adding new ests. app=%s |pts|=%d\n", app.getFullName(), app.newEstimates.size());
//       for (Map.Entry<Point, Double> estEntry : app.newEstimates.entrySet()) {
//         Point pt = estEntry.getKey();
//         double est = estEntry.getValue();
// 
//         // add this point to the appropriate set to be put in a new expSettings
//         Map<FileApp, Set<Point>> ptsPerApp = rawPointsPerArch.get(app.arch);
//         if (ptsPerApp == null) {
//           ptsPerApp = new HashMap<FileApp, Set<Point>>();
//           rawPointsPerArch.put(app.arch, ptsPerApp);
//         }
//         Set<Point> pts = ptsPerApp.get(app);
//         if (pts == null) {
//           pts = new HashSet<Point>();
//           ptsPerApp.put(app, pts);
//         }
//         pts.add(pt);
//       }
//     }
// 
//     // now partition the map into bite-size chunks
//     int biteSize = 100;
//     long timeStamp = System.currentTimeMillis();
//     // This rather gnarly type can be read like so:
//     // "mapping from architectures to sets of `point collections', where each
//     // `point collection' is a pair of expSettings file name and mapping from
//     // apps to points"
//     Map<Arch, Set<Pair<String,ComparableMap<FileApp, Set<Point>>>>> chunkedPointsPerArch =
//       new HashMap<Arch, Set<Pair<String,ComparableMap<FileApp, Set<Point>>>>>();
//     for (Map.Entry<Arch, Map<FileApp,Set<Point>>> archEntry : rawPointsPerArch.entrySet()) {
//       Arch arch = archEntry.getKey();
//       Map<FileApp, Set<Point>> ptsPerApp = archEntry.getValue();
//       
//       int totalNumPoints = 0;
//       for (Set<Point> points : ptsPerApp.values()) {
//         totalNumPoints += points.size();
//       }
//       if (totalNumPoints > 0) {
//         int numSets = ((totalNumPoints - 1) / biteSize) + 1;
//         int setNumber = 1;
//         
//         Set<Pair<String, ComparableMap<FileApp, Set<Point>>>> ptsPerAppSet =
//           new HashSet<Pair<String, ComparableMap<FileApp, Set<Point>>>>();
//         chunkedPointsPerArch.put(arch, ptsPerAppSet);
// 
//         Iterator<Map.Entry<FileApp,Set<Point>>> ptsPerAppIter = ptsPerApp.entrySet().iterator();
//         Map.Entry<FileApp,Set<Point>> ptsPerAppCurr = null;
//         ComparableMap<FileApp,Set<Point>> ptsPerAppTarg = new ComparableHashMap<FileApp, Set<Point>>();
//         String expSettingsFilename = String.format("expSet_TKAuto_%s_%d_%dof%d.pl",
//           arch.getName(), timeStamp, setNumber, numSets);
//         // expSettingsFilenames.put(app.arch, expSettingsFilename);
//         ptsPerAppSet.add(Tuple.from(expSettingsFilename, ptsPerAppTarg));
// 
//         if (ptsPerAppIter.hasNext()) {
//           ptsPerAppCurr = ptsPerAppIter.next();
//         }
//         else { break; }
// 
//         FileApp appCurr = ptsPerAppCurr.getKey();
//         Iterator<Point> pointsCurrIter = ptsPerAppCurr.getValue().iterator();
//         Set<Point> pointsTarg = new HashSet<Point>();
//         ptsPerAppTarg.put(appCurr, pointsTarg);
// 
//         while (true) {
//           int i = 0;
//           while (i < biteSize) {
//             if (pointsCurrIter.hasNext()) {
//               Point p = pointsCurrIter.next();
//               pointsTarg.add(p);
//               i++;
//             }
//             else { // no more points in the current app
//               if (ptsPerAppIter.hasNext()) {
//                 ptsPerAppCurr = ptsPerAppIter.next();
//                 appCurr = ptsPerAppCurr.getKey();
//                 pointsCurrIter = ptsPerAppCurr.getValue().iterator();
//                 pointsTarg = new HashSet<Point>();
//                 ptsPerAppTarg.put(appCurr, pointsTarg);
//               }
//               else { // no more applications left
//                 break;
//               }
//             }
//           }
//           if ((!pointsCurrIter.hasNext()) && (!ptsPerAppIter.hasNext())) { break; }
//           else {
//             ptsPerAppTarg = new ComparableHashMap<FileApp, Set<Point>>();
//             setNumber++;
//             expSettingsFilename = String.format("expSet_TKAuto_%s_%d_%dof%d.pl",
//               arch.getName(), timeStamp, setNumber, numSets);
//             ptsPerAppSet.add(Tuple.from(expSettingsFilename, ptsPerAppTarg));
//             pointsTarg = new HashSet<Point>();
//             ptsPerAppTarg.put(appCurr, pointsTarg);
//           }
//         }
//       }
//     }
// 
//     
//     HSSFSheet sheet = wb.getSheetAt(0);
//     for (Map.Entry<Arch, Set<Pair<String,ComparableMap<FileApp, Set<Point>>>>> archEntry :
//             chunkedPointsPerArch.entrySet()) {
//       Arch arch = archEntry.getKey();
// 
//       for (Pair<String,ComparableMap<FileApp, Set<Point>>> chunk : archEntry.getValue()) {
//         String settingsFilename = Tuple.get1(chunk);
//         Map<FileApp, Set<Point>> ptsPerApp = Tuple.get2(chunk);
// 
//         // update the spreadsheet in memory
//         for (Map.Entry<FileApp, Set<Point>> appEntry : ptsPerApp.entrySet()) {
//           FileApp app = appEntry.getKey();
//           for (Point pt : appEntry.getValue()) {
//             double est = app.newEstimates.get(pt);
//             // add the estimate to the spreadsheet
//             int lastRowOnSheet = lastRowIdxOnSheet.get(0);
//             sheet.shiftRows(app.lastRowIdx + 1,lastRowOnSheet,1);
//             lastRowIdxOnSheet.put(0, lastRowOnSheet+1);
//             app.lastRowIdx++;
//             for (App genericApp : apps.values()) {
//               FileApp app2 = (FileApp) genericApp;
//               if (app2.lastRowIdx > app.lastRowIdx)
//                 app2.lastRowIdx++;
//             }
//             HSSFRow row = sheet.getRow(app.lastRowIdx);
//             if (row == null) {
//               row = sheet.createRow(app.lastRowIdx);
//             }
//             Iterator coords = pt.iterator();
//             Iterator<TuningKnob> dims = app.space.iterator();
//             while (coords.hasNext() && dims.hasNext()) {
//               Object coord = coords.next();
//               TuningKnob dim = dims.next();
//               HSSFCell cell = row.getCell(dim.getColumnIdx());
//               if (cell == null)
//                 cell = row.createCell(dim.getColumnIdx());
//               String valueStr = dim.valueToString(coord);
//               try {
//                 double value = Double.parseDouble(valueStr);
//                 cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
//                 cell.setCellValue(value);
//               }
//               catch (NumberFormatException e) {
//                 cell.setCellType(HSSFCell.CELL_TYPE_STRING);
//                 cell.setCellValue(new HSSFRichTextString(valueStr));
//               }
//             }
//             assert(!coords.hasNext());
//             assert(!dims.hasNext());
// 
//             HSSFCell cell = row.getCell(app.estimateCol);
//             if (cell == null)
//               cell = row.createCell(app.estimateCol);
//             cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
//             cell.setCellValue(est);
// 
//             cell = row.getCell(app.expSettingsCol);
//             if (cell == null) {
//               cell = row.createCell(app.expSettingsCol);
//             }
//             if (cell == null) {
//               System.out.printf("Why does java hate me?\n  %s\n  %s\n", app.getFullName(), pt);
//               System.exit(1);
//             }
//             cell.setCellType(HSSFCell.CELL_TYPE_STRING);
//             // don't replace the settings filename, if there's already one there
//             if (cell.getRichStringCellValue() == null) {
//               cell.setCellValue(new HSSFRichTextString(settingsFilename));
//             }
//             else if (cell.getRichStringCellValue().getString().trim().length() < 1) {
//               cell.setCellValue(new HSSFRichTextString(settingsFilename));
//             }
//           }
//         }
// 
//         // dump a settings file
//         File templateFilePath = new File(expDir, templateFilename);
//         BufferedReaderIterable templateFile = null;
//         try {
//           templateFile = new BufferedReaderIterable(new FileReader(templateFilePath));
//         }
//         catch (FileNotFoundException e) {
//           System.err.printf("Unable to open template file '%s'\n", templateFilename);
//           System.exit(1);
//         }
// 
//         File expFilePath = new File(expFilesDir, settingsFilename);
//         PrintWriter expFile = null;
//         try {
//           expFile = new PrintWriter(expFilePath);
//         }
//         catch (FileNotFoundException e) {
//           System.err.printf("Unable to open exp settings file '%s'\n", settingsFilename);
//           System.exit(1);
//         }
// 
//         System.out.printf("Writing settings file %s\n", settingsFilename);
// 
//         for (String templateLine : templateFile) {
//           int appNamesIdx     = templateLine.indexOf("<APPLICATION NAMES>");
//           int appSetsIdx      = templateLine.indexOf("<APPLICATION SETTINGS>");
//           int appSweepsIdx    = templateLine.indexOf("<APPLICATION SWEEPS>");
//           int numClustersIdx  = templateLine.indexOf("<NUMBER OF CLUSTERS>");
//           int clusterNameIdx  = templateLine.indexOf("<CLUSTER NAME>");
//           if (appNamesIdx != -1) {
//             // our @appList     = (<APPLICATION NAMES>);
//             StringBuffer apps = new StringBuffer("");
//             for (App app : ptsPerApp.keySet()) {
//               apps.append("\"" + app.getName() + "\", ");
//             }
//             String outStr = templateLine.replaceAll("<APPLICATION NAMES>", apps.toString());
//             expFile.printf("%s\n", outStr);
//           }
//           else if (appSetsIdx != -1) {
//             // our %appSettings = ( 
//             // <APPLICATION SETTINGS>
//             // );
//             for (App app : ptsPerApp.keySet()) {
//               expFile.printf("  \"%s\"=> {},\n", app.getName());
//             }
//           }
//           else if (appSweepsIdx != -1) {
//             // our %appSweeps = ( 
//             // <APPLICATION SWEEPS>
//             // );
//             for (Map.Entry<FileApp,Set<Point>> ptsAppEntry : ptsPerApp.entrySet()) {
//               FileApp app = ptsAppEntry.getKey();
//               Set<Point> pts = ptsAppEntry.getValue();
// 
//               expFile.printf("  \"%s\"=> {\n", app.getName());
//               for (Map.Entry<String, String> settingEntry : app.globalSettings.entrySet()) {
//                 String settingName = settingEntry.getKey();
//                 String settingValue = settingEntry.getValue();
//                 expFile.printf("    \"%s\"=>[", settingName);
//                 for (Point pt : pts) { expFile.printf("\"%s\", ", settingValue); }
//                 expFile.printf("],\n");
//               }
//               int dimIdx = 0;
//               for (TuningKnob knob : app.space) {
//                 expFile.printf("    \"%s\"=>[", knob.getName());
//                 for (Point pt : pts) {
//                   String setting = knob.valueToString(pt.get(dimIdx));
//                   expFile.printf("\"%s\", ", setting);
//                 }
//                 expFile.printf("],\n");
//                 dimIdx++;
//               }
//               expFile.printf("  },\n");
//             }
//           }
//           else if (numClustersIdx != -1) {
//             //   "numPEs"          => <NUMBER OF CLUSTERS>,
//             String outStr = templateLine.replaceAll("<NUMBER OF CLUSTERS>", "\""+arch.numberOfClusters+"\"");
//             expFile.printf("%s\n", outStr);
//           }
//           else if (clusterNameIdx != -1) {
//             //   "peNameShort"     => <CLUSTER NAME>,
//             String outStr = templateLine.replaceAll("<CLUSTER NAME>", "\""+arch.clusterName+"\"");
//             expFile.printf("%s\n", outStr);
//           }
//           else {
//             expFile.printf("%s\n", templateLine);
//           }
//         }
// 
//         expFile.close();
// 
//         // makeSettingsFile( archEntry.getKey(), archEntry.getValue(),
//         //                   expSettingsFilenames.get(archEntry.getKey()));
// 
//       }
//     }
//   
//     // dump the updated spreadsheet
//     try {
//       FileOutputStream fileOut = new FileOutputStream(excelFilename);
//       // HSSFSheet sheet = wb.getSheetAt(0);
//       // HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
//       wb.write(fileOut);
//       fileOut.close();
//     }
//     catch (FileNotFoundException e) {
//       System.err.printf("FNF when trying to write new estimates\n");
//       System.exit(1);
//     }
//     catch (IOException e) {
//       System.err.printf("IO when trying to write new estimates\n");
//       System.exit(1);
//     }
//   }
// 
//   public static void makeSettingsFile(Arch arch, Map<FileApp, Set<Point>> ptsPerApp, 
//       String expSettingsFilename)
//   {
//     File templateFilePath = new File(expDir, templateFilename);
//     BufferedReaderIterable templateFile = null;
//     try {
//       templateFile = new BufferedReaderIterable(new FileReader(templateFilePath));
//     }
//     catch (FileNotFoundException e) {
//       System.err.printf("Unable to open template file '%s'\n", templateFilename);
//       System.exit(1);
//     }
// 
//     // String expSettingsFilename = expSettingsFilenames.get(arch);
//     File expFilePath = new File(expFilesDir, expSettingsFilename);
//     PrintWriter expFile = null;
//     try {
//       expFile = new PrintWriter(expFilePath);
//     }
//     catch (FileNotFoundException e) {
//       System.err.printf("Unable to open exp settings file '%s'\n", expSettingsFilename);
//       System.exit(1);
//     }
// 
//     for (String templateLine : templateFile) {
//       int appNamesIdx     = templateLine.indexOf("<APPLICATION NAMES>");
//       int appSetsIdx      = templateLine.indexOf("<APPLICATION SETTINGS>");
//       int appSweepsIdx    = templateLine.indexOf("<APPLICATION SWEEPS>");
//       int numClustersIdx  = templateLine.indexOf("<NUMBER OF CLUSTERS>");
//       int clusterNameIdx  = templateLine.indexOf("<CLUSTER NAME>");
//       if (appNamesIdx != -1) {
//         // our @appList     = (<APPLICATION NAMES>);
//         StringBuffer apps = new StringBuffer("");
//         for (App app : ptsPerApp.keySet()) {
//           apps.append("\"" + app.getName() + "\", ");
//         }
//         String outStr = templateLine.replaceAll("<APPLICATION NAMES>", apps.toString());
//         expFile.printf("%s\n", outStr);
//       }
//       else if (appSetsIdx != -1) {
//         // our %appSettings = ( 
//         // <APPLICATION SETTINGS>
//         // );
//         for (App app : ptsPerApp.keySet()) {
//           expFile.printf("  \"%s\"=> {},\n", app.getName());
//         }
//       }
//       else if (appSweepsIdx != -1) {
//         // our %appSweeps = ( 
//         // <APPLICATION SWEEPS>
//         // );
//         for (Map.Entry<FileApp,Set<Point>> ptsAppEntry : ptsPerApp.entrySet()) {
//           FileApp app = ptsAppEntry.getKey();
//           Set<Point> pts = ptsAppEntry.getValue();
// 
//           expFile.printf("  \"%s\"=> {\n", app.getName());
//           for (Map.Entry<String, String> settingEntry : app.globalSettings.entrySet()) {
//             String settingName = settingEntry.getKey();
//             String settingValue = settingEntry.getValue();
//             expFile.printf("    \"%s\"=>[", settingName);
//             for (Point pt : pts) { expFile.printf("\"%s\", ", settingValue); }
//             expFile.printf("],\n");
//           }
//           int dimIdx = 0;
//           for (TuningKnob knob : app.space) {
//             expFile.printf("    \"%s\"=>[", knob.getName());
//             for (Point pt : pts) {
//               String setting = knob.valueToString(pt.get(dimIdx));
//               expFile.printf("\"%s\", ", setting);
//             }
//             expFile.printf("],\n");
//             dimIdx++;
//           }
//           expFile.printf("  },\n");
//         }
//       }
//       else if (numClustersIdx != -1) {
//         //   "numPEs"          => <NUMBER OF CLUSTERS>,
//         String outStr = templateLine.replaceAll("<NUMBER OF CLUSTERS>", "\""+arch.numberOfClusters+"\"");
//         expFile.printf("%s\n", outStr);
//       }
//       else if (clusterNameIdx != -1) {
//         //   "peNameShort"     => <CLUSTER NAME>,
//         String outStr = templateLine.replaceAll("<CLUSTER NAME>", "\""+arch.clusterName+"\"");
//         expFile.printf("%s\n", outStr);
//       }
//       else {
//         expFile.printf("%s\n", templateLine);
//       }
//     }
// 
//     expFile.close();
//   }
// 
//   public void plotGraphs()
//   {
//     for (Map.Entry<String, App> appEntry : apps.entrySet()) {
//       FileApp app = (FileApp)appEntry.getValue();
//       if (app.space.size() != 2) {
//         System.out.printf("plotGraphs can only handle apps with 2 knobs, not %d\n", app.space.size());
//       }
//       else {
//         TuningKnob k0 = app.space.get(0);
//         TuningKnob k1 = app.space.get(1);
// 
//         int numGoodValues = 0;
//         for (Map.Entry<Point, Double> scoreEntry : app.scores.entrySet()) {
//           Point pt = scoreEntry.getKey();
//           Double score = scoreEntry.getValue();
//           if (score != null) {
//             if (score < 10) {
//               numGoodValues++;
//             }
//           }
//         }
//         double plot[][] = new double[numGoodValues][3];
// 
//         int i = 0;
//         double max = 0.0;
//         for (Map.Entry<Point, Double> scoreEntry : app.scores.entrySet()) {
//           Point pt = scoreEntry.getKey();
//           Double score = scoreEntry.getValue();
//           if (score != null) {
//             if (score < 10) {
//               Object c0 = pt.get(0);
//               Object c1 = pt.get(1);
//               plot[i][0] = k0.valueToCoordinate(c0);
//               plot[i][1] = k1.valueToCoordinate(c1);
//               plot[i][2] = score;
//               max = Math.max(max, score);
//               i++;
//             }
//           }
//         }
// 
//         String gnuplotpath = MiscUtils.readLineFromCommand("which gnuplot");
//         if (gnuplotpath == null) {
//           System.out.printf("Failed to get gnuplot path\n");
//           System.exit(1);
//         }
// 
//         JavaPlot p = new JavaPlot(gnuplotpath);
//         JavaPlot.getDebugger().setLevel(Debug.ERROR);
//         // JavaPlot.getDebugger().setLevel(Debug.VERBOSE);
//         // p.setTerminal(new PostscriptTerminal("foo.ps"));
// 
//         p.setTitle(app.getFullName());
//         p.setKey(JavaPlot.Key.TOP_RIGHT);
//         p.newGraph3D();
//         p.getAxis("x").setLabel(""+k0.getName());
//         p.getAxis("y").setLabel(""+k1.getName());
// 
//         //// p.set("surface", "");
//         //// p.set("contour", "both");
//         //// p.set("cntrparam", "bspline");
//         //// p.set("hidden3d", "");
// 
//         p.getAxis("x").setBoundaries(k0.minCoordinateValue(), k0.maxCoordinateValue());
//         p.getAxis("y").setBoundaries(k1.minCoordinateValue(), k1.maxCoordinateValue());
//         p.getAxis("z").setBoundaries(0, max);
//         p.addPlot(plot);
//         p.getAxis("x").setBoundaries(k0.minCoordinateValue(), k0.maxCoordinateValue());
//         p.getAxis("y").setBoundaries(k1.minCoordinateValue(), k1.maxCoordinateValue());
//         p.getAxis("z").setBoundaries(0, max);
// 
//         PlotStyle stl2 = ((AbstractPlot) p.getPlots().get(0)).getPlotStyle();
//         // stl2.setStyle(Style.LINES);
//         stl2.setStyle(Style.POINTS);
//         stl2.set("linetype", "palette");
//         stl2.setPointType(4);
// 
//         // If terminal is not set to x11, it will open up a "AquaTerm" window on
//         // some OS-X boxes.
//         // p.set("mouse", "");
//         p.set("terminal", "x11");
//         // p.set("terminal", "pdf");
//         // p.setMultiTitle("All the pretty pictures");
//         // AutoGraphLayout lo = new AutoGraphLayout();
//         // lo.setColumns(2);
//         // lo.setRows(2);
//         // p.getPage().setLayout(lo);
//         p.plot();
//       }
//     }
//   }
// 
//   public static void fooTest(String args[])
//   {
//     boolean totaljunk = false;
//     if (totaljunk) {
//       BufferedReaderIterable templateFile = null;
//       try {
//         int i = 0;
//         templateFile = new BufferedReaderIterable(new FileReader(new File("/Users/ben8/.profile")));
//         for (String line : templateFile) {
//           System.out.printf("%3d: %s\n", i, line);
//           i++;
//         }
//       }
//       catch (FileNotFoundException e) {
//         System.err.printf("Unable to open template file whatever\n");
//         System.exit(1);
//       }
//       
//     }
//     else {
//       try {
//         POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(args[0]));
//         HSSFWorkbook wb = new HSSFWorkbook(fs);
//         HSSFSheet sheet = wb.getSheetAt(0);
//         HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
//         sheet.shiftRows(3,5,2);
// 
//         HSSFRow row = sheet.getRow(3);
//         //HSSFRow row = sheet.createRow(5);
//         HSSFCell cell = row.getCell(3);
//         if (cell == null)
//           cell = row.createCell(3);
//         cell.setCellType(HSSFCell.CELL_TYPE_STRING);
//         cell.setCellValue(new HSSFRichTextString("a test"));
// 
//         // Write the output to a file
//         FileOutputStream fileOut = new FileOutputStream(args[0]);
//         wb.write(fileOut);
//         fileOut.close();
//       }
//       catch (FileNotFoundException e) {
//         System.out.printf("FNF\n");
//         System.exit(1);
//       }
//       catch (IOException e) {
//         System.out.printf("IO\n");
//         System.exit(1);
//       }
//     }
//   }
// 
// }
