/*
 *
 */

public abstract class Calculated<ValueType, PredictionType> implements Feature<ValueType, PredictionType>
{
  protected String name = null;

  public String getName() { return name; }

  protected Calculated(String n)
  {
    name = n;
  }

  public PredictionType getPrediction(ValueAndPredictionSource source, Point p)
  {
    boolean debugFlood = false;
    // if (name.equals("betterThanBest")) {System.out.printf("Calculating prediction for %s\n", name);}
    if (debugFlood) {
      System.err.printf("%s.getPrediction(_, %s) called ...\n", this, p);
    }
    if (source.isPredictionCached(p, this)) {
      Object v = source.getCachedPrediction(p, this);
      if (v != null) {
        try {
          if (debugFlood) {
            System.err.printf("  ... cached value: %s\n", v);
          }
          return (PredictionType)v;
        }
        catch (ClassCastException e) {
          System.err.printf("getPrediction: %s is of incorrect type.\n", v);
          assert(false);
        }
      }
      if (debugFlood) {
        System.err.printf("  ... cached value is null\n", v);
      }
      return null;
    }
    else {
      PredictionType v = calcPrediction(source, p);
      source.updatePredictionCache(p, this, v);
      if (debugFlood) {
        System.err.printf("  ... %s.getPrediction(_, %s) -> %s\n", this, p, v);
      }
      return v;
    }
  }

  public ValueType getValue(ValueSource source, Point p)
  {
    if (source.isValueCached(p, this)) {
      Object v = source.getCachedValue(p, this);
      if (v != null) {
        try { return (ValueType)v; }
        catch (ClassCastException e) {
          System.out.printf("getValue: %s is of incorrect type.\n", v);
          assert(false);
        }
      }
      return null;
    }
    else {
      ValueType v = calcValue(source, p);
      source.updateValueCache(p, this, v);
      return v;
    }
  }

  abstract protected PredictionType calcPrediction(ValueAndPredictionSource source, Point p);
  abstract protected ValueType calcValue(ValueSource source, Point p);

  public int magicNumber()
  {
    if (this instanceof RealAggregate)          return 100;
    if (this instanceof BooleanAggregate)       return 101;
    if (this instanceof RealSelect)             return 102;
    if (this instanceof BooleanSelect)          return 103;
    if (this instanceof PointDispersion)        return 104;
    if (this instanceof RealDefault)            return 105;
    if (this instanceof BooleanDefault)         return 106;
    if (this instanceof IsDefined)              return 107;
    if (this instanceof AvgConfidenceInterval)  return 108;
    /* Arithmetic */
    if (this instanceof Add)                    return 200;
    if (this instanceof Subtract)               return 201;
    if (this instanceof Multiply)               return 202;
    if (this instanceof Divide)                 return 203;
    if (this instanceof Negate)                 return 204;
    /* Logical */
    if (this instanceof AndFeature)             return 300;
    if (this instanceof OrFeature)              return 301;
    if (this instanceof OrMutexFeature)         return 302;
    if (this instanceof NotFeature)             return 303;
    if (this instanceof XorFeature)             return 304;
    if (this instanceof ImpliesFeature)         return 305;
    if (this instanceof AndGameFeature)         return 306;
    if (this instanceof OrGameFeature)          return 307;
    if (this instanceof OrGameFalseBias)        return 308;
    /* Comparison */
    if (this instanceof LessThan)               return 400;
    if (this instanceof GreaterThan)            return 401;
    assert (false);
    return -1;
  }
  
  public String toString()
  {
    return "calculated feature \""+name+"\"";
  }
}
