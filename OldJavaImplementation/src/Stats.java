/*
 * Just a collection of simple statistical functions
 */

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Random;
import com.mallardsoft.tuple.*;

public class Stats
{
  // epsilon is a little arbitrary.  MIN_VALUE is 2^-1074.  Multiplying by 10
  // is just an attempt to avoid weird underflow errors when epsilon is used.
  static final double epsilon = Double.MIN_VALUE * 10.0;

  static double max(Iterable<Double> xs)
  {
    double max = Double.NEGATIVE_INFINITY;
    for (Double x : xs) {
      max = Math.max(x, max);
    }
    return max;
  }

  static double min(Iterable<Double> xs)
  {
    double min = Double.NEGATIVE_INFINITY;
    for (Double x : xs) {
      min = Math.min(x, min);
    }
    return min;
  }

  static double genericMedian(Collection<Double> xs, double frac)
  {
    if (xs.size() < 1) {
      return Double.NaN;
    }
    else if (xs.size() < 2) {
      double r = Double.NaN;
      for (Double x : xs) { r = x; }
      return r;
    }
    else {
      double xsArr[] = new double[xs.size()];
      int i = 0;
      for (Double x : xs) { xsArr[i++] = x; }
      Arrays.sort(xsArr);
      double rawIndex = ((double)xs.size() - 1) * frac;
      long indexFloor = Math.round(Math.floor(rawIndex)), indexCeil = Math.round(Math.ceil(rawIndex));
      double ceilFrac = rawIndex - ((double)indexFloor);
      return xsArr[((int)indexCeil)] * ceilFrac + xsArr[((int)indexFloor)] * (1.0 - ceilFrac);
    }
  }

  static double arithmeticMean(Collection<Double> xs)
  {
    double total = 0.0;
    for (Double x : xs) {
      total += x;
    }
    if (xs.size() > 0) {
      return total / ((double)xs.size());
    }
    else {
      return Double.NaN;
    }
  }

  static double geometricMean(double epsilon, Collection<Double> xs)
  {
    double total = 0.0;
    for (Double x : xs) {
      if (x < epsilon) {
        System.out.printf("WARNING: Very small number in geometric mean set\n");
      }
      total += Math.log(x);
    }
    if (xs.size() > 0) {
      return Math.exp(total / ((double)xs.size()));
    }
    else {
      return Double.NaN;
    }
  }

  static double harmonicMean(double epsilon, Collection<Double> xs)
  {
    double total = 0.0;
    for (Double x : xs) {
      if (false) { System.out.printf("%f???\n", x); }
      if (x < epsilon) {
        System.out.printf("WARNING: Very small number in harmonic mean set\n");
      }
      total += 1.0/x;
    }
    if (xs.size() > 0) {
      double result = ((double)xs.size()) / total;
      if (false) { System.out.printf("xs:%d tot:%f res:%f?????\n", xs.size(), total, result); }
      return result;
    }
    else {
      return Double.NaN;
    }
  }

  static double weightedArithmeticMean(Iterable<Pair<Double, Double>> xs)
  {
    double minWeight = Double.POSITIVE_INFINITY;
    boolean atLeastOne = false;
    for (Pair<Double, Double> weightAndValue : xs) {
      minWeight = Math.min(minWeight, Tuple.get1(weightAndValue));
      atLeastOne = true;
    }
    if (atLeastOne) {
      double weightAdjuster = 0.0;
      if (minWeight < 0.0) {
        weightAdjuster = epsilon - minWeight;
      }
      double weightTotal = 0.0;
      double valTotal = 0.0;
      for (Pair<Double, Double> weightAndValue : xs) {
        double adjustedWeight = Tuple.get1(weightAndValue) + weightAdjuster;
        weightTotal += adjustedWeight;
        valTotal += adjustedWeight * Tuple.get2(weightAndValue);
      }
      return valTotal / weightTotal;
    }
    else { return Double.NaN; }
  }

  static double weightedStandardDeviation(double mean, Iterable<Pair<Double, Double>> xs)
  {
    double weightTotal = 0.0;
    double valTotal = 0.0;
    boolean atLeastOne = false;
    for (Pair<Double, Double> weightAndValue : xs) {
      weightTotal += Tuple.get1(weightAndValue);
      double diff = Tuple.get2(weightAndValue) - mean;
      valTotal += Tuple.get1(weightAndValue) * diff * diff;
      atLeastOne = true;
    }
    if (false) { System.out.printf("WSD: %f %f\n", valTotal, weightTotal); }
    if (atLeastOne) {
      return Math.sqrt(valTotal / weightTotal);
    }
    else { return Double.NaN; }
  }

