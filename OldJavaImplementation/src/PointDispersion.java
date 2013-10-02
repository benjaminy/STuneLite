/*
 *
 */

import com.mallardsoft.tuple.*;

public class PointDispersion extends Calculated<Double, Pair<Double, Double>> implements RealValued
{
  protected Feature feature = null;
  protected BooleanValued filter = null;

  public PointDispersion(Feature s, BooleanValued f, String n)
  {
    super(n);
    feature = s;
    filter = f;
  }

  protected Pair<Double, Double> calcPrediction(ValueAndPredictionSource source, Point p)
  {
    assert (source != null);
    assert (p != null);
    Double v = calcValue(source, p);
    if (v == null) { return null; }
    else { return Tuple.from(v, 0.0); }
  }
  
  protected Double calcValue(ValueSource source, Point dummy)
  {
    if (source instanceof SingleSearch) {
      return ((SingleSearch)source).getPointDispersion(feature, filter);
    }
    else {
      // dispersion doesn't make much sense outside the context of a search
      return null;
    }
  }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        PointDispersion that = null;
        try { that = (PointDispersion)f; }
        catch (ClassCastException e) { assert(false); }
        int n = name.compareTo(that.getName());
        if (n == 0) {
          if (feature == null) {
            if (that.feature == null)  { return 0; }
            else                        { return -1; }
          }
          else {
            int s = feature.getName().compareTo(that.feature.getName());
            if (s == 0) {
              if (filter == null) {
                if (that.filter == null) { return 0; }
                else                      { return -1; }
              }
              else { return filter.getName().compareTo(that.filter.getName()); }
            }
            else { return s; }
          }
        }
        else { return n; }
      }
      else { return magicDiff; }
    }
  }
  

}
