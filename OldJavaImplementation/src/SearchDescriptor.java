/*
 *
 */

// import javax.xml.parsers.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import com.mallardsoft.tuple.*;

public class SearchDescriptor {
  TuningKnobSearchProperties props = null;
  Logger l = null;

  public SearchDescriptor (TuningKnobSearchProperties p, Set<MosaicApp> apps)
  {
    assert (p != null);
    props = p;
    l = Logger.getLogger(props.defaultLoggerName);

    javax.xml.parsers.SAXParser parser = null;
    try {
      parser = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser();
    }
    catch (javax.xml.parsers.ParserConfigurationException e) {
      System.out.printf("Parser configuration exception?  huh?\n");
      assert(false);
    }
    catch (org.xml.sax.SAXException e) {
      System.out.printf("SAX exception?  huh?\n");
      assert(false);
    }

    TuningKnobDescHandler handler = new TuningKnobDescHandler(props, apps);
    java.io.File f = new java.io.File(props.searchDescFilename);
    try {
      parser.parse(f, handler);
    }
    catch (org.xml.sax.SAXException e) {
      System.out.printf("SAX exception during parsing?  huh?\n");
      e.printStackTrace();
      System.exit(1);
    }
    catch (java.io.IOException e) {
      l.severe("IO exception while trying to parse file \""+props.searchDescFilename+"\"");
      e.printStackTrace();
      System.exit(1);
    }

    
  }

  private static class ParserState
  {
    private int s = 0;
    public static final int init          = 1;
    public static final int archs         = 2;
    public static final int arch          = 3;
    public static final int appTemplates  = 4;
    public static final int appTemplate   = 5;
    public static final int feature       = 6;
    public static final int knob          = 7;
    public static final int apps          = 8;

    public ParserState() { s = init; }
    public int getState() { return s; }
    public void setState(int t)
    {
      if (t == init || t == archs || t == arch || t == appTemplates || t == appTemplate || t == feature || t == knob ||
        t == apps)
        s = t;
      else {
        assert(false);
      }
    }
    public String toString()
    {
      switch (s)
      {
        case init:          return "init";
        case archs:         return "archs";
        case arch:          return "arch";
        case appTemplates:  return "appTemplates";
        case appTemplate:   return "appTemplate";
        case feature:       return "feature";
        case knob:          return "knob";
        case apps:          return "apps";
        default: assert(false); return "";
      }
    }
  }

  /* tuning knob search descriptors */
  private static class TuningKnobDescHandler extends org.xml.sax.helpers.DefaultHandler {
    TuningKnobSearchProperties props = null;
    Logger l = null;
    Set<MosaicApp> apps;
    private ParserState state = new ParserState();
    private String expName = null, name = null, fname = null, kname = null;

    private Map<String, AppTemplate> templates = null;
    private Map<String, Architecture> architectures = null;
    private Set<MosaicApp> applications = null;

    private Map<String, String> settings = null;
    private KnobSettingSpace space = null;
    private Map<String, Feature> features = null;
    private Set<RealValued> plotting = null;
    private Set<RealValued> directPredict = null;
    private Objective objective = null;
    private Collection<Triple<Feature<Feature, Feature>, String, String>> aggregates = null;

    public TuningKnobDescHandler(TuningKnobSearchProperties p, Set<MosaicApp> a)
    {
      assert (p != null);
      assert (a != null);
      props = p;
      apps = a;
      l = Logger.getLogger(props.defaultLoggerName);
    }

