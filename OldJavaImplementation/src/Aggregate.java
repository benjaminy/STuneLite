/*
 *
 */

import java.util.Set;
import java.util.HashSet;
import com.mallardsoft.tuple.*;

public abstract class Aggregate<ValueType, PredictionType> extends Calculated<ValueType, PredictionType>
{
  protected Feature<ValueType, PredictionType> feature = null;
  protected BooleanValued filter = null;
  protected Set<Thread> evaluating = null;

  Aggregate(Feature<ValueType, PredictionType> s, BooleanValued f, String n)
  {
    super(n);
    feature = s;
    filter = f;
    evaluating = new HashSet<Thread>();
  }

  public void setFeature(Feature<ValueType, PredictionType> f)
  {
    assert (f != null);
    feature = f;
  }

  public void setFilter(BooleanValued f)
  {
    filter = f;
  }

  protected PredictionType calcPrediction(ValueAndPredictionSource source, Point dummy) {
    return calcPredictionJavaIsAnnoying(source, dummy);
  }

  protected PredictionType calcPredictionJavaIsAnnoying(ValueSource source, Point dummy)
  {
    synchronized (evaluating) {
      Thread me = Thread.currentThread();
      if (evaluating.contains(me)) {
        System.out.printf("Loops are evil %s\n", this);
        throw new NullPointerException();
      }
      evaluating.add(me);
    }
    Object a = initAccum();
    boolean none = true;
    for (Point p : source.getTestedPoints()) {
      boolean includePoint = true;
      if (filter != null) {
        Boolean b = filter.getValue(source, p);
        includePoint = (b != null) && b;
      }
      if (includePoint) {
        ValueType v = feature.getValue(source, p);
        if (v != null) {
          none = false;
          a = accumulate(a, v);
        }
      }
    }
    synchronized (evaluating) {
      Thread me = Thread.currentThread();
      evaluating.remove(me);
    }
    if (none) { return null; }
    else      { return finish(a); }
  }

  protected abstract PredictionType calcPredictionFromValue(ValueType v);
  protected abstract ValueType calcValueFromPrediction(PredictionType v);

  protected ValueType calcValue(ValueSource source, Point dummy)
  {
    assert (source != null);
    assert (dummy != null);
    PredictionType v = calcPredictionJavaIsAnnoying(source, dummy);
    if (v == null) { return null; }
    else { return calcValueFromPrediction(v); }
  }

  protected abstract Object initAccum();
  protected abstract Object accumulate(Object a, ValueType v);
  protected abstract PredictionType finish(Object a);

  public int compareTo(Feature<ValueType, PredictionType> f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        Aggregate that = null;
        try { that = (Aggregate)f; }
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

  public String toString()
  {
    return "aggregate feature " + getName() + "("+feature.getName()+","+filter.getName()+")";
  }
}
