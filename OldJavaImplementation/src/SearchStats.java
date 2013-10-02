/*
 *
 */

import java.util.List;
import java.util.Vector;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.locks.*;
import java.util.logging.Logger;

import java.io.PrintWriter;

import com.mallardsoft.tuple.*;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.layout.StripeLayout;
import com.panayotis.gnuplot.layout.AutoGraphLayout;
import com.panayotis.iodebug.Debug;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.plot.DataSetLinePlot;
import com.panayotis.gnuplot.plot.Graph;
import com.panayotis.gnuplot.style.NamedPlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;


public class SearchStats
{
  protected static Lock gnuplotLock = new ReentrantLock();
  TuningKnobSearchProperties props = null;
  public KnobSettingSpace space = null;
  public List<Point> pointsTested = new LinkedList<Point>();

  public SearchStats (TuningKnobSearchProperties p, KnobSettingSpace s)
  {
    assert (p != null);
    assert (s != null);
    props = p;
    space = s;
  }

  static class AC implements Comparator<double[]> {
    public int compare(double[] p1, double[] p2)
    {
      double diff = p2[2] - p1[2];
      return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
    }
  }
  static class AC2 implements Comparator<double[]> {
    public int compare(double[] p1, double[] p2)
    {
      double diff = p1[2] - p2[2];
      return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
    }
  }

  void plotTestedPointsInSpace (SingleSearch search, RealValued f, JavaPlot p)
  {
    p.newGraph();
    int i = 0;
    TuningKnob xKnob = space.get(0);
    TuningKnob yKnob = space.get(1);
    double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
    for (Point pt : pointsTested) {
      Double value = f.getValue(search, pt);
      if (value != null) {
        max = Math.max(value, max);
        min = Math.min(value, min);
      }
    }
    assert(!(min > max));
    boolean rangeTooSmall = max - min < props.epsilon;
    double effMin = rangeTooSmall ? min - props.epsilon : min;
    double effMax = rangeTooSmall ? max + props.epsilon : max;

    double numPointsTested = pointsTested.size();

    for (Point pt : pointsTested) {
      if (f.getValue(search, pt) != null) { i++; }
    }
    double[][] plot2d = new double[i][4];
    int numFailed = pointsTested.size() - i;
    if (false) { System.out.printf("s=%d i=%d f=%d\n", pointsTested.size(), i, numFailed); }
    double[][] failedPoints = new double[numFailed][4];

    i = 0;
    int fCount = 0;

    for (Point pt : pointsTested) {
      Double value = f.getValue(search, pt);
      Object px = pt.get(0);
      Object py = pt.get(1);
      if (value != null) {
        plot2d[i][0] = xKnob.valueToCoordinate(px);
        plot2d[i][1] = yKnob.valueToCoordinate(py);
        // [2] is the size of the point. The good range seems to be [0..5]
        double invPointSize = (value - effMin) / (effMax - effMin);
        // System.out.printf("invPointSize %f\n", invPointSize);
        plot2d[i][2] = (1.05 - invPointSize) * 5.0;
        // [3] is the color of the point. The good range is set by the "cb" axis
        plot2d[i][3] = ((double)(i + fCount)+1);
        i++;
      }
      else {
        failedPoints[fCount][0] = xKnob.valueToCoordinate(px);
        failedPoints[fCount][1] = yKnob.valueToCoordinate(py);
        failedPoints[fCount][2] = 1.0;
        failedPoints[fCount][3] = ((double)(i + fCount)+1);
        fCount++;
      }
    }

    Arrays.sort(plot2d, new AC());

    p.getAxis("x").setBoundaries(xKnob.minCoordinateValue(), xKnob.maxCoordinateValue());
    p.getAxis("y").setBoundaries(yKnob.minCoordinateValue(), yKnob.maxCoordinateValue());
    p.getAxis("cb").setBoundaries(1.0, numPointsTested);
    p.addPlot(plot2d);
    PlotStyle stl = ((AbstractPlot) p.getPlots().get(0)).getPlotStyle();
    stl.setStyle(Style.POINTS);
    stl.set("linetype", "palette");
    stl.setPointType(7);
    stl.set("pointsize", "variable");
    if (numFailed > 0) {
      p.addPlot(failedPoints);
      PlotStyle stl1 = ((AbstractPlot) p.getPlots().get(1)).getPlotStyle();
      stl1.setStyle(Style.POINTS);
      stl1.set("linetype", "palette");
      stl1.setPointType(3);
      stl1.set("pointsize", "variable");
    }
  }

