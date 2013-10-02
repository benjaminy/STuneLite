/*
 *
 */

import java.util.Collection;

public interface TuningKnobSystemInterface
{
  Collection<Application> getApps();
  void addApp(Application a);
}
