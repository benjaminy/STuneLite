/*
 *
 */

// import java.util.EnumSet;
import java.util.Properties;

import java.util.logging.Logger;
// import logging.LogManager;

public class TuningKnobSearchProperties extends LoadableProperties {
  public enum RunMode {
    synthetic,
    mosaicExperiment,
    visualizeAllPoints,
    exhaustiveMosaicSearch,
    distsTest,
    excelTest,
    xmlTest,
    gaussians,
    normalDistTest
  };
  public RunMode runMode = RunMode.synthetic;

  public enum PointFinder {
    randomPoints,
    awesomePoints,
    awesomePointsWithoutFailures
  };
  public PointFinder pointFinder = PointFinder.awesomePoints;
  public boolean scaleFailureProbsHistory = true;
  // public boolean scaleFailureProbsHistory = true;

  public enum DistanceMethod {
    euclidean,
    manhattan
  };
  public DistanceMethod distanceMethod = DistanceMethod.manhattan;

  public enum NeighborhoodMethod {
    neighborhoodMesh,
    neighborhoodNearestK
  };
  public NeighborhoodMethod neighborhoodMethod = NeighborhoodMethod.neighborhoodMesh;

  public int    kForNearestNeighbors = 4;

  public enum CandidateSelectionMethod {
    circleMethod,
    exponentialMethod,
    tournamentMethod,
    takeHighestMethod
  };
  public CandidateSelectionMethod candidateSelectionMethod = CandidateSelectionMethod.takeHighestMethod;

  /** DEPRECATED */
  public int    numInits            = 1;

  /** NOT USED YET */
  public int    parallelRuns        = 1;

  /** The number of candidate points that will be evaluated for each selection */
  public int    numCandidates       = 100;

  /** The total number of points that will be tested for a search */
  public int    totalPoints         = 50;

  /** seed */
  public int    initSeed            = 0;

  /** The number of times a search will be run with different random seeds */
  public int    numSeeds            = 10;

  /** The initial random seed */
  public int    globalSeed          = 42;

  /** A very small number */
  public double epsilon             = 0.00000001;

  /** Number of dimensions in synth app space.  For now this really has to be 2. */
  public int synthAppNumKnobs           = 2;

  /** The size of each dimension in synthetic app spaces */
  public int synthAppNumKnobSettings    = 200;

  /** A shape number for synthetic app mode */
  public int    shape               = 21;

  /** Name of the file that describes the knobs and features of a search */
  public String searchDescFilename  = null;

  /** Name of the file that holds the data for a search */
  public String spreadsheetFilename = null;

  /**  */
  public String expSettingsTemplateFilename = null;

  public String expDirFilename = MiscUtils.readLineFromCommand(new String[]{"experiment_dir"});
  public String expFilesDirFilename       = "ExpSettingsFiles";
  public String outputSpreadsheetDirname  = "TuningKnobResultsSpreadsheets";
  public String allResultsFilenamePrefix  = "allResults";
  public String applicationNameSuffix     = "applicationName";
  public boolean deleteOlderSpreadsheets  = true;
  public String runSprDirFilename         = "RunTkSpr";

  public String defaultLoggerName   = "default";
  public String execLogFilename     = null;

  public boolean doDimensionScaling = true;
  public boolean noNewTesting       = false;

  public boolean printAvgsAcrossSeeds   = true;
  public boolean printScriptOutput      = true;

  public boolean displayPlotsAtEndOfSearch      = true;
  public boolean doPlotFunction                 = false;
  public boolean doPlotPredictions              = false;
  public boolean doPlotMesh                     = false;
  public boolean doEveryPointViz                = false;
  public int     everyPointVizLimit             = 2;

  public boolean multiThreadApps        = true;
  public boolean multiThreadSeeds       = true;

  public double betweenEllipseConst     = 1.20;
  public double derivativeDistFactor    = 0.1;

  public long submitSleepTimeMillis   = 600000;
  public long jobWaitSleepTimeMillis  =  30000;
  public long sleepTimeMillis         =  10000;
  public long shortSleepTimeMillis    =    100;

  /**
   * Indicates whether the DPG is predicate aware.
   */
  // public boolean predicateAwareDPG     = false; 
  
  /**
   * Name of the output generator to use to create
   * the final configuration from a successful mapping.
   */
  // public String configWriter        = "configWriter.MosaicWriter";
  
  /**
   * Name of the file for output verilog of
   * mapped result.
   */
  // public String outputFileName            = null;
  
  /**
   * Colon delimited list of possible include paths.
   */
  // public String includePaths             = "./";
  
