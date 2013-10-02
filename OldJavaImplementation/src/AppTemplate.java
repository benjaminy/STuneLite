/*
 *
 */

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;

// public class AppTemplate implements Comparable <AppTemplate> {
public class AppTemplate {
  private String                        name              = null;
  private Map<String, String>           settings          = null;
  private KnobSettingSpace              space             = null;
  private Map<String, TuningKnob>       knobMap           = null;
  private Map<String, Feature>          features          = null;
  private Set<RealValued>               plottingFeatures  = null;
  private Set<RealValued>               directPredict     = null;
  private Objective                     objective         = null;

  public AppTemplate(String n, Map<String, String> set, Map<String, Feature> fe,
      Objective o, KnobSettingSpace s, Set<RealValued> pl, Set<RealValued> dp)
  {
    if (false) {
      for (RealValued f : pl) {
        System.out.printf("plotting feature: %s\n", f);
      }
      for (RealValued f : dp) {
        System.out.printf("direct prediction feature: %s\n", f);
      }
    }
    assert (s   != null);
    assert (set != null);
    assert (n   != null);
    assert (fe  != null);
    assert (o   != null);
    assert (o.getFeature()   != null);
    assert (o.getFailure()   != null);
    name = new String(n);
    settings = new HashMap<String, String>(set);
    features  = new HashMap<String, Feature>();
    knobMap   = new HashMap<String, TuningKnob>();
    plottingFeatures = new TreeSet<RealValued>();
    directPredict = new TreeSet<RealValued>();
    boolean foundObjectiveFeature = false;
    boolean foundObjectiveFailure = false;
    for (Map.Entry<String, Feature> ent : fe.entrySet()) {
      String fn = ent.getKey();
      Feature f = ent.getValue();
      if (f.compareTo(o.getFeature()) == 0) {
        assert(!foundObjectiveFeature);
        foundObjectiveFeature = true;
      }
      if (f.compareTo(o.getFailure()) == 0) {
        assert(!foundObjectiveFailure);
        foundObjectiveFailure = true;
      }
      features.put(new String(fn), f);
      if (pl.contains(f)) {
        plottingFeatures.add((RealValued)f);
      }
      if (dp.contains(f)) {
        directPredict.add((RealValued)f);
      }
    }
    objective = o;
    space = s;
    for (TuningKnob knob : space) {
      knobMap.put(knob.getName(), knob);
    }
  }

  public String                 getName() { return name; }
  public Set<Map.Entry<String, String>> getSettings() { return settings.entrySet(); }
  public String                 getSetting(String name) { return settings.get(name); }
  public Objective              getObjective() { return objective; }
  public Feature                getFeature(String name) { return features.get(name); }
  public TuningKnob             getTuningKnob(String name) { return knobMap.get(name); }
  public Collection<Feature>    getFeatures() { return features.values(); }
  public Collection<RealValued> getPlottingFeatures() { return plottingFeatures; }
  public Collection<RealValued> getDirectPredictFeatures() { return directPredict; }
  public boolean                isFeatureForPlotting(Feature f) { return plottingFeatures.contains(f); }
  public boolean                isFeatureForDirectPrediction(Feature f) { return directPredict.contains(f); }
  public KnobSettingSpace       getKnobSpace() { return space; }
}
