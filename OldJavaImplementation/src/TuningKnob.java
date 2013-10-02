/*
 *
 */

import java.util.Random;

interface TuningKnob extends Comparable<TuningKnob>, Iterable
{
  boolean legalValue(Object o);
  int numLegalValues();
  double valueToCoordinate(Object o);
  Object coordinateToValue(double coord);
  double minCoordinateValue();
  double maxCoordinateValue();

  int valueToOrd(Object o);
  Object ordToValue(int i);

  Object nextRandom(Random rGen);
  Object stringToValue(String s);
  String valueToString(Object v);
  int compareValues(Object v1, Object v2);
  double distance(Object v1, Object v2);
  double longestDist();
  String getName();
}
