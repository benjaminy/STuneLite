/*
 *
 */

public class BooleanConstant extends Constant<Boolean, Double> implements BooleanValued
{
  protected boolean val = false;

  public BooleanConstant(boolean v, String n)
  {
    super(n);
    val = v;
  }

  public Double getPrediction(ValueAndPredictionSource source, Point p) { return val ? 1.0 : 0.0; }
  public Boolean getValue(ValueSource source, Point p) { return val; }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        BooleanConstant other = null;
        try { other = (BooleanConstant)f; }
        catch (ClassCastException e) { assert(false); }
        int n = name.compareTo(other.getName());
        if (n == 0) {
          if (val) {
            if (other.val)  { return 0; }
            else            { return 1; }
          }
          else {
            if (other.val)  { return -1; }
            else            { return 0; }
          }
        }
        else {
          return n;
        }
      }
      return magicDiff;
    }
  }
}