  void plotFunction (SingleSearch search, RealValued f, JavaPlot p)
  {
    assert(space.size() == 2);
    int numValidPoints = 0;
    for (int xOrd = 0; xOrd < space.get(0).numLegalValues(); xOrd++) {
      for (int yOrd = 0; yOrd < space.get(1).numLegalValues(); yOrd++) {
        Point pt = new Point(search.app.getTemplate().getKnobSpace());
        pt.add(space.get(0).ordToValue(xOrd));
        pt.add(space.get(1).ordToValue(yOrd));
        if (f.getValue(search.app, pt) != null) { numValidPoints++; }
      }
    }
    double[][] plot3d = new double[numValidPoints][3];
    double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
    int ptIdx = 0;
    for (int xOrd = 0; xOrd < space.get(0).numLegalValues(); xOrd++) {
      for (int yOrd = 0; yOrd < space.get(1).numLegalValues(); yOrd++) {
        Point pt = new Point(search.app.getTemplate().getKnobSpace());
        Object xVal = space.get(0).ordToValue(xOrd);
        Object yVal = space.get(1).ordToValue(yOrd);
        pt.add(xVal); pt.add(yVal);
        if (false) { System.out.printf("x=%d y=%d pt=%s\n", xOrd, yOrd, pt); }
        Double score = f.getValue(search.app, pt);
        // Double failureFactor = app.getFailureFactor(pt);
        if (score != null /* || failureFactor != null */) {
          // double s = score == null ? -failureFactor : score;
          double s = score == null ? 0.0 : score;
          plot3d[ptIdx][0] = space.get(0).valueToCoordinate(xVal);
          plot3d[ptIdx][1] = space.get(1).valueToCoordinate(yVal);
          plot3d[ptIdx][2] = s;
          max = Math.max(s, max);
          min = Math.min(s, min);
          ptIdx++;
        }
      }
    }
    Arrays.sort(plot3d, new AC2());

    if (min > max) {
      // l.warn("Aborting plotting of feature "+f+".  min > max.");
      System.err.printf("Aborting plotting of feature %s.  min > max.", f);
    }
    else {
      Graph g = p.newGraph3D();
      g.props.set("surface");
      // p.set("surface", "");
      // p.set("contour", "both");
      // p.set("cntrparam", "bspline");
      // p.set("hidden3d", "");
      boolean rangeTooSmall = max - min < props.epsilon;
      double effMin = rangeTooSmall ? min - props.epsilon : min;
      double effMax = rangeTooSmall ? max + props.epsilon : max;
      p.getAxis("z").setBoundaries(effMin, effMax);
      p.getAxis("cb").setBoundaries(effMin, effMax);
      p.addPlot(plot3d);
      PlotStyle stl2 = ((AbstractPlot) p.getPlots().get(0)).getPlotStyle();
      // stl2.setStyle(Style.LINES);
      stl2.setStyle(Style.POINTS);
      stl2.set("linetype", "palette");
      stl2.setPointType(0);
    }
  }

