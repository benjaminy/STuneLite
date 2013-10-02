/*
 *
 */

public abstract class Constant<ValueType, PredictionType> implements Feature<ValueType, PredictionType>
{
  protected String name = null;

  public String getName() { return name; }
  protected Constant(String n) { name = n; }

  public int magicNumber()
  {
    if (this instanceof RealConstant)           return 500;
    if (this instanceof BooleanConstant)        return 501;
    if (this instanceof KnobConstant)           return 502;
    assert(false);
    return -1;
  }
}
