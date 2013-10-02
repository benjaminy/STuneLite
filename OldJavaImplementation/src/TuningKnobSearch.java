/*
 *
 */

import java.util.Iterator;
import java.util.Vector;
import java.util.Set;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.logging.Logger;

public class TuningKnobSearch
{
  TuningKnobSearchProperties props = null;
  SearchStats stats[] = null;
  Logger l = null;

  public TuningKnobSearch(TuningKnobSearchProperties p)
  {
    assert (p != null);
    props = p;
    l = Logger.getLogger(props.defaultLoggerName);
  }

  private class EvaluateApp implements Runnable
  {
    private Application app = null;
    public EvaluateApp(Application a)
    {
      assert (a != null);
      app = a;
    }
    public void run()
    {
      stats = new SearchStats[props.numSeeds];
      KnobSettingSpace knobs = app.getTemplate().getKnobSpace();
      for (int seed = props.initSeed; seed < props.initSeed + props.numSeeds; seed++) {
        SearchStats whyDoesJavaHateMe = new SearchStats(props, knobs);
        if (whyDoesJavaHateMe == null) {
          l.severe ("Can't a guy get a 'new' to work?");
          System.exit(1);
        }
        stats[seed - props.initSeed] = whyDoesJavaHateMe;
        if (stats[seed - props.initSeed] == null) {
          l.severe ("stats should not be null");
          System.exit(1);
        }
      }
      app.doSearches(stats);

      // Compute some averages and cumulative whatnots
      // double bests[] = new double[props.numSeeds];
      // for (int i = 0; i < props.numSeeds; i++) {
      //   bests[i] = Double.NaN;
      // }
      // // double mins[] = new double[numSeeds];
      // // double maxs[] = new double[numSeeds];
      // for (int j = 0; j < props.numSeeds; j++) bests[j] = 0.0;
      // 
      // Iterator<Point> pts[] = new Iterator<Point>[props.numSeeds];
      // for (int i = 0; i < props.numSeeds; i++) {
      //   pts.add(i, stats[i].pointsTested.iterator());
      // }
      // 
      // if (props.printAvgsAcrossSeeds) {
      //   for (int i = 0; i < props.totalPoints; i++) {
      //     for (RealValued f : app.getTemplate().getPlottingFeatures()) {
      //       double avg = 0, min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
      // 
      //       int numPtsWithValues = 0;
      //       for (int j = 0; j < numSeeds; j++) {
      //         // System.out.printf("%3.1f", stats.value[j][i]);
      //         if (pts[j].hasNext()) {
      //           Point pt = pts[j].next();
      //           Double value = f.getValue(app, pt);
      //           if (value != null) {
      //             bests[j] = Math.max(value, bests[j]);
      //             avg += bests[j];
      //             min = Math.min(bests[j], min);
      //             max = Math.max(bests[j], max);
      //             numPtsWithValues++;
      //           }
      //         }
      //         if (value != null) {
      //           bests[j] = Math.max(value, bests[j]);
      //           avg += bests[j];
      //           min = Math.min(bests[j], min);
      //           max = Math.max(bests[j], max);
      //           numPtsWithValues++;
      //         }
      //       }
      //       // System.out.printf("\n");
      //       if (numPtsWithValues > 0) {
      //         avg /= ((double)numPtsWithValues);
      //       }
      //       // System.out.printf("[%3d] %3.1f %3.1f %3.1f\n", i, min, avg, max);
      //       System.out.printf("%3.1f \t%3.1f \t%3.1f\n", min, avg, max);
      //     }
      //   }
      // }

      // Let's display some visualizations
      boolean doViz = false;
      if (doViz) {
        for (int i = 0; i < props.numSeeds; i++) {
          boolean doEPS = false;
          // FIXME needs "search"
          // if (doEPS)
          //   stats[i].defaultTerminal(app, "final_"+props.totalPoints+"_"+i);
          // else
          //   stats[i].defaultTerminal(app, null);
        }
      }
    }
  }

  void setup(String args[])
  {
    l = Logger.getLogger(props.defaultLoggerName);

    TuningKnobSystemInterface si = null;

    switch (props.runMode) {
      case synthetic:
        { si = new SyntheticSystemInterface(props); break; }
      case mosaicExperiment:
      case visualizeAllPoints:
      case exhaustiveMosaicSearch:
      {
        Set<MosaicApp> apps = new TreeSet<MosaicApp>();
        new SearchDescriptor(props, apps);
        si = new MosaicSystemInterface(props, apps);
        for (MosaicApp app : apps) {
          app.setSystemInterface((MosaicSystemInterface)si);
          if (true) {System.out.printf("app (%s)  arch (%s)\n", app, app.getArchitecture());}
        }
        break;
      }
      default:
        l.severe("Unknown run mode!");
        throw new IllegalArgumentException();
    }

    Collection<Application> apps = si.getApps();

    switch (props.runMode) {
      case synthetic:
      case mosaicExperiment:
      {
        if (props.multiThreadApps) {
          Collection<Thread> threads = new LinkedList<Thread>();

          for (Application app : apps) {
            Thread t = new Thread(new EvaluateApp(app));
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
          for (Application app : apps) {
            (new EvaluateApp(app)).run();
          }
        }

        // if (sSource instanceof FileScoreSource) {
        //   ((FileScoreSource) sSource).writeNewEstimates();
        // }
        break;
      }
      case exhaustiveMosaicSearch:
      {
        ((MosaicSystemInterface)si).exhaustiveSearch();
        break;
      }
      case visualizeAllPoints:
      {
        SearchStats.completeAppVisualization(props, apps);
        break;
      }
      default:
        l.severe("Unknown run mode!");
        throw new IllegalArgumentException();
    }
  }

  public static void main(String args[])
  {
    Properties cmdLineParams = new Properties();
    TuningKnobSearchProperties props = new TuningKnobSearchProperties(cmdLineParams);
    Logger l = Logger.getLogger(props.defaultLoggerName);
    if (args.length > 0) {
      l.info("Trying to parse file " + args[0] + " as command-line parameters");
      InputStream i = null;
      try {
        i = new FileInputStream(args[0]);
        try {
          cmdLineParams.load(i);
        }
        catch (java.io.IOException e) {
          System.out.printf("Trying to load parameters.  Unable to parse file %s\n", args[0]);
          cmdLineParams = new Properties();
        }
      }
      catch (java.io.FileNotFoundException e) {
        System.out.printf("Trying to load parameters.  Unable to find file %s\n", args[0]);
      }
    }
    props = new TuningKnobSearchProperties(cmdLineParams);

    switch (props.runMode) {
      case excelTest:
        MosaicSystemInterface.fooTest(args);
        break;
      case distsTest:
        Stats.distsTest();
        break;
      case xmlTest:
        SearchDescriptor.xmlTest(props, args);
        break;
      case gaussians:
        Stats.messingAroundWithGaussians(args);
        break;
      case normalDistTest:
        Stats.normalDistTest();
        break;
      case synthetic:
      case mosaicExperiment:
      case visualizeAllPoints:
      {
        TuningKnobSearch s = new TuningKnobSearch(props);
        s.setup(args);
        break;
      }
      default:
        System.out.printf("Unknown mode\n");
    }
  }
}