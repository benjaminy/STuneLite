/*
 * This is a total hack to make a map that pretends to be comparable.  Do not
 * ever use in a context where "compareTo" actually has to work.
 */

import java.util.HashMap;

public class ComparableHashMap<K,V> extends HashMap<K,V> implements ComparableMap<K,V>
{
  ComparableHashMap()
  {
    super();
  }

  public int compareTo(ComparableMap<K,V> other)
  {
    return 0;
  }
}
