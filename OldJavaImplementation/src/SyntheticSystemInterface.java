/*
 *
 */

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

public class SyntheticSystemInterface implements TuningKnobSystemInterface
{
  TuningKnobSearchProperties props = null;
  Collection<Application> apps = null;

  public SyntheticSystemInterface(TuningKnobSearchProperties p)
  {
    assert(p != null);
    props = p;
    Random rGen = new Random(props.globalSeed);
    { long discard = rGen.nextLong(); }
    apps = new LinkedList<Application>();
    SynthApp app = new SynthApp(props, props.synthAppNumKnobs, props.synthAppNumKnobSettings, rGen);
    apps.add(app);
  }

  public Collection<Application> getApps()
  {
    return apps;
  }

  public void addApp(Application app) { assert (false); }
}
