/*
 *
 */

import com.mallardsoft.tuple.*;

public class IsDefined extends Calculated<Boolean, Double> implements BooleanValued
{
  protected Feature feature = null;

  public IsDefined(Feature f, String n)
  {
    super(n);
    feature = f;
  }

  protected Double calcPrediction(ValueAndPredictionSource source, Point p)
  {
    assert (source != null);
    assert (p != null);
    Object v = feature.getPrediction(source, p);
    if (v == null)  { return 0.0; }
    else            { return 1.0; }
  }
  
  protected Boolean calcValue(ValueSource source, Point p)
  {
    assert (source != null);
    assert (p != null);
    Object v = feature.getValue(source, p);
    if (v == null)  { return false; }
    else            { return true; }
  }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        IsDefined that = null;
        try { that = (IsDefined)f; }
        catch (ClassCastException e) { assert(false); }
        int n = name.compareTo(that.getName());
        if (n == 0) {
          if (feature == null) {
            if (that.feature == null) { return 0; }
            else                      { return -1; }
          }
          else { return feature.getName().compareTo(that.feature.getName()); }
        }
        else { return n; }
      }
      else { return magicDiff; }
    }
  }
}
