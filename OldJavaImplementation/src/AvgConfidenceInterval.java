/*
 *
 */

import com.mallardsoft.tuple.*;

public class AvgConfidenceInterval extends Calculated<Double, Pair<Double, Double>> implements RealValued
{
  protected RealValued feature = null;
  protected RealValued weight = null;
  protected BooleanValued filter = null;

  public AvgConfidenceInterval(RealValued s, RealValued w, BooleanValued f, String n)
  {
    super(n);
    feature = s;
    weight = w;
    filter = f;
  }

  public AvgConfidenceInterval(RealValued s, BooleanValued f, String n)
  {
    super(n);
    feature = s;
    weight = null;
    filter = f;
  }

  protected Pair<Double, Double> calcPrediction(ValueAndPredictionSource source, Point p)
  {
    assert (source != null);
    assert (p != null);
    if (source instanceof SingleSearch) {
      Double v = ((SingleSearch)source).avgConfidenceInterval(this, feature, weight, filter);
      return (v == null) ? null : Tuple.from(v, 0.0);
    }
    else {
      // average confidence interval doesn't make much sense outside the context of a search
      return null;
    }
  }
  
  protected Double calcValue(ValueSource source, Point dummy)
  {
    // average confidence interval doesn't make much sense outside the context of prediction
    return null;
  }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        AvgConfidenceInterval that = null;
        try { that = (AvgConfidenceInterval)f; }
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