    public void startDocument() {
    }


    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attrs) {
      System.out.printf("start: (state: %s) (uri: %s) (localName: %s) (qName: %s)\n",
        state, uri, localName, qName);
      for (int idx = 0; idx < attrs.getLength(); idx++) {
        System.out.printf("    attr: (%d) (local: %s) (q: %s) (t: %s) (uri: %s) (v: %s)\n",
          idx, attrs.getLocalName(idx), attrs.getQName(idx), attrs.getType(idx), attrs.getURI(idx), attrs.getValue(idx));
      }
      switch (state.getState())
      {
        case ParserState.init:
          if (qName.equals("tuning_knob_search_set")) {
            if (attrs.getQName(0).equals("name")) {
              expName = new String(attrs.getValue(0));
              architectures = new HashMap<String, Architecture>();
              templates = new HashMap<String, AppTemplate>();
              state.setState(ParserState.archs);
            }
            else { assert(false); }
          }
          else { assert(false); }
          break;

        case ParserState.archs:
        {
          if (qName.equals("architecture")) {
            if (attrs.getQName(0).equals("name") && attrs.getQName(1).equals("clusterName") &&
                attrs.getQName(2).equals("numberOfClusters") && attrs.getQName(3).equals("numberOfMems")) {
              name = new String(attrs.getValue(0));
              String clusterName = new String(attrs.getValue(1));
              int numberOfClusters = Integer.parseInt(attrs.getValue(2));
              int numberOfMems = Integer.parseInt(attrs.getValue(3));
              architectures.put(name, new Architecture(clusterName, numberOfClusters, numberOfMems));
              state.setState(ParserState.archs);
            }
            else {
              assert (false);
            }
          }
          else if (qName.equals("applicationTemplate")) {
            state.setState(ParserState.appTemplates);
            startElement(uri, localName, qName, attrs);
          }
          else {
            assert (false);
          }
          break;
        }

        case ParserState.appTemplates:
          if (qName.equals("applicationTemplate")) {
            if (attrs.getQName(0).equals("name")) {
              name = new String(attrs.getValue(0));
              settings = new HashMap<String, String>();
              space = new KnobSettingSpace(props, 2);
              features = new HashMap<String, Feature>();
              plotting = new TreeSet<RealValued>();
              directPredict = new TreeSet<RealValued>();
              aggregates = new LinkedList<Triple<Feature<Feature, Feature>, String, String>>();

              state.setState(ParserState.appTemplate);
            }
            else { assert(false); }
          }
          else if (qName.equals("application")) {
            state.setState(ParserState.apps);
            startElement(uri, localName, qName, attrs);
          }
          else {
            l.severe("Unknown element name: "+qName);
            assert(false);
          }
          break;

        case ParserState.appTemplate:
          if (qName.equals("knob")) {
            if (attrs.getQName(0).equals("name")) {
              kname = new String(attrs.getValue(0));
              state.setState(ParserState.knob);
            }
            else { assert(false); }
          }
          else if (qName.equals("feature")) {
            if (attrs.getQName(0).equals("name")) {
              fname = new String(attrs.getValue(0));
              state.setState(ParserState.feature);
            }
            else { assert(false); }
          }
          else if (qName.equals("objective")) {
            if (attrs.getQName(0).equals("feature") && attrs.getQName(1).equals("failure")) {
              if (objective != null) {
                l.severe("Multiple objectives in an application template");
                assert (false);
              }
              BooleanValued e = (BooleanValued)features.get(attrs.getValue(0));
              BooleanValued a = (BooleanValued)features.get(attrs.getValue(1));
              if (e == null) {
                l.severe("Objective refers to nonexistent feature");
                assert (false);
              }
              if (a == null) {
                l.severe("Objective refers to nonexistent failure");
                assert (false);
              }
              assert (a != null);
              objective = new Objective(e, a);
            }
            else {
              l.severe("Bad objective");
              assert(false);
            }
          }
          else if (qName.equals("plotting")) {
            if (attrs.getQName(0).equals("feature")) {
              RealValued f = (RealValued)features.get(attrs.getValue(0));
              plotting.add(f);
            }
            else { assert(false); }
          }
          else if (qName.equals("predict")) {
            if (attrs.getQName(0).equals("feature")) {
              RealValued f = (RealValued)features.get(attrs.getValue(0));
              directPredict.add(f);
            }
            else { assert(false); }
          }
          else if (qName.equals("setting")) {
            if (attrs.getQName(0).equals("name") && attrs.getQName(1).equals("value")) {
              String name = attrs.getValue(0);
              String value = attrs.getValue(1);
              assert (!settings.containsKey(name));
              settings.put(name, value);
            }
            else { assert(false); }
          }
          else if (qName.equals("include")) {
            if (attrs.getQName(0).equals("template")) {
              AppTemplate t = (AppTemplate)templates.get(attrs.getValue(0));
              for (Feature f : t.getFeatures()) {
                assert (!features.containsKey(f.getName()));
                features.put(f.getName(), f);
              }
              if (t.getObjective() != null) {
                objective = t.getObjective();
              }

              // maybe do something about the knob space

              for (RealValued f : t.getPlottingFeatures()) {
                assert (!plotting.contains(f));
                plotting.add(f);
              }
              for (RealValued f : t.getDirectPredictFeatures()) {
                assert (!directPredict.contains(f));
                directPredict.add(f);
              }
            }
            else { assert(false); }
          }
          else {
            l.severe("Unknown element name: "+qName);
            assert(false);
          }
          break;

        case ParserState.knob:
          TuningKnob k = null;
          if (qName.equals("discrete")) {
            if (attrs.getQName(0).equals("min") && attrs.getQName(1).equals("max")) {
              int min = Integer.parseInt(attrs.getValue(0));
              int max = Integer.parseInt(attrs.getValue(1));
              assert(min <= max);
              k = new IntTuningKnob(props, new String(kname), min, max, -1);
            }
            else { assert(false); }
          }
          else { assert(false); }
          assert(k != null);
          features.put(new String(kname), new KnobConstant(k, new String(kname)));
          space.add(k);
          state.setState(ParserState.appTemplate);
          kname = null;
          break;

        case ParserState.feature:
          Feature f = null;
          if (qName.equals("sensor")) {
            if (attrs.getQName(0).equals("key")) {
              f = new Sensor(new String(fname), new String(attrs.getValue(0)));
            }
            else { assert(false); }
          }
          else if (qName.equals("algebraic")) {
            if (attrs.getQName(0).equals("op")) {
              String op = attrs.getValue(0);
              if (op.equals("not")) {
                if (attrs.getQName(1).equals("in")) {
                  BooleanValued in = null;
                  try { in = (BooleanValued) features.get(attrs.getValue(1)); }
                  catch (ClassCastException e) { assert(false); }
                  if (in == null) {
                    l.severe("Missing feature \""+attrs.getValue(1)+"\"");
                    assert (false);
                  }
                  f = new NotFeature(in, new String(fname));
                }
                else { assert(false); }
              }
              else if (op.equals("negate")) {
                if (attrs.getQName(1).equals("in")) {
                  RealValued in = null;
                  try { in = (RealValued) features.get(attrs.getValue(1)); }
                  catch (ClassCastException e) { assert(false); }
                  f = new Negate(in, new String(fname));
                }
                else { assert(false); }
              }
              else {
                if (attrs.getQName(1).equals("inL") && attrs.getQName(2).equals("inR")) {
                  Feature inL = features.get(attrs.getValue(1));
                  Feature inR = features.get(attrs.getValue(2));
                  if (inL == null) {
                    l.severe("Missing feature \""+attrs.getValue(1)+"\"");
                    assert (false);
                  }
                  if (inR == null) {
                    l.severe("Missing feature \""+attrs.getValue(2)+"\"");
                    assert (false);
                  }
                  try {
                    if      (op.equals("add"))
                      { f = new Add           ((RealValued)   inL, (RealValued)   inR, new String(fname)); }
                    else if (op.equals("subtract"))
                      { f = new Subtract      ((RealValued)   inL, (RealValued)   inR, new String(fname)); }
                    else if (op.equals("multiply"))
                      { f = new Multiply      ((RealValued)   inL, (RealValued)   inR, new String(fname)); }
                    else if (op.equals("divide"))
                      { f = new Divide        ((RealValued)   inL, (RealValued)   inR, new String(fname)); }
                    else if (op.equals("and"))
                      { f = new AndFeature    (props, (BooleanValued)inL, (BooleanValued)inR, new String(fname)); }
                    else if (op.equals("or"))
                      { f = new OrFeature     (props, (BooleanValued)inL, (BooleanValued)inR, new String(fname)); }
                    else if (op.equals("andGame"))
                      { f = new AndGameFeature(props, (BooleanValued)inL, (BooleanValued)inR, new String(fname)); }
                    else if (op.equals("andGameTrueBias"))
                      { f = new AndGameTrueBias ((BooleanValued)inL, (BooleanValued)inR, new String(fname)); }
                    else if (op.equals("orGame"))
                      { f = new OrGameFeature (props, (BooleanValued)inL, (BooleanValued)inR, new String(fname)); }
                    else if (op.equals("orGameFalseBias"))
                      { f = new OrGameFalseBias ((BooleanValued)inL, (BooleanValued)inR, new String(fname)); }
                    else if (op.equals("orMutex"))
                      { f = new OrMutexFeature((BooleanValued)inL, (BooleanValued)inR, new String(fname)); }
                    else if (op.equals("xor"))
                      { f = new XorFeature    (props, (BooleanValued)inL, (BooleanValued)inR, new String(fname)); }
                    else if (op.equals("implies"))
                      { f = new ImpliesFeature((BooleanValued)inL, (BooleanValued)inR, new String(fname)); }
                    else if (op.equals("lessThan"))
                      { f = new LessThan      (props, (RealValued)   inL, (RealValued)   inR, new String(fname)); }
                    else if (op.equals("greaterThan"))
                      { f = new GreaterThan   (props, (RealValued)   inL, (RealValued)   inR, new String(fname)); }
                    else {
                      l.severe("Unknown op: "+op);
                      assert(false);
                    }
                  }
                  catch (ClassCastException e) { assert(false); }
                }
              }
              // Sensor s = new Sensor(fname, new String());
              // features.put(s);
              // state.setState(ParserState.application);
            }
            else { assert(false); }
          }
          else if (qName.equals("aggregate")) {
            if (attrs.getQName(0).equals("op") && attrs.getQName(1).equals("feature") && attrs.getQName(2).equals("filter")) {
              String op = attrs.getValue(0);
              if (op.equals("sum"))
                { f = new RealAggregate(RealAggregate.Operator.sum, null, null, new String(fname)); }
              else if (op.equals("product"))
                { f = new RealAggregate(RealAggregate.Operator.product, null, null, new String(fname)); }
              else if (op.equals("max"))
                { f = new RealAggregate(RealAggregate.Operator.max, null, null, new String(fname)); }
              else if (op.equals("min"))
                { f = new RealAggregate(RealAggregate.Operator.min, null, null, new String(fname)); }
              else if (op.equals("mean"))
                { f = new RealAggregate(RealAggregate.Operator.mean, null, null, new String(fname)); }
              else if (op.equals("meanAndDev"))
                { f = new RealAggregate(RealAggregate.Operator.meanAndDev, null, null, new String(fname)); }

              else if (op.equals("and"))
                { f = new BooleanAggregate(BooleanAggregate.Operator.and, null, null, new String(fname)); }
              else if (op.equals("or"))
                { f = new BooleanAggregate(BooleanAggregate.Operator.or, null, null, new String(fname)); }

              else { assert(false); }

              aggregates.add(new Triple<Feature<Feature, Feature>, String, String>(f, attrs.getValue(1), attrs.getValue(2)));
            }
            else { assert(false); }
          }
          else if (qName.equals("constant")) {
            if (attrs.getQName(0).equals("type") && attrs.getQName(1).equals("value")) {
              if (attrs.getValue(0).equals("Boolean")) {
                f = new BooleanConstant(Boolean.parseBoolean(attrs.getValue(1)), new String(fname));
              }
              else if (attrs.getValue(0).equals("double")) {
                f = new RealConstant(Double.parseDouble(attrs.getValue(1)), new String(fname));
              }
              else { assert(false); }
            }
            else { assert(false); }
          }
          else if (qName.equals("failure")) {
            if (attrs.getQName(0).equals("feature") && attrs.getQName(1).equals("key")) {
              BooleanValued b = (BooleanValued)features.get(attrs.getValue(0));
              f = new Failure(fname, b, attrs.getValue(1));
            }
            else { assert(false); }
          }
          else if (qName.equals("select")) {
            if (attrs.getQName(0).equals("type") && attrs.getQName(1).equals("sel") && 
              attrs.getQName(2).equals("then") && attrs.getQName(3).equals("else")) {

              BooleanValued sel = (BooleanValued)features.get(attrs.getValue(1));
              String type = attrs.getValue(0);
              if (type.equals("double")) {
                RealValued thenF = (RealValued)features.get(attrs.getValue(2));
                RealValued elseF = (RealValued)features.get(attrs.getValue(3));
                f = new RealSelect(fname, sel, thenF, elseF, props);
              }
              else if (type.equals("Boolean")) {
                BooleanValued thenF = (BooleanValued)features.get(attrs.getValue(2));
                BooleanValued elseF = (BooleanValued)features.get(attrs.getValue(3));
                f = new BooleanSelect(fname, sel, thenF, elseF, props);
              }
              else { assert (false); }
            }
            else { assert (false); }
          }
          else if (qName.equals("dispersion")) {
            if (attrs.getQName(0).equals("feature") && attrs.getQName(1).equals("filter")) {
              RealValued feat = (RealValued)features.get(attrs.getValue(0));
              BooleanValued filt = (BooleanValued)features.get(attrs.getValue(1));
              f = new PointDispersion(feat, filt, fname);
            }
            else { assert (false); }
          }
          else if (qName.equals("avgConfidenceInterval")) {
            if (attrs.getQName(0).equals("feature") && attrs.getQName(1).equals("filter")) {
              RealValued feat = (RealValued)features.get(attrs.getValue(0));
              BooleanValued filt = (BooleanValued)features.get(attrs.getValue(1));
              f = new AvgConfidenceInterval(feat, filt, fname);
            }
            else { assert (false); }
          }
          // else if (qName.equals("avgConfidenceInterval")) {
          //   if (attrs.getQName(0).equals("feature") && attrs.getQName(1).equals("weight") && attrs.getQName(2).equals("filter")) {
          //     RealValued feat = (RealValued)features.get(attrs.getValue(0));
          //     RealValued weight = (RealValued)features.get(attrs.getValue(1));
          //     BooleanValued filt = (BooleanValued)features.get(attrs.getValue(2));
          //     f = new AvgConfidenceInterval(feat, weight, filt, fname);
          //   }
          //   else { assert (false); }
          // }
          else if (qName.equals("isDefined")) {
            if (attrs.getQName(0).equals("feature")) {
              Feature feat = (RealValued)features.get(attrs.getValue(0));
              f = new IsDefined(feat, fname);
            }
            else { assert (false); }
          }
          else if (qName.equals("default")) {
            if (attrs.getQName(0).equals("type") && attrs.getQName(1).equals("main") && 
                attrs.getQName(2).equals("ifNotAvail")) {
              String type = attrs.getValue(0);
              if (type.equals("double")) {
                RealValued main = (RealValued)features.get(attrs.getValue(1));
                RealValued ifNot = (RealValued)features.get(attrs.getValue(2));
                f = new RealDefault(fname, main, ifNot);
              }
              else if (type.equals("Boolean")) {
                BooleanValued main = (BooleanValued)features.get(attrs.getValue(1));
                BooleanValued ifNot = (BooleanValued)features.get(attrs.getValue(2));
                f = new BooleanDefault(fname, main, ifNot);
              }
              else { assert (false); }
            }
            else { assert (false); }
          }
          else { assert(false); }
          assert(f != null);
          features.put(new String(fname), f);
          state.setState(ParserState.appTemplate);
          fname = null;
          break;

        case ParserState.apps:
          if (qName.equals("application")) {
            if (attrs.getQName(0).equals("name") && attrs.getQName(1).equals("template") &&
                attrs.getQName(2).equals("architecture")) {
              name = new String(attrs.getValue(0));
              AppTemplate t = templates.get(attrs.getValue(1));
              Architecture a = architectures.get(attrs.getValue(2));
              apps.add(new MosaicApp(props, name, t, a));
            }
            else { assert(false); }
          }
          else {
            l.severe("Unknown element in state apps: " + qName);
            assert(false);
          }
         break;
      }
    }

