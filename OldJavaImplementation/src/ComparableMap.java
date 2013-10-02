/*
 * This is a total hack to make a map that pretends to be comparable.
 */

import java.util.Map;

public interface ComparableMap<K,V> extends Map<K,V>, Comparable<ComparableMap<K,V>>
{
  
}