  void plotPredictions (SingleSearch search, RealValued s, JavaPlot p)
  {
    assert(space.size() == 2);
    Set<Point> testedSet = new TreeSet<Point>(pointsTested);
    Mesh foo = new Mesh(props, search, null, testedSet);
    double cov = Stats.coefficientOfVariation(foo.getDistsBetweenNeighborPairs(search.getScalingFactors(s)));

    // // p.set("surface", "");
    // // p.set("contour", "both");
    // // p.set("cntrparam", "bspline");
    // // p.set("hidden3d", "");
    // // FIXME: Must change the size on the next line at some point

    Set<Point> pseudoCandidates = new TreeSet<Point>();
    for (Object x0 : space.get(0)) {
      for (Object x1 : space.get(1)) {
        Point pt = new Point(search.app.getTemplate().getKnobSpace());
        pt.add(x0);
        pt.add(x1);
        if (!testedSet.contains(pt)) {
          pseudoCandidates.add(pt);
        }
      }
    }
    search.makePredictionsForFeature(s, pseudoCandidates, cov);

    double minMean = Double.POSITIVE_INFINITY;
    for (Object x0 : space.get(0)) {
      for (Object x1 : space.get(1)) {
        Point pt = new Point(search.app.getTemplate().getKnobSpace());
        pt.add(x0);
        pt.add(x1);
        if (testedSet.contains(pt)) {
          Double m = s.getValue(search, pt);
          if (m != null) {
            minMean = Math.min(minMean, m);
          }
        }
        else {
          Pair<Double, Double> prediction = s.getPrediction(search, pt);
          minMean = Math.min(minMean, Tuple.get1(prediction));
        }
      }
    }

    int totalSize = space.get(0).numLegalValues() * space.get(1).numLegalValues();
    double[][] means = new double[totalSize][3];
    double[][] stdDevs = new double[totalSize][3];
    double meanMax   = Double.NEGATIVE_INFINITY, meanMin   = Double.POSITIVE_INFINITY;
    double stdDevMax = Double.NEGATIVE_INFINITY, stdDevMin = Double.POSITIVE_INFINITY;

    int i = 0;
    for (Object x0 : space.get(0)) {
      for (Object x1 : space.get(1)) {
        Point pt = new Point(search.app.getTemplate().getKnobSpace());
        pt.add(x0);
        pt.add(x1);
        double mean, stdDev;
        if (testedSet.contains(pt)) {
          Double m = s.getValue(search, pt);
          if (m == null) {
            mean = minMean;
            stdDev = 0.0;
            // mean = Double.NaN;
            // stdDev = Double.NaN;
          }
          else {
            mean = m;
            stdDev = 0;
          }
        }
        else {
          Pair<Double, Double> prediction = s.getPrediction(search, pt);
          mean = Tuple.get1(prediction);
          stdDev = Tuple.get2(prediction);
        }
        means[i][0] = (Double)x0;
        means[i][1] = (Double)x1;
        means[i][2] = mean;
        stdDevs[i][0] = (Double)x0;
        stdDevs[i][1] = (Double)x1;
        stdDevs[i][2] = stdDev;
        meanMax = Math.max(mean, meanMax);
        meanMin = Math.min(mean, meanMin);
        stdDevMax = Math.max(stdDev, stdDevMax);
        stdDevMin = Math.min(stdDev, stdDevMin);
        i++;
      }
    }

    assert(!(meanMin > meanMax));
    assert(!(stdDevMin > stdDevMax));
    boolean meanRangeTooSmall   = meanMax   - meanMin   < props.epsilon;
    boolean stdDevRangeTooSmall = stdDevMax - stdDevMin < props.epsilon;
    double meanEffMin   = meanRangeTooSmall   ? meanMin   - props.epsilon : meanMin;
    double stdDevEffMin = stdDevRangeTooSmall ? stdDevMin - props.epsilon : stdDevMin;
    double meanEffMax   = meanRangeTooSmall   ? meanMax   + props.epsilon : meanMax;
    double stdDevEffMax = stdDevRangeTooSmall ? stdDevMax + props.epsilon : stdDevMax;

    DataSetLinePlot mp = new DataSetLinePlot(space.get(1).numLegalValues(), means);
    DataSetLinePlot vp = new DataSetLinePlot(space.get(1).numLegalValues(), stdDevs);

    // mp.set("style", "pm3d");
    mp.setTitle("aaa");

    boolean do3D = true;
    Graph g;
    if (do3D) { g = p.newGraph3D(); }
    else { g = p.newGraph(); }
    g.props.set("pm3d", "");
    g.props.set("palette", "");
    g.props.set("format", "z");
    // g.props.set("pm3d", "transparent");
    g.props.set("pm3d", "at sb");
    g.unprops.set("surface");
    // g.props.set("linetype", "palette");
    p.getAxis("x").setBoundaries(space.get(0).minCoordinateValue(), space.get(0).maxCoordinateValue());
    p.getAxis("y").setBoundaries(space.get(1).minCoordinateValue(), space.get(1).maxCoordinateValue());
    if (do3D) { p.getAxis("z").setBoundaries(meanEffMin, meanEffMax); }
    p.getAxis("cb").setBoundaries(meanEffMin, meanEffMax);
    p.addPlot(mp);
    // PlotStyle stl2 = ((AbstractPlot) p.getPlots().get(0)).getPlotStyle();
    // stl2.setStyle(Style.LINES);
    // stl2.setStyle(Style.PM3D);
    // stl2.set("at", "b");
    // stl2.set("linetype", "palette");
    // stl2.setPointType(0);

    if (do3D) { p.newGraph3D(); }
    else { p.newGraph(); }
    p.getAxis("x").setBoundaries(space.get(0).minCoordinateValue(), space.get(0).maxCoordinateValue());
    p.getAxis("y").setBoundaries(space.get(1).minCoordinateValue(), space.get(1).maxCoordinateValue());
    if (do3D) { p.getAxis("z").setBoundaries(stdDevEffMin, stdDevEffMax); }
    p.getAxis("cb").setBoundaries(stdDevEffMin, stdDevEffMax);
    p.addPlot(vp);
    // PlotStyle stl3 = ((AbstractPlot) p.getPlots().get(0)).getPlotStyle();
    // stl3.setStyle(Style.LINES);
    // stl3.setStyle(Style.PM3D);
    // stl3.set("linetype", "palette");
    // stl3.setPointType(0);
  }