  /**
   * Whether or not to run the scheduler.  If this
   * is set to false, schedule data should be provided.
   * A permissive schedule is inferred otherwise.
   */
  // public boolean  doSchedule        = true;

  /**
   * Whether or not to run the placer.  If this is
   * set to false, placement data must be provided.
   */
  // public boolean   doPlace         = true;

  /**
   * Whether or not to run the router.
   *
   */
  // public boolean  doRoute                 = true;
  
  /**
   * Whether or not to visualize the results at the end.
   */
  // public boolean   visualize        = true;

  /**
   * This is set to true if the pre-placement is fixed,
   * false if the placer is allowed to move pre-placed
   * nodes.
   */
  // public boolean fixPrePlacement          = true;
  
  /**
   * This can be used to do a best-effort read of the 
   * pre-schedule and placement data.  By default this is false
   * and an error in the data will throw an exception.
   * By setting this to true, SPR will pre-schedule and 
   * place as much as possible, and do regular scheduling
   * and placement for everything else. 
   */
  // public boolean continueOnPreSchedulePlaceError = false;
  
  /**
   * Minimum II for the Scheduler to start with
   */
  // public int minII = 1;
  
  
  /**
   * Maximum II to try before failing.
   */
  // public int maxII = 40;
  
  /**
   * Specifies the type of data flow graph we are reading.
         */
  // public String dfgStyle = dfgStyleOptions.Default.toString();

  // public enum dfgStyleOptions{
  //   /** default name based mapping */
  //   Default,
  //   /** Original Macah generated */
  //   Macah,
  //   /** Generated by Macah with the new loop controller */
  //   MacahNewLoopController,
  //   /** Macah graph with handling for control network items*/
  //   MacahWithControl,
  //   /** For mapping Macah programs to ADRES based architectures */
  //   MacahToADRESTechMapper, 
  //   /** Placeholder for using externally defined tech mappers */
  //   External};
  
  /** 
   * Number of iterations for SPR to run.
   */
  // public int SPRIterations = 1;
  // 
  //       public enum visMethodEnum{
  //               /** scalable vector graphics output - memory intensive */
  //               SVG,
  //               /** output sutable for visualization in the Electric VLSI CAD system */
  //               Electric, 
  //               /** 
  //                * Output of mapping of operations to device nodes, only including arcs for
  //                * routed values.
  //                */
  //               PlaceNRouteDot,
  //               SVGUnrolled,
  //               SVGTrans,
  //               SVGGI,
  //               SVGHeat,
  //               /** 
  //                * SVG output highlighted with uses at the top level interconnect only.
  //                *  Probably only applicable to early Mosaic (interconnect study)
  //                */
  //               SVGInterconnect,
  //               /** output of the data flow graph after parsing/preprocessing */
  //               DPGDot,
  //               /** output of the data path graph after parsing/preprocessing */
  //               DFGDot};

    
   /**
   * Specifies the method of visualization. This is a semicolon separated list. 
   */
  // public EnumSet<visMethodEnum> visMethod = EnumSet.noneOf(visMethodEnum.class);
  
  /**
   * Specifies a file to write overall statistics to for the run.
   */
  // public String outputStatsFileName = null;

  /**
   * Specifies which debug flags should be set to 1.
   */
  // public String debugDevices="";
      

  /**
   * Time limit in seconds for SPR run, -1 for unlimited;
   */
  // public long timeLimit = -1;

  /**
   * When this is set to true, we will try routing when the placer cost function estimates
   * the placement is unroutable, but the router cost function estimates the placement is routeable.
   */
  // public boolean optimisticallyTryToRoute = false;
  
  /**
   * This option enables resource sharing across mutually exclusive code.
   */
  // public boolean allowMutexSharing = false;
  
  /**
   * Default constructor.  This must call the extractFields after the 
   * super constructor to fill in all parameters from the properties object
   * appropriately
   * @param props
   */
  public TuningKnobSearchProperties(Properties props) {
    super();
    extractFields(props);
  }

  public boolean validate() {
    Logger l = Logger.getLogger(defaultLoggerName);
    boolean propValid = super.validate();
    if (runMode == RunMode.mosaicExperiment) {
      if (searchDescFilename == null) {
        l.severe("No search descriptor filename provided.");
        propValid = false;
      }
      if (spreadsheetFilename == null) {
        l.severe("No spreadsheet filename provided.");
        propValid = false;
      }
      if (expSettingsTemplateFilename == null) {
        l.severe("No expSettings template filename provided.");
        propValid = false;
      }
    }
     return propValid;
  }

}
