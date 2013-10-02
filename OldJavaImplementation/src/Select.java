/*
 *
 */

public abstract class Select<ValueType, PredictionType> extends Calculated<ValueType, PredictionType>
{
  // It would be nice if somehow we could convince the type system to check that
  // the selection feature needs to be "constant like", while reusing features
  // like comparison in both constant and non-constant contexts.  For now we
  // will just dynamically check that the probabilities are within epsilon of
  // zero or one.
  protected BooleanValued sel = null;
  protected Feature thenF = null, elseF = null;
  protected TuningKnobSearchProperties props = null;

  public Select(String n, BooleanValued s, Feature t, Feature e, TuningKnobSearchProperties p)
  {
    super(n);
    assert (s != null);
    assert (t != null);
    assert (e != null);
    assert (p != null);
    sel = s;
    thenF = t;
    elseF = e;
    props = p;
  }

  protected PredictionType calcPrediction(ValueAndPredictionSource source, Point p)
  {
    Double prob = sel.getPrediction(source, p);
    if (prob == null) {
      return null;
    }
    else {
      if (prob > (1.0 - props.epsilon)) {
        return (PredictionType)thenF.getPrediction(source, p);
      }
      else if (prob < props.epsilon) {
        return (PredictionType)elseF.getPrediction(source, p);
      }
      else {
        assert (false);
        return null;
      }
    }
  }

  protected ValueType calcValue(ValueSource source, Point p)
  {
    Boolean choice = sel.getValue(source, p);
    if (choice == null) {
      return null;
    }
    else {
      if (choice) {
        return (ValueType)thenF.getValue(source, p);
      }
      else {
        return (ValueType)elseF.getValue(source, p);
      }
    }
  }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        Select other = null;
        try { other = (Select)f; }
        catch (ClassCastException e) { assert(false); }
        int n = name.compareTo(other.getName());
        if (n == 0) {
          int s = sel.compareTo(other.sel);
          if (s == 0) {
            int t = thenF.compareTo(other.thenF);
            if (t == 0) {
              return elseF.compareTo(other.elseF);
            }
            else { return t; }
          }
          else { return s; }
        }
        else { return n; }
      }
      return magicDiff;
    }
  }
  
  public String toString()
  {
    return "select "+sel+" ? "+thenF+" : "+elseF;
  }
}