  static double coefficientOfVariation(Collection<Double> xs)
  {
    double total = 0.0;
    for (Double x : xs) {
      total += x;
    }
    double mean = total / xs.size();

    total = 0.0;
    for (Double x : xs) {
      double diff = x - mean;
      total += diff * diff;
    }
    
    double standardDeviation = Math.sqrt(total / xs.size());
    double cov = standardDeviation / mean;
    // System.out.printf("CoV: s %f  m %f  c %f\n", standardDeviation, mean, cov);
    return cov;
  }

  /* Assuming a normal distribution with mean 0.0 and standard deviation 1.0,
   * this function will return an approximation of the portion of the
   * distribution less than x. */
  static double cumulativeNormalDistribution(double x)
  {
    double b1 =  0.319381530;
    double b2 = -0.356563782;
    double b3 =  1.781477937;
    double b4 = -1.821255978;
    double b5 =  1.330274429;
    double p  =  0.2316419;
    double c  =  0.39894228;

    // These first two cases handle the extreme tails of the distribution
    if (x > 6.0)  return 1.0 - epsilon;
    if (x < -6.0) return epsilon;

    if (x >= 0.0) {
      double t = 1.0 / ( 1.0 + p * x );
      return (1.0 - c * Math.exp( -x * x / 2.0 ) * t *
        ( t *( t * ( t * ( t * b5 + b4 ) + b3 ) + b2 ) + b1 ));
    }
    else {
      double t = 1.0 / ( 1.0 - p * x );
      return ( c * Math.exp( -x * x / 2.0 ) * t *
        ( t *( t * ( t * ( t * b5 + b4 ) + b3 ) + b2 ) + b1 ));
    }
  }

  static void normalDistTest()
  {
    for (double x = -6.0; x < 6.0; x += 1.0) {
      for (double y = 0.0; y < 0.99; y += 0.1) {
        double p = cumulativeNormalDistribution(x+y);
        System.out.printf("x=%1.2g, p=%1.4g  ", x+y, p);
      }
      System.out.printf("\n");
    }
  }

  static void distsTest()
  {
    long seed = 2;
    Random rand = new Random(seed);
    double mu1 = 150.0, sigma1 = 2.0, mu2 = 100.0, sigma2 = 3.0;
    int numSamples = 10000;
    SortedSet<Double> ratios = new TreeSet<Double>();
    SortedSet<Double> products = new TreeSet<Double>();
    double x1, x2;
    for (int i = 0; i < numSamples; i++) {
      x1 = rand.nextGaussian();
      x2 = rand.nextGaussian();
      x1 = (x1 * sigma1) + mu1;
      x2 = (x2 * sigma2) + mu2;
      ratios.add(x1/x2);
      products.add(x1 * x2);
    }
    int i = 0;
    double sum = 0.0;
    for (Double d : ratios) {
      // System.out.printf("i=%3d x1/x2=%f\n", i, d);
      sum += d;
      i++;
    }
    double mean = sum / ((double)numSamples);
    double differenceTotal = 0.0;
    for (Double d : ratios) {
      double diff = mean - d;
      differenceTotal += diff * diff;
    }
    double stdDev = Math.sqrt(differenceTotal / ((double)numSamples));
    System.out.printf("Ratio: mu:%f  sigma:%f\n", mean, stdDev);
    i = 0;
    sum = 0.0;
    for (Double d : products) {
      // System.out.printf("i=%3d x1*x2=%f\n", i, d);
      i++;
      sum += d;
    }
    mean = sum / ((double)numSamples);
    differenceTotal = 0.0;
    for (Double d : products) {
      double diff = mean - d;
      differenceTotal += diff * diff;
    }
    stdDev = Math.sqrt(differenceTotal / ((double)numSamples));
    System.out.printf("Product: mu:%f  sigma:%f\n", mean, stdDev);
  }

  static Pair<Double, Double> meanStdDev (Collection<Double> x)
  {
    boolean none = true;
    double total = 0.0;
    int count = 0;
    for (Double v : x) {
      if (v != null) {
        none = false;
        total += v;
        count++;
      }
    }
    if (none) {
      return null;
    }
    double mean = total / ((double)count);
    total = 0.0;
    for (Double v : x) {
      if (v != null) {
        double diff = v - mean;
        total += diff * diff;
      }
    }
    double stdDev = Math.sqrt(total / ((double)count));
    return Tuple.from(mean, stdDev);
  }