  void plotMesh (SingleSearch search, RealValued f, JavaPlot p)
  {
    assert(space.size() == 2);
    Collection<Point> haveValue = new LinkedList<Point>();
    for (Point pt : pointsTested) {
      if (f.getValue(search, pt) != null) { haveValue.add(pt); }
    }
    Mesh mesh = new Mesh(props, search, null, haveValue);
    Collection<Pair<Point,Point>> connections = mesh.getAllNeighborPairs();
    double plotData[][] = new double[connections.size() * 2][3];
    System.out.printf("Num connections: %d  Num points tested: %d\n", connections.size(), haveValue.size());
    // double plotData[][] = new double[connections.size() * 2][2];
    double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
    int idx = 0;
    TuningKnob k0 = space.get(0);
    TuningKnob k1 = space.get(1);
    for (Pair<Point,Point> pp : connections) {
      // System.out.printf("%s <-> %s\n", pp.x, pp.y);
      Point x = Tuple.get1(pp);
      Point y = Tuple.get2(pp);
      assert(x.size() == 2);
      assert(y.size() == 2);
      plotData[idx  ][0] = k0.valueToCoordinate(x.get(0));
      plotData[idx  ][1] = k1.valueToCoordinate(x.get(1));
      plotData[idx  ][2] = f.getValue(search, x);
      plotData[idx+1][0] = k0.valueToCoordinate(y.get(0));
      plotData[idx+1][1] = k1.valueToCoordinate(y.get(1));
      plotData[idx+1][2] = f.getValue(search, y);
      max = Math.max(max, f.getValue(search, x));
      max = Math.max(max, f.getValue(search, y));
      min = Math.min(min, f.getValue(search, x));
      min = Math.min(min, f.getValue(search, y));
      idx += 2;
    }
    p.newGraph3D();
    p.getAxis("x").setBoundaries(k0.minCoordinateValue(), k0.maxCoordinateValue());
    p.getAxis("y").setBoundaries(k1.minCoordinateValue(), k1.maxCoordinateValue());
    assert(!(min > max));
    boolean rangeTooSmall = max - min < props.epsilon;
    double effMin = rangeTooSmall ? min - props.epsilon : min;
    double effMax = rangeTooSmall ? max + props.epsilon : max;
    p.getAxis("z").setBoundaries(effMin, effMax);
    p.getAxis("cb").setBoundaries(effMin, effMax);
    DataSetLinePlot lp = new DataSetLinePlot(2, plotData);
    p.addPlot(lp);
    PlotStyle stl2 = ((AbstractPlot) p.getPlots().get(0)).getPlotStyle();
    stl2.setStyle(Style.LINES);
    // stl2.setStyle(Style.POINTS);
    stl2.set("linetype", "palette");
    stl2.setPointType(0);
  }

  void plotTestedPointScores (SingleSearch search, RealValued f, JavaPlot p)
  {
    p.newGraph();
    Collection<Point> haveValue = new LinkedList<Point>();
    for (Point pt : pointsTested) {
      if (f.getValue(search, pt) != null) { haveValue.add(pt); }
    }
    double[][] valsVersusTime = new double[haveValue.size()][2];
    double[][] bestVersusTime = new double[haveValue.size()][2];
    double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
    int i = 0, j = 0;
    for (Point pt : pointsTested) {
      if (false) { System.out.printf("Hallo %s i=%d j=%d\n", f.getValue(search, pt), i, j); }
      Double value = f.getValue(search, pt);
      if (value != null) {
        valsVersusTime[i][0] = j+1.0;
        valsVersusTime[i][1] = value;
        max = Math.max(value, max);
        min = Math.min(value, min);
        bestVersusTime[i][0] = j+1.0;
        bestVersusTime[i][1] = max;
        i++;
      }
      j++;
    }
    p.getAxis("x").setBoundaries(0, j);
    assert(!(min > max));
    boolean rangeTooSmall = max - min < props.epsilon;
    double effMin = rangeTooSmall ? min - props.epsilon : min;
    double effMax = rangeTooSmall ? max + props.epsilon : max;
    p.getAxis("y").setBoundaries(effMin, effMax + 0.3 * (effMax - effMin));
    p.addPlot(valsVersusTime);
    p.addPlot(bestVersusTime);
    PlotStyle stlBest = ((AbstractPlot) p.getPlots().get(1)).getPlotStyle();
    stlBest.setStyle(Style.LINES);
  }

