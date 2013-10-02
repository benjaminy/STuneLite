/*
 *
 */

public class LessThan extends Comparison
{
  TuningKnobSearchProperties props = null;

  public LessThan(TuningKnobSearchProperties p, RealValued l, RealValued r, String n)
  {
    super(l, r, n);
    props = p;
  }

  protected Double calcComparisonPrediction(double mean1, double dev1, double mean2, double dev2)
  {
    double diffMean = mean1 - mean2;
    double diffDev = dev1 + dev2;
    double ret = Double.NaN;
    if (diffDev < props.epsilon) {
      ret = mean1 < mean2 ? 1.0 : 0.0;
      if (true) { System.out.printf("\"<\" %s m1:%e s1:%e %s m2:%e s2:%e z:%e ret:%e\n",
          left, mean1, dev1, right, mean2, dev2, -diffMean/diffDev, ret); }
    }
    else {
      /* Assuming a normal distribution with mean 0.0 and standard deviation 1.0,
       * this function will return an approximation of the portion of the
       * distribution less than x. */
      ret = Stats.cumulativeNormalDistribution(-diffMean/diffDev);
      if (Double.isNaN(ret)) {
        System.out.printf("less than broke: m1:%f s1:%f m2:%f s2:%f\n", mean1, dev1, mean2, dev2);
        assert (false);
      }
    }
    if (false) { System.out.printf("\"<\" m1:%e s1:%e m2:%e s2:%e z:%e cnd:%e\n",
        mean1, dev1, mean2, dev2, -diffMean/diffDev, ret); }
    return ret;
  }

  protected Boolean calcComparisonValue(double d1, double d2) { return d1 < d2; }
}
