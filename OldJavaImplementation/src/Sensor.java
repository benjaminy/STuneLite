/*
 *
 */

import com.mallardsoft.tuple.*;

public class Sensor implements RealValued
{
  private String name = null, extractionName = null;

  public String getName() { return name; }
  public String getKey() { return extractionName; }

  public Sensor(String n, String e) {
    name = n;
    extractionName = e;
  }

  public Pair<Double, Double> getPrediction(ValueAndPredictionSource source, Point p)
  {
    Pair<Double, Double> prd = null;

    if (source.isPredictionCached(p, this)) {
      prd = (Pair<Double, Double>)source.getCachedPrediction (p, this);
    }
    else {
      prd = source.getSensorPrediction(p, this);
      if (prd != null) {
        source.updatePredictionCache (p, this, prd);
      }
    }

    return prd;
  }

  public Double getValue(ValueSource source, Point p)
  {
    if (source.isValueCached(p, this)) {
      return (Double)source.getCachedValue(p, this);
    }
    else {
      return source.getSensorReading(p, this);
    }
  }

  public int magicNumber() { return 1; }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      if (f instanceof Sensor) {
        Sensor other = (Sensor)f;
        int n = name.compareTo(other.getName());
        if (n == 0) {
          return extractionName.compareTo(other.extractionName);
        }
        else {
          return n;
        }
      }
      else {
        return magicNumber() - f.magicNumber();
      }
    }
  }
  public String toString()
  {
    return "sensor \""+name+"\"";
  }
}