  /* This demo code uses default terminal. Use it as reference for other javaplot arguments  */
  public void defaultTerminal(SingleSearch search, String filename) {
    Map<Feature, JavaPlot> ps = new TreeMap<Feature, JavaPlot>();
    gnuplotLock.lock();
    try {
      String gnuplotpath = MiscUtils.readLineFromCommand(new String[]{"which","gnuplot"});
      if (gnuplotpath == null) {
        System.out.printf("Failed to get gnuplot path\n");
        System.exit(1);
      }

      for (Feature f : search.app.getTemplate().getFeatures()) {
        JavaPlot p = new JavaPlot(gnuplotpath);
        ps.put(f, p);
        p.set("nokey", "");
        p.set("datafile", "missing \"?\"");
        // JavaPlot.getDebugger().setLevel(Debug.ERROR);
        JavaPlot.getDebugger().setLevel(Debug.WARNING);
        // JavaPlot.getDebugger().setLevel(Debug.INFO);
        // JavaPlot.getDebugger().setLevel(Debug.VERBOSE);
        if (filename != null) {
          PostscriptTerminal pTerm = new PostscriptTerminal(filename + ".ps");
          pTerm.setColor(true);
          pTerm.setEPS(false);
          p.setTerminal(pTerm);
        }
        else {
          p.set("terminal", "x11");
        }

        p.setTitle("Stupid Title");
        // p.getAxis("x").setLabel("X axis", "Arial", 20);
        // p.getAxis("y").setLabel("Y axis");

        p.setKey(JavaPlot.Key.TOP_RIGHT);

        // double[][] plot = {{1, 1.1}, {2, 2.2}, {3, 3.3}, {4, 4.3}};
        // DataSetPlot s = new DataSetPlot(plot);
        // p.addPlot(s);
        // p.addPlot("besj0(x)*0.12e1");
        // PlotStyle stl = ((AbstractPlot) p.getPlots().get(1)).getPlotStyle();
        // stl.setStyle(Style.POINTS);
        // stl.setLineType(NamedPlotColor.GOLDENROD);
        // stl.setPointType(5);
        // stl.setPointSize(1);
        // p.addPlot("sin(x)");
        // 
        // p.newGraph();
        // p.addPlot("sin(x)");
      }


      TreeSet<Point> sorted = new TreeSet<Point>(new FeatureComparator(search));
      for (Point t : pointsTested) {
        sorted.add(t);
      }

      // plot function ...
      for (RealValued f : search.app.getTemplate().getPlottingFeatures()) {
        JavaPlot p = ps.get(f);
        boolean plotSearchPointScatter = true;
        if (plotSearchPointScatter) {
          plotTestedPointsInSpace(search, f, p);
        }

        if (props.doPlotFunction) {
          plotFunction(search, f, p);
        }

        if (props.doPlotMesh) {
          plotMesh(search, f, p);
        }

        boolean plotScoresVersusTime = true;
        if (plotScoresVersusTime) {
          plotTestedPointScores(search, f, p);
        }

        if (props.doPlotPredictions && f instanceof RealValued) {
          plotPredictions(search, f, p);
        }

        // If terminal is not set to x11, it will open up a "AquaTerm" window on
        // some OS-X boxes.
        // p.set("mouse", "");
        // p.set("terminal", "pdf");
        // p.set("terminal", "postscript enhanced color");
        p.setMultiTitle("Plots for \"" + search.app.getName() +
          "\" (seed "+search.id + ") Feature: \"" + f.getName() + "\"");
        AutoGraphLayout lo = new AutoGraphLayout();
        int nCols = 0, nRows = 0;
        if (props.doPlotFunction) {
          if (props.doPlotPredictions) {
            nCols = 4; nRows = 3;
          }
          else {
            nCols = 3; nRows = 2;
          }
        }
        else {
          if (props.doPlotPredictions) {
            nCols = 3; nRows = 3;
          }
          else {
            nCols = 2; nRows = 2;
          }
        }

        lo.setColumns(nCols);
        lo.setRows(nRows);
        p.getPage().setLayout(lo);
        p.plot();
      }
    }
    catch (Exception e) {
      System.out.printf("Exception in plotting.  Just keep going\n");
      e.printStackTrace();
    }
    gnuplotLock.unlock();
    // return p;
  }

  public static class FeatureComparator implements Comparator<Point>
  {
    SingleSearch search = null;
    public FeatureComparator(SingleSearch s) { search = s; }
    public int compare(Point p1, Point p2)
    {
      for (Feature f : search.app.getTemplate().getFeatures()) {
        Object v1 = f.getValue(search, p1);
        Object v2 = f.getValue(search, p2);
        if (v1 == null) {
          if (v2 == null) { }
          else {            return -1; }
        }
        else {
          if (v2 == null) { return 1;  }
          else {
            assert (v1 instanceof Comparable && v2 instanceof Comparable);
            Comparable c1 = (Comparable)v1, c2 = (Comparable)v2;
            return c1.compareTo(c2);
          }
        }
      }
      return 0;
    }
  }