  static void messingAroundWithGaussians(String args[])
  {
    Random r = new Random(42);
    int outerIters = 10;
    int iters = 100000;
    double meanMultiplier = 100.0;
    double stdDevMultiplier = 25.0;
    boolean add = true;

    for (int outer = 0; outer < outerIters; outer++) {
      double offset1 = (r.nextDouble() + 1.0) * meanMultiplier;
      double offset2 = (r.nextDouble() + 1.0) * meanMultiplier;
      // double offset = 100.0;
      List<Double> ds1 = new LinkedList<Double>();
      List<Double> ds2 = new LinkedList<Double>();
      List<Double> mul = new LinkedList<Double>();
      for (int i = 0; i < iters; i++) {
        double g1 = (r.nextGaussian() * stdDevMultiplier) + offset1;
        ds1.add(g1);
        double g2 = (r.nextGaussian() * stdDevMultiplier) + offset2;
        ds2.add(g2);
        if (add)
          mul.add(g1 + g2);
        else
          mul.add(g1 * g2);
      }
      Pair<Double, Double> meanStdDev1 = meanStdDev(ds1);
      double mean1 = Tuple.get1(meanStdDev1), stdDev1 = Tuple.get2(meanStdDev1);
      Pair<Double, Double> meanStdDev2 = meanStdDev(ds2);
      double mean2 = Tuple.get1(meanStdDev2), stdDev2 = Tuple.get2(meanStdDev2);
      Pair<Double, Double> meanStdDevM = meanStdDev(mul);
      double meanM = Tuple.get1(meanStdDevM), stdDevM = Tuple.get2(meanStdDevM);
      double theoreticalMeanM = (add) ?
          mean1 + mean2
        :
          mean1 * mean2
        ;
      double meanErrorM = Math.abs(meanM - theoreticalMeanM) / meanM;
      double theoreticalStdDevM = (add) ?
          Math.sqrt(stdDev1 * stdDev1 + stdDev2 * stdDev2)
        :
      //    offset1 + offset2 + 1.0
          // Math.sqrt(mean1 * mean1 * stdDev2 * stdDev2 + stdDev1 * stdDev1 * mean2 * mean2)
          Math.sqrt(stdDev1 * stdDev1 * stdDev2 * stdDev2 + mean1 * mean1 * stdDev2 * stdDev2 + stdDev1 * stdDev1 * mean2 * mean2)
        ;
      double stdDevErrorM = Math.abs(stdDevM - theoreticalStdDevM) / stdDevM;
      System.out.printf("1(%f,%f)  2(%f,%f)  mean: %f (%f)  mean error: %f  std dev: %f (%f)  std dev error: %f\n",
        mean1, stdDev1, mean2, stdDev2, meanM, theoreticalMeanM, meanErrorM, stdDevM, theoreticalStdDevM, stdDevErrorM);
    }

    for (int outer = 0; outer < outerIters; outer++) {
      double offset3 = (r.nextDouble() + 1.0) * meanMultiplier;
      // double offset = 100.0;
      List<Double> div = new LinkedList<Double>();
      List<Double> ds3 = new LinkedList<Double>();
      for (int i = 0; i < iters; i++) {
        double g3 = (r.nextGaussian() * stdDevMultiplier) + offset3;
        ds3.add(g3);
        div.add(1.0 / g3);
      }
      Pair<Double, Double> meanStdDev3 = meanStdDev(ds3);
      double mean3 = Tuple.get1(meanStdDev3), stdDev3 = Tuple.get2(meanStdDev3);
      Pair<Double, Double> meanStdDevD = meanStdDev(div);
      double meanD = Tuple.get1(meanStdDevD), stdDevD = Tuple.get2(meanStdDevD);
      double theoreticalMeanD = 1.0/mean3;
      // double theoreticalMeanD = (1.0 + (1.0 / (mean3 * mean3)))/mean3;
      double meanErrorD = Math.abs(meanD - theoreticalMeanD) / meanD;
      // double theoreticalStdDevD = 1.0 / (offset3 * Math.sqrt(offset3*offset3 - 1.0));
      double foo1 = stdDev3 / (mean3 * (mean3 + stdDev3));
      double foo2 = stdDev3 / (mean3 * (mean3 - stdDev3));
      // double theoreticalStdDevD = Math.sqrt(Math.abs(foo1 * foo1 - foo2 * foo2));
      // double theoreticalStdDevD = Math.abs(foo2 - foo1);
      // double theoreticalStdDevD = Math.abs(stdDev3/((mean3-stdDev3)*(mean3+stdDev3)));
      // double theoreticalStdDevD = Math.sqrt((foo1 * foo1 + foo2 * foo2)/2.0);
      double theoreticalStdDevD = Math.abs(0.5*foo1+0.5*foo2);
      // double theoreticalStdDevD = 1.0 / (offset3 * (offset3 - 0.5));
      double stdDevErrorD = Math.abs(stdDevD - theoreticalStdDevD) / stdDevD;
      System.out.printf("i(%f,%f)  mean: %f (%f)  mean error: %f  std dev: %f (%f)  std dev error: %f\n",
        mean3, stdDev3, meanD, theoreticalMeanD, meanErrorD, stdDevD, theoreticalStdDevD, stdDevErrorD);
    }
  }
}