    public void endElement(String uri, String localName, String qName) {
      if (false) { System.out.printf("end: (state: %s) (uri: %s) (localName: %s) (qName: %s)\n",
        state, uri, localName, qName); }

      switch (state.getState())
      {
        case ParserState.archs:
        {
          if (qName.equals("architecture")) {
            state.setState(ParserState.archs);
          }
          else { assert (false); }
          break;
        }
        case ParserState.arch:
        {
          assert (false);
          break;
        }
        case ParserState.appTemplates: break;
        case ParserState.appTemplate:
          if (qName.equals("applicationTemplate")) {
            
            for (Triple<Feature<Feature, Feature>, String, String> aggEntry : aggregates) {
              Aggregate a = (Aggregate)Tuple.get1(aggEntry);
              String featureName = Tuple.get2(aggEntry);
              Feature in = features.get(featureName);
              if (in == null) {
                l.severe("Missing feature "+featureName);
              }
              a.setFeature(in);
              BooleanValued filter = (BooleanValued)features.get(Tuple.get3(aggEntry));
              a.setFilter(filter);
            }

            templates.put(name, new AppTemplate(name, settings, features, objective, space, plotting, directPredict));
            state.setState(ParserState.appTemplates);
          }
          break;
        case ParserState.apps: break;
      }
    }
  }
  
  public static void xmlTest(TuningKnobSearchProperties props, String args[])
  {
    javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
    javax.xml.parsers.SAXParser parser = null;
    try {
      parser = factory.newSAXParser();
    }
    catch (javax.xml.parsers.ParserConfigurationException e) {
      System.out.printf("Parser configuration exception?  huh?\n");
      assert(false);
    }
    catch (org.xml.sax.SAXException e) {
      System.out.printf("SAX exception?  huh?\n");
      assert(false);
    }

    TuningKnobDescHandler handler = new TuningKnobDescHandler(props, null);
    java.io.File f = new java.io.File(args[0]);
    try {
      parser.parse(f, handler);
    }
    catch (org.xml.sax.SAXException e) {
      System.out.printf("SAX exception during parsing?  huh?\n");
      e.printStackTrace();
      System.exit(1);
    }
    catch (java.io.IOException e) {
      System.out.printf("IO exception?  huh?\n");
      System.exit(1);
    }

  }
}