  public static void completeAppVisualization(TuningKnobSearchProperties props, Collection<Application> apps)
  {
    for (Application app:apps) {
      AppTemplate template = app.getTemplate();
      Collection<Feature> features = template.getFeatures();
      RealValued performanceFr  = (RealValued)template.getFeature("performanceTheRealOne");
      RealValued dfgSizeFr      = (RealValued)template.getFeature("preSprDfgSize");
      RealValued arraysFr       = (RealValued)template.getFeature("requiredArrays");
      RealValued streamInsFr    = (RealValued)template.getFeature("requiredStreamIns");
      RealValued streamOutsFr   = (RealValued)template.getFeature("requiredStreamOuts");
      RealValued recIIFr        = (RealValued)template.getFeature("recII");
      RealValued resIIFr        = (RealValued)template.getFeature("resII");
      RealValued grpIIFr        = (RealValued)template.getFeature("grpII");
      RealValued schedIIFr      = (RealValued)template.getFeature("schedII");
      Failure sprTimedOutFa     = (Failure)template.getFeature("sprTimedOut");
      Failure maxIterPlacerFa   = (Failure)template.getFeature("maxIterPlacer");
      Failure maxIIFa           = (Failure)template.getFeature("maxIIFailure");
      Failure tooManyStatefulFa = (Failure)template.getFeature("tooManyStatefulFailure");
      Failure congestionFa      = (Failure)template.getFeature("congestionFailure");

      KnobSettingSpace space = template.getKnobSpace();
      assert(space.size() == 2);
      TuningKnob xKnob = space.get(0);
      TuningKnob yKnob = space.get(1);

      Set<Point> allPts         = new TreeSet<Point>();
      Set<Point> performancePts = new TreeSet<Point>();
      Set<Point> dfgSizePts     = new TreeSet<Point>();
      Set<Point> arraysPts      = new TreeSet<Point>();
      Set<Point> streamInsPts   = new TreeSet<Point>();
      Set<Point> streamOutsPts  = new TreeSet<Point>();
      Set<Point> iiPts          = new TreeSet<Point>();
      int[][] failureCode = new int[xKnob.numLegalValues()][yKnob.numLegalValues()];
      for (int xOrd = 0; xOrd < xKnob.numLegalValues(); xOrd++) {
        Object xVal = xKnob.ordToValue(xOrd);
        for (int yOrd = 0; yOrd < yKnob.numLegalValues(); yOrd++) {
          Object yVal = yKnob.ordToValue(yOrd);
          Point pt = new Point(space);
          pt.add(xVal); pt.add(yVal);
          if (app.hasCompletedTesting(pt)) {
            allPts.add(pt);
            Double performanceVal = performanceFr.getValue(app, pt);
            if (performanceVal != null) { performancePts.add(pt); }
            Double dfgSizeVal     = dfgSizeFr.getValue(app, pt);
            if (dfgSizeVal != null) { dfgSizePts.add(pt); }
            Double arraysVal      = arraysFr.getValue(app, pt);
            if (arraysVal != null) { arraysPts.add(pt); }
            Double streamInsVal   = streamInsFr.getValue(app, pt);
            if (streamInsVal != null) { streamInsPts.add(pt); }
            Double streamOutsVal  = streamOutsFr.getValue(app, pt);
            if (streamOutsVal != null) { streamOutsPts.add(pt); }
            Double recIIVal       = recIIFr.getValue(app, pt);
            Double resIIVal       = resIIFr.getValue(app, pt);
            Double grpIIVal       = grpIIFr.getValue(app, pt);
            Double schedIIVal     = schedIIFr.getValue(app, pt);
            if (recIIVal != null || resIIVal != null || grpIIVal != null || schedIIVal != null)
              { streamOutsPts.add(pt); }
            Boolean sprTimedOutVal      = sprTimedOutFa.getValue(app, pt);
            Boolean maxIterPlacerVal    = maxIterPlacerFa.getValue(app, pt);
            Boolean maxIIVal            = maxIIFa.getValue(app, pt);
            Boolean tooManyStatefulVal  = tooManyStatefulFa.getValue(app, pt);
            Boolean congestionVal       = congestionFa.getValue(app, pt);
            if (sprTimedOutVal != null && sprTimedOutVal || maxIterPlacerVal != null && maxIterPlacerVal)
              { failureCode[xOrd][yOrd] = 2; } // 2 = "too big"
            else if (maxIIVal != null && maxIIVal)
              { failureCode[xOrd][yOrd] = 3; } // 3 = "max II"
            else if (tooManyStatefulVal != null && tooManyStatefulVal)
              { failureCode[xOrd][yOrd] = 4; } // 4 = "too many stateful"
            else if (sprTimedOutVal != null && maxIterPlacerVal != null && maxIIVal != null &&
                      tooManyStatefulVal != null && congestionVal != null &&
                      (!sprTimedOutVal) && (!maxIterPlacerVal) && (!maxIIVal) && (!tooManyStatefulVal) && (!congestionVal))
              { failureCode[xOrd][yOrd] = 0; } // 0 = "success"
            else
              { failureCode[xOrd][yOrd] = 5; } // 5 = "weird"
          }
          else {
            failureCode[xOrd][yOrd] = 1; // 1 = "not tested"
          }
        }
      }

      for (int failCode = 1; failCode <= 5; failCode++) {
        PrintWriter fileOut = null;
        try { fileOut = new PrintWriter("app_"+app.getName()+"_fail_"+failCode+".dat"); }
        catch (java.io.FileNotFoundException e) {
          e.printStackTrace();
          Logger l = Logger.getLogger(props.defaultLoggerName);
          l.severe("blah");
          System.exit(1);
        }
        for (int xOrd = 0; xOrd < xKnob.numLegalValues(); xOrd++) {
          for (int yOrd = 0; yOrd < yKnob.numLegalValues(); yOrd++) {
            if (failureCode[xOrd][yOrd] == failCode) {
              if (true) {
                double xCoord = xKnob.valueToCoordinate(xKnob.ordToValue(xOrd));
                double yCoord = yKnob.valueToCoordinate(yKnob.ordToValue(yOrd));
                fileOut.printf("%f %f 0\n", xCoord, yCoord);
              }
              if (false) {
                fileOut.printf("%d %d\n", xOrd, yOrd);
                fileOut.printf("%d %d\n", xOrd, yOrd + 1);
                fileOut.printf("%d %d\n", xOrd + 1, yOrd + 1);
                fileOut.printf("%d %d\n", xOrd + 1, yOrd);
                fileOut.printf("%d %d\n", xOrd, yOrd);
              }
              fileOut.printf("\n");
              fileOut.printf("\n");
            }
          }
        }
        fileOut.close();
      }

      completeAppFeature(props, app, space, performanceFr,  allPts);
      completeAppFeature(props, app, space, dfgSizeFr,      allPts);
      completeAppFeature(props, app, space, arraysFr,       allPts);
      completeAppFeature(props, app, space, streamInsFr,    allPts);
      completeAppFeature(props, app, space, streamOutsFr,   allPts);
      RealValued [] iiFrs = {recIIFr, resIIFr, grpIIFr, schedIIFr};
      completeAppFeature(props, app, space, iiFrs,   allPts);
    }
  }

