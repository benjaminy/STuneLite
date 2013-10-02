/*
 *
 */

import com.mallardsoft.tuple.*;

public abstract class DefaultFeature<ValueType, PredictionType> extends Calculated<ValueType, PredictionType>
{
  protected Feature<ValueType, PredictionType> feature = null;
  protected Feature<ValueType, PredictionType> def = null;

  public DefaultFeature(String n, Feature<ValueType, PredictionType> f, Feature<ValueType, PredictionType> d)
  {
    super(n);
    feature = f;
    def = d;
  }

  protected PredictionType calcPrediction(ValueAndPredictionSource source, Point pt)
  {
    assert (source != null);
    assert (pt != null);
    PredictionType v = feature.getPrediction(source, pt);
    if (v == null) { return def.getPrediction(source, pt); }
    return v;
  }

  protected ValueType calcValue(ValueSource source, Point pt)
  {
    ValueType val = feature.getValue(source, pt);
    if (val == null) { return def.getValue(source, pt); }
    return val;
  }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        DefaultFeature that = null;
        try { that = (DefaultFeature)f; }
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
              if (def == null) {
                if (that.def == null) { return 0; }
                else                    { return -1; }
              }
              else { return def.getName().compareTo(that.def.getName()); }
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