  public static void completeAppFeature(TuningKnobSearchProperties props, Application app, KnobSettingSpace space,
    RealValued feature, Collection<Point> points)
  {
    RealValued [] fs = { feature };
    completeAppFeature(props, app, space, fs, points);
  }

  public static void completeAppFeature(TuningKnobSearchProperties props, Application app, KnobSettingSpace space,
    RealValued [] features, Collection<Point> points)
  {
    TuningKnob xKnob = space.get(0);
    TuningKnob yKnob = space.get(1);
    Vector<Double> scaling = space.unitScalingFactors();

    double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
    for (Point pt1 : points) {
      Double val = null;
      for (RealValued feature : features) {
        Double val2 = feature.getValue(app, pt1);
        if (val2 != null) {
          val = val == null ? val2 : Math.max(val, val2);
        }
      }
      if (val != null) {
        min = Math.min(val, min);
        max = Math.max(val, max);
      }
    }

    if (false) {
      PrintWriter fileOut = null;
      try {
        String frNames = "";
        for (RealValued feature : features) {
          frNames += "_" + feature.getName();
        }
        fileOut = new PrintWriter("app_"+app.getName()+"_fr"+frNames+".dat");
      }
      catch (java.io.FileNotFoundException e) {
        e.printStackTrace();
        Logger l = Logger.getLogger(props.defaultLoggerName);
        l.severe("blah");
        System.exit(1);
      }
      double scale = max - min;
      for (Point pt : points) {
        // double xCoord = xKnob.valueToCoordinate(pt.get(0));
        // double yCoord = xKnob.valueToCoordinate(pt.get(1));
        // double val = (feature.getValue(app, pt) - min) / scale;
        // fileOut.printf("%f %f %f\n", xCoord, yCoord, val);
        // fileOut.printf("%f %f %f\n", xCoord, yCoord + 1, val);
        // fileOut.printf("%f %f %f\n", xCoord + 1, yCoord + 1, val);
        // fileOut.printf("%f %f %f\n", xCoord + 1, yCoord, val);
        // fileOut.printf("%f %f %f\n", xCoord, yCoord, val);
        // fileOut.printf("\n");
        // fileOut.printf("\n");
      }
      fileOut.close();
    }
    else {
      Collection<Pair<Point,Point>> neighbors = new LinkedList<Pair<Point,Point>>();
      
      if (false) {
        for (Point pt1 : points) {
          // double val = feature.getValue(app, pt1);
          // Point nPt = null, sPt = null, ePt = null, wPt = null;
          // double nDist = Double.POSITIVE_INFINITY, sDist = Double.POSITIVE_INFINITY, eDist = Double.POSITIVE_INFINITY, wDist = Double.POSITIVE_INFINITY;
          // double xCoord1 = xKnob.valueToCoordinate(pt1.get(0));
          // double yCoord1 = xKnob.valueToCoordinate(pt1.get(1));
          // for (Point pt2 : points) {
          //   if (pt1.compareTo(pt2) != 0) {
          //     double dist = space.distance(scaling, pt1, pt2);
          //     double xCoord2 = xKnob.valueToCoordinate(pt2.get(0));
          //     double yCoord2 = xKnob.valueToCoordinate(pt2.get(1));
          //     double xDiff = xCoord2 - xCoord1;
          //     double yDiff = yCoord2 - yCoord1;
          //     double xDiffAbs = Math.abs(xDiff);
          //     double yDiffAbs = Math.abs(yDiff);
          //     if (xDiffAbs > yDiffAbs) {
          //       if (xDiff > 0) {
          //         if (dist < wDist) { wPt = pt2; wDist = dist; }
          //       }
          //       else {
          //         if (dist < eDist) { ePt = pt2; eDist = dist; }
          //       }
          //     }
          //     else {
          //       if (yDiff > 0) {
          //         if (dist < nDist) { nPt = pt2; nDist = dist; }
          //       }
          //       else {
          //         if (dist < sDist) { sPt = pt2; sDist = dist; }
          //       }
          //     }
          //   }
          // }
          // if (nPt != null) { neighbors.add(Tuple.from(pt1, nPt)); }
          // if (sPt != null) { neighbors.add(Tuple.from(pt1, sPt)); }
          // if (ePt != null) { neighbors.add(Tuple.from(pt1, ePt)); }
          // if (wPt != null) { neighbors.add(Tuple.from(pt1, wPt)); }
        }
      }
      
      PrintWriter fileOut = null;
      try {
        String frNames = "";
        for (RealValued feature : features) {
          frNames += "_" + feature.getName();
        }
        fileOut = new PrintWriter("app_"+app.getName()+"_fr"+frNames+".dat");
      }
      catch (java.io.FileNotFoundException e) {
        e.printStackTrace();
        Logger l = Logger.getLogger(props.defaultLoggerName);
        l.severe("blah");
        System.exit(1);
      }
      double scale = max - min;
      if (true) {
        for (Point pt : points) {
          double xCoord = xKnob.valueToCoordinate(pt.get(0));
          double yCoord = xKnob.valueToCoordinate(pt.get(1));
          Double val = null;
          for (RealValued feature : features) {
            Double val2 = feature.getValue(app, pt);
            if (val2 != null) {
              val = val == null ? val2 : Math.max(val, val2);
            }
          }
          if (val != null) {
            double effVal = (max - min > props.epsilon) ? (val - min) / scale : 0.5;
            fileOut.printf("%f %f %f\n", xCoord, yCoord, effVal);
            fileOut.printf("\n");
            fileOut.printf("\n");
          }
        }
      }
      else {
        for (Pair<Point,Point> line : neighbors) {
          // Point pt1 = Tuple.get1(line);
          // Point pt2 = Tuple.get2(line);
          // double xCoord1 = xKnob.valueToCoordinate(pt1.get(0));
          // double yCoord1 = xKnob.valueToCoordinate(pt1.get(1));
          // double xCoord2 = xKnob.valueToCoordinate(pt2.get(0));
          // double yCoord2 = xKnob.valueToCoordinate(pt2.get(1));
          // fileOut.printf("%f %f %f\n", xCoord1, yCoord1, (feature.getValue(app, pt1) - min) / scale);
          // fileOut.printf("%f %f %f\n", xCoord2, yCoord2, (feature.getValue(app, pt2) - min) / scale);
          // fileOut.printf("\n");
          // fileOut.printf("\n");
        }
      }
      fileOut.close();
    }
  }
}
