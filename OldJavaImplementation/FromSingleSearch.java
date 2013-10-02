// public Pair<Double, Double> predictSensor(Point pt, Sensor sensor, Collection<Point> pts, Neighborhood nbhood,
//     double globalErrorRate, double cov, Collection<Pair<Double, Double>> rates)
// {
//   KnobSettingSpace space = app.getTemplate().getKnobSpace();
//   // I can't remeber why I thought I needed to make a new neighborhood
//   // Neighborhood nbhood = new Mesh(pts);
//   Collection<Point> neighbors = nbhood.wouldBeNeighbors(pt);
//   double p = basePredictionNeighbors(pt, sensor, neighbors);
//   double valDiffTotal = 0.0;
//   int neighborsThatMatter = 0;
//   for (Point n : neighbors) {
// 
//     // get the vector from the neighbor to the candidate
//     double distanceSum = 0.0;
//     LinkedList<Double> vector = new LinkedList<Double>();
//     {
//       Iterator candidateSettings  = pt.iterator();
//       Iterator neighborSettings   = n.iterator();
//       Iterator <TuningKnob> knobs = app.getTemplate().getKnobSpace().iterator();
//       while (candidateSettings.hasNext() && neighborSettings.hasNext() && knobs.hasNext()) {
//         Object candidateSetting = candidateSettings.next();
//         Object neighborSetting  = neighborSettings.next();
//         TuningKnob knob  = knobs.next();
//         double diff = knob.valueToCoordinate(candidateSetting) - knob.valueToCoordinate(neighborSetting);
//         vector.addLast(diff);
//         distanceSum += diff * diff;
//       }
//       assert(!candidateSettings.hasNext());
//       assert(!neighborSettings.hasNext());
//       assert(!knobs.hasNext());
//     }
// 
//     // build a point that's close to the neighbor, in the opposite direction from the candidate
//     double distance = Math.sqrt(distanceSum);
//     Point derivPt = new Point(space);
//     {
//       int kIdx = 0;
//       Iterator neighborSettings = n.iterator();
//       Iterator <TuningKnob> knobs = app.getTemplate().getKnobSpace().iterator();
//       Iterator<Double> vectorComponents = vector.iterator();
//       while (neighborSettings.hasNext() && vectorComponents.hasNext() && knobs.hasNext()) {
//         Object neighborSetting  = neighborSettings.next();
//         TuningKnob knob = knobs.next();
//         double v = vectorComponents.next();
//         // FIXME: replace 0.5 with a better distance
//         derivPt.add(kIdx, knob.valueToCoordinate(neighborSetting) - (0.5 * v / distance));
//         kIdx++;
//       }
//       assert(!neighborSettings.hasNext());
//       assert(!vectorComponents.hasNext());
//       assert(!knobs.hasNext());
//     }
// 
//     Set<Point> neighborNeighbors = nbhood.get(n);
//     neighborNeighbors.add(n);
// 
//     Collection<Point> derivNeighbors = nbhood.wouldBeNeighbors(derivPt, neighborNeighbors);
//     double nVal = Double.NaN, extrapolatedCandidateVal = Double.NaN, derivVal = Double.NaN;
//     if (derivNeighbors.size() > 1) {
//       derivVal = basePredictionNeighbors(derivPt, sensor, derivNeighbors);
//       nVal = sensor.getValue(this, n);
//       // FIXME: 0.5 thing again
//       double deriv = (nVal - derivVal) / 0.5;
//       extrapolatedCandidateVal = nVal + (deriv * distance);
//       double valDiff = Math.abs(extrapolatedCandidateVal - p);
//       valDiffTotal += valDiff;
//       double valDiffRate = valDiff / distance;
//       if (false) { System.out.printf("  %s valDiff: %f  distance: %f  rate: %f\n",
//         sensor.getName(), valDiff, distance, valDiffRate); }
//       rates.add(Tuple.from(distance, valDiffRate));
//       neighborsThatMatter++;
//     }
//     else {
//       rates.add(Tuple.from(distance, Double.NaN));
//     }
//     if (false) { System.out.printf("#dn: %d  c: (%f - %f = %f) %s  n: (%f) %s  d: (%f) %s\n", derivNeighbors.size(), 
//       p, extrapolatedCandidateVal, p - extrapolatedCandidateVal, this, nVal, n, derivVal, derivPt); }
// 
//     // double effectiveErrorRate = Math.max (n.errorRate, globalErrorRate);
//     // minError = Math.min (minError, effectiveErrorRate * distance(n));
//   }
//   double avgValDiff = valDiffTotal / ((double)neighborsThatMatter);
//   return Tuple.from(p, avgValDiff);
// }









//from Point
//
//  public Point(Application a)
//  {
//    super();
//    app = a;
//    predicted = new Predictions();
//    predictions = new TreeMap<Feature, Object>();
//    values = new TreeMap<Feature, Object>();
//    failureCauses = new TreeMap<Failure, Boolean>();
//  }
//
//  public String getNote(String key)
//  {
//    assert (key != null);
//    return notes.get(key);
//  }
//
//  public void setNote(String key, String note)
//  {
//    assert (key != null);
//    assert (note != null);
//    assert (!notes.containsKey(key));
//    notes.put(key, note);
//  }
//
//
//  public void setStatus(String statusName)
//  {
//    status = Status.valueOf(statusName);
//  }
//
//  // public static class ExpectedValComparator implements Comparator<Point>
//  // {
//  //   /* This comparison claims that points with higher value are "lower" in the
//  //    * sorting order.  It is designed this way so that the standard iterator
//  //    * will visit higer-valued points first. */
//  //   public int compare(Point p1, Point p2)
//  //   {
//  //     assert(p1.predicted.finalValue != null && p2.predicted.finalValue != null);
//  //     double diff = p1.predicted.finalValue - p2.predicted.finalValue;
//  //     return diff < 0 ? 1 : (diff > 0 ? -1 : 0);
//  //   }
//  // }
//
//  class Predictions {
//    Double harmonicMeanDistance = null;
//    Double inverseDistancesTotal = null;
//    Double baseVal = null;
//    Double dispersion = null;
//    Double variationScore = null;
//    Double spacing = null;
//    // Double finalValue = null;
//    Double smoothedScore = null;
//    Double scoreSmoothedDiff = null;
//    Double inaccuracyRate = null;
//    double scratch = 0.0;
//  }
//
//  public void setPrediction(Feature f, Object p) { predictions.put(f, p); }
//  public Object getPrediction(Feature f) { return predictions.get(f); }
//  public void setValue(Feature f, Object v) { values.put(f, v); }
//  public Object getValue(Feature f) { return values.get(f); }
//  public void setFailureCause(Failure f, boolean isCause) { failureCauses.put(f, isCause); }
//  public Boolean getFailureCause(Failure f) { return failureCauses.get(f); }
//  public void clearCaches()
//  {
//    predictions.clear();
//    values.clear();
//  }
//
//  public void setSensorPrediction(Feature f, Pair<Double, Double> p)
//  {
//    predictions.put(f, p);
//    Pair<Double, Double> p2 = (Pair<Double, Double>)predictions.get(f);
//    if (false) { System.out.printf("set pt=%s pr=%s pr2=%s\n", this, p, p2); }
//  }
//  public Pair<Double, Double> getSensorPrediction(Feature f)
//  {
//    Pair<Double, Double> p = (Pair<Double, Double>)predictions.get(f);
//    if (false) { System.out.printf("get pt=%s pr=%s\n", this, p); }
//    return (p == null) ? null : p;
//  }
//  public void setSensorValue(Feature f, double v) { values.put(f, v); }
//  public Double getSensorValue(Feature f)
//  {
//    Object v = values.get(f);
//    return (v == null) ? null : (Double)v;
//  }
//
//  void setHeuristicScore(double s) { heuristicScore = s; }
//  double getHeuristicScore() { return heuristicScore; }
//
//  Double getSensorReading(Sensor sensor)
//  {
//    return app.getSensorReading(this, sensor);
//  }
//
//  public boolean  hasStartedTesting   () { return app.hasStartedTesting(this); }
//  public boolean  hasCompletedTesting () { return app.hasCompletedTesting(this); }
//  public boolean  wasSuccessful       () { return app.wasSuccessful(this); }
//
//  static void calcLocalInaccuracyRates(Sensor sensor, Neighborhood nbhood)
//  {
//    for (Map.Entry<Point, Set<Point>> entry : nbhood.entrySet()) {
//      double minDist      = Double.POSITIVE_INFINITY;
//      Point x = entry.getKey();
//      for (Point y : entry.getValue()) {
//        minDist = Math.min(x.distance(y), minDist);
//      }
//      double predictedScore = x.basePredictionNeighbors(sensor, entry.getValue());
//      Double score = x.app.getSensorReading(x, sensor);
//      if (score == null) {
//        System.err.printf("(LIR) Neighbor has no sensor reading\n");
//        System.exit(1);
//      }
//      double absoluteInaccuracy = Math.abs(predictedScore - score);
//      x.predicted.inaccuracyRate = absoluteInaccuracy / minDist;
//    }
//  }
//
//  double basePrediction(Sensor sensor, Neighborhood nbhood)
//  {
//    return basePredictionNeighbors(sensor, nbhood.wouldBeNeighbors(this));
//  }
//
//
//  public double findModelError(Sensor sensor, double actualValue, Collection<Point> pts)
//  {
//    Neighborhood nbhood = new Mesh(pts);
//    Collection<Point> neighbors = nbhood.wouldBeNeighbors(this);
//    double p = basePredictionNeighbors(sensor, neighbors);
//    double absoluteError = Math.abs(p - actualValue);
//    double minDistance = Double.POSITIVE_INFINITY;
//    for (Point n : neighbors) { minDistance = Math.min (minDistance, distance(n)); }
//    errorRate = absoluteError / minDistance;
//    if (false) { System.out.printf("pt:%s e:%f p:%f v:%f r:%f d:%f\n", 
//          this, absoluteError, p, actualValue, errorRate, minDistance); }
//    return errorRate;
//  }
//
//  public Pair<Double, Double> predictSensorOld(Sensor sensor, Collection<Point> pts, Neighborhood nbhood, 
///    double globalErrorRate, double cov)
//  {
//    Collection<Point> neighbors = nbhood.wouldBeNeighbors(this);
//    double p = basePredictionNeighbors(sensor, neighbors);
//    double minError = Double.POSITIVE_INFINITY;
//    for (Point n : neighbors) {
//      double effectiveErrorRate = Math.max (n.errorRate, globalErrorRate);
//      minError = Math.min (minError, effectiveErrorRate * distance(n));
//    }
//    return Tuple.from(p, minError * cov);
//  }
//
//  double inaccuracyRange(Sensor sensor, Neighborhood nbhood, double globalInaccuracyRate, double cov)
//  {
//    double minRange = Double.POSITIVE_INFINITY;
//    for (Point neighbor : nbhood.wouldBeNeighbors(this)) {
//      assert(app.getSensorReading(neighbor, sensor) != null);
//      double effectiveRate = Math.max(globalInaccuracyRate * cov, neighbor.predicted.inaccuracyRate);
//      double range = effectiveRate * distance(neighbor);
//      minRange = Math.min(minRange, range);
//    }
//    return minRange;
//  }
//
//  void fillInHeuristics(Set<Point> points, double slope, Neighborhood allNbhood, 
//       Neighborhood justSuccessfulNbhood, double magicDist, double cov,
//    double refDist, double maxScore, double smoothingFactor, double highestScore, double globalInaccuracyRate)
//  {
//    // Take the failures into account
//    // boolean useFullScore = hasSuccessfulNeighbor(allNbhood);
//    // double closestSuccessful = Double.POSITIVE_INFINITY;
//    // int numSuccessful = 0, numFailed = 0;
//    // for (Point pt : points) {
//    //   if (pt.actualScore != null) {
//    //     numSuccessful++;
//    //     closestSuccessful = Math.min(closestSuccessful, distance(pt));
//    //   }
//    //   else if (pt.failureFactor != null) {
//    //     numFailed++;
//    //   }
//    //   else {
//    //     System.err.printf("Neither successful nor failed????\n");
//    //     System.exit(1);
//    //   }
//    // }
//    // 
//    // int numFailedCloser = 0;
//    // for (Point pt : points) {
//    //   if (pt.failureFactor != null) {
//    //     if (distance(pt) < closestSuccessful) {
//    //       numFailedCloser++;
//    //     }
//    //   }
//    // }
//    // 
//    // double failureScaling = 1.0;
//    // if (numFailedCloser > 0) {
//    // // if (!useFullScore) {
//    //   failureScaling = 1.0 / ((double)numFailedCloser);
//    //   // scale = 1.0 - (((double) numFailedCloser) / ((double) numSuccessful + numFailed));
//    //   // scale = scale * scale;
//    //   // System.out.printf("scale: %f\n", scale);
//    // }
//    // else {
//    //   if (numFailedCloser > 0) {
//    //     System.out.printf("Why? nfc:%d  pt:%s\n", numFailedCloser, this);
//    //     Collection<Point> neighbors = allNbhood.wouldBeNeighbors(this);
//    //     for (Point n : neighbors) {
//    //       if (n.actualScore != null) {
//    //         System.out.printf("  ns:%s\n", n);
//    //       }
//    //       else if (n.failureFactor != null) {
//    //         System.out.printf("  nf:%s\n", n);
//    //       }
//    //       else {
//    //         assert(false);
//    //       }
//    //     }
//    //   }
//    // }
//    // 
//    // double minDist = Double.POSITIVE_INFINITY;
//    // for (Point pt : points) {
//    //   minDist = Math.min(minDist, distance(pt));
//    // }
//    // 
//    // double sum = 0.0;
//    // if (false) {
//    //   interpolatedScoreHeuristic(points, false);
//    //   // dispersionHeuristic(points);
//    //   dispersionHeuristicNbhood(justSuccessfulNbhood, slope, magicDist, cov);
//    //   // dispersionHeuristic2(points, 0.01);
//    //   // untestedAreaHeuristic(points, slope);
//    // 
//    //   // double sum = 
//    //   //   predicted.baseVal.doubleValue() + predicted.spacing.doubleValue() +
//    //   //     1.8 * predicted.dispersion.doubleValue();
//    // 
//    //   // double sum = 1.3 * predicted.baseVal / cov + predicted.dispersion;
//    //   sum = 0.85 * predicted.baseVal / cov + predicted.dispersion;
//    // }
//    // else if (false) {
//    //   interpolatedScoreHeuristic(points, false);
//    //   dispersionHeuristicNbhood(justSuccessfulNbhood, slope, magicDist, cov);
//    // 
//    //   sum = 0.85 * predicted.baseVal / cov + predicted.dispersion;
//    // }
//    // else if (false) {
//    //   interpolatedScoreHeuristic(points, false);
//    //   variationHeuristic(points, smoothingFactor);
//    //   // untestedAreaHeuristic2(points, slope, cov);
//    //   untestedAreaHeuristic3(points, refDist, maxScore, cov);
//    // 
//    //   sum = (failureScaling * (predicted.baseVal + predicted.variationScore)) + (2.0 * predicted.spacing);
//    //   // sum = failureScaling * predicted.baseVal;
//    //   // sum = predicted.baseVal;
//    //   // sum = predicted.baseVal + predicted.variationScore + (0.01 * predicted.spacing);
//    // }
//    // else if (false) {
//    //   double modelPrediction      = modelHeuristic(false, justSuccessfulNbhood);
//    //   double nonlinearPrediction  = nonlinearityHeuristic(false, justSuccessfulNbhood);
//    //   double untestedPrediction   = untestedRegionHeuristic(points, slope, cov);
//    //   sum = (failureScaling * (modelPrediction + nonlinearPrediction * minDist)) + untestedPrediction;
//    // }
//    // else if (false) {
//    //   double modelPrediction  = modelHeuristic(false, justSuccessfulNbhood);
//    //   double nonlinearityRate = nonlinearityHeuristic(false, justSuccessfulNbhood);
//    //   double variation = (slope + nonlinearityRate) * cov * minDist;
//    //   double effectiveScore = modelPrediction * failureScaling;
//    //   double max = effectiveScore + variation;
//    //   double min = effectiveScore;
//    //   // double min = Math.max(effectiveScore - variation, 0.0);
//    //   if (max > highestScore) {
//    //     if (min > highestScore) { // this should never happen, but whatever
//    //       sum = 1.0;
//    //     }
//    //     else {
//    //       double betterRange  = max - highestScore;
//    //       double wholeRange   = max - min;
//    //       sum = (betterRange / wholeRange);
//    //     }
//    //   }
//    //   else {
//    //     sum = TuningKnobSearch.epsilon;
//    //   }
//    // }
//    // else if (true) {
//    //   double predictedScore =       modelHeuristic(false, justSuccessfulNbhood);
//    //   double localInaccuracyRate =  localInaccuracyRate(false, justSuccessfulNbhood);
//    //   double absoluteInaccuracy = Math.max(globalInaccuracyRate * cov, localInaccuracyRate) * minDist;
//    //   double pWorseThanBest = Stats.cumulativeNormalDistribution((highestScore - predictedScore) / absoluteInaccuracy);
//    //   double pBetterThanBest = 1 - pWorseThanBest;
//    //   sum = failureScaling * pBetterThanBest;
//    // }
//    // else {
//    //   assert(false);
//    // }
//    // 
//    // // predicted.finalValue = sum * failureScaling;
//    // predicted.finalValue = sum;
//    // 
//    // if (predicted.finalValue.isNaN()) {
//    //   System.out.println("b "+predicted.baseVal+"s "+
//    //     predicted.spacing+"d "+predicted.dispersion);
//    //   System.exit(1);
//    // }
//  }
//
//  // if you re-implement equals you have to re-implement hashCode to get hash-
//  // based data structures to work correctly.
//  public int hashCode()
//  {
//    Iterator dims = this.iterator();
//    Iterator <TuningKnob> knobs = app.getTemplate().getKnobSpace().iterator();
//
//    int code = 1;
//    while (dims.hasNext() && knobs.hasNext()) {
//      Object o = dims.next();
//      TuningKnob knob = knobs.next();
//      code = code * 31 + (knob.getName().hashCode() * 29 + knob.valueToOrd(o));
//    }
//    return code;
//  }
//
//  public String toFancyString() {
//    StringBuffer sb = new StringBuffer();
//    Iterator dims = this.iterator();
//    Iterator <TuningKnob> knobs = app.getTemplate().getKnobSpace().iterator();
//
//    sb.append("[");
//    boolean first = true;
//    while (dims.hasNext() && knobs.hasNext()) {
//      Object o = dims.next();
//      TuningKnob knob = knobs.next();
//      if (!first)
//        sb.append(", ");
//      sb.append("\"" + knob.getName() + "\"=" + o);
//      first = false;
//    }
//    assert(!dims.hasNext()); assert(!knobs.hasNext());
//    sb.append("]");
//    return sb.toString();
//  }
//
//}
//
//// /*
////  * The intension of this heuristic is to more heavily weight candidates that are
////  * far from any tested point.
////  */
//// /* we need the means, which should be calculated by the high value heuristic */
//// void untestedAreaHeuristic(Set<Point> points, double valPerDist)
//// {
////   calcHarmonicMean(points);
////   double h = getHarmonicMeanDist();
////   // taking the square root of the harmonic mean of the distances sometimes
////   // seems to work better.  Not sure why.
////   predicted.spacing = valPerDist * h;
//// }
//// 
//// void untestedAreaHeuristic2(Set<Point> tested, double valPerDist, double cov)
//// {
////   double minDist = Double.MAX_VALUE;
////   for (Point pt : tested) {
////     minDist = Math.min(minDist, distance(pt));
////   }
////   predicted.spacing = valPerDist * cov * minDist;
//// }
//// 
//// void untestedAreaHeuristic3(Set<Point> tested, double refDist, double refScore, double cov)
//// {
////   double minDist = Double.POSITIVE_INFINITY;
////   for (Point pt : tested) {
////     minDist = Math.min(minDist, distance(pt));
////   }
////   predicted.spacing = refScore * cov * cov * cov * cov * 0.001 * (minDist / refDist);
////   // System.out.printf("maxScore: %10f  cov: %10f  minDist: %10f  refDist: %10f\n", maxScore, cov, minDist, refDist);
//// }
//// double nonlinearityHeuristic(boolean allFailedMode, Neighborhood justSuccessfulNbhood)
//// {
////   if (allFailedMode) {
////     assert(false);
////     return Double.NaN;
////   }
////   else {
////     double scoreTotal = 0.0;
////     double invDistTotal = 0.0;
////     for (Point neighbor : justSuccessfulNbhood.wouldBeNeighbors(this)) {
////       assert(neighbor.actualScore != null);
////       double invDist = 1.0 / distance(neighbor);
////       // scoreTotal += neighbor.predicted.scoreSmoothedDiff * invDist;
////       scoreTotal += neighbor.predicted.inaccuracyRate * invDist;
////       invDistTotal += invDist;
////     }
////     return scoreTotal / invDistTotal;
////   }
//// }
//// 
//// /*
////  *
////  */
//// void variationHeuristic(Set<Point> tested, double smoothingFactor)
//// {
////   double scoreTotal = 0.0, weightTotal = 0.0;
////   for (Point other : tested) {
////     if (other.predicted.scoreSmoothedDiff != null) {
////       double inverseDist = 1.0 / distance(other);
////       scoreTotal  += inverseDist * other.predicted.scoreSmoothedDiff;
////       weightTotal += inverseDist;
////     }
////   }
////   predicted.variationScore = scoreTotal / weightTotal;
//// }
//// 
//// /*
////  * This heuristic gives the standard deviation of the values of all the tested
////  * points, weighted by the inverse distance from the given candidate point.
////  */
//// /* we need the means, which should be calculated by the high value heuristic */
//// void dispersionHeuristic(Set<Point> selected)
//// {
////   initInverseDistances(selected, false);
//// 
////   if (predicted.baseVal != null) {
////     double mean = predicted.baseVal.doubleValue();
//// 
////     double weight = 0.0;
////     // This loop calculates the weighted variance.  Take the square root to
////     // get the standard deviation.
////     for (Point other : selected) {
////       if (other.actualScore != null) {
////         double temp = other.actualScore.doubleValue() - mean;
////         temp = temp * temp * niDistance(other);
////         weight += temp;
////       }
////     }
//// 
////     predicted.dispersion = new Double(Math.sqrt(weight));
////   }
//// }
//// 
//// void dispersionHeuristic2(Set<Point> points, double magicNumber)
//// {
////   double total = 0.0;
////   Point p2 = null;
////   int count = 0;
////   for (Point p1 : points) {
////     if (p1.actualScore != null) {
////       if (p2 != null) {
////         double d1 = distance(p1);
////         double d2 = distance(p2);
////         double d3 = p1.distance(p2);
////         double vDiff = Math.abs(p1.actualScore - p2.actualScore);
////         total += vDiff / ((0.01 + d1 + d2 - d3) * d3);
////         count++;
////       }
////       p2 = p1;
////     }
////     // if (p1.actualScore != null) {
////     //   double d1 = distance(p1);
////     //   for (Point p2 : points) {
////     //     if (p2.actualScore != null && !p1.equals(p2)) {
////     //       double d2 = distance(p2);
////     //       double d3 = p1.distance(p2);
////     //       double vDiff = Math.abs(p1.actualScore - p2.actualScore);
////     //       total += vDiff / (d1 + d2 + Math.max(d1, d2) - d3);
////     //     }
////     //   }
////     // }
////   }
////   // disps.add(total/count);
////   predicted.dispersion = new Double(magicNumber * (total / count));
////   // predicted.dispersion = new Double(0.0);
////   // System.out.printf("new %f  ", total / count);
//// }
//// 
//// void dispersionHeuristicNbhood(Neighborhood nbhood, double magicSlope, double magicDist, double cov)
//// {
////   Set<Point> ns = nbhood.wouldBeNeighbors(this);
//// 
////   Set<Pair<Point,Point>> neighborConns = new HashSet<Pair<Point,Point>>();
//// 
////   double totalInvDist = 0.0;
////   for (Point neighbor : ns) {
////     totalInvDist += 1.0 / distance(neighbor);
////     if (false) { // this is for the neighbors of neighbors version
////       for (Point neighborsNeighbor : nbhood.get(neighbor)) {
////         Pair<Point,Point> p;
////         if (neighbor.compareTo(neighborsNeighbor) < 0) {
////           p = Tuple.from(neighbor, neighborsNeighbor);
////         }
////         else {
////           p = Tuple.from(neighborsNeighbor, neighbor);
////         }
////         neighborConns.add(p);
////       }
////     }
////   }
////   double harmonicMeanDist = ((double)ns.size()) / totalInvDist;
//// 
////   double slopeToUse = -1000.0;
////   if (true) {
////     double totalSlope = 0.0;
////     double totalWeight = 0.0;
////     for (Map.Entry<Point, Set<Point>> entry : nbhood.entrySet()) {
////       Point x = entry.getKey();
////       for (Point y : entry.getValue()) {
////         if (x.compareTo(y) < 0) { // avoid double counting connections
////           double dx = distance(x), dy = distance(y);
////           double smallerDist = Math.min(dx, dy);
////           double weight = (1.0/smallerDist) * x.distance(y) / (dx + dy);
////           double slope = Math.abs(x.actualScore - y.actualScore) / x.distance(y);
////           totalSlope += slope * weight;
////           totalWeight += weight;
////         }
////       }
////     }
////     slopeToUse = totalSlope / totalWeight;
////   }
////   else { // this is the neighbors of neighbors version
////     double totalSlope = 0.0;
////     double totalWeight = 0.0;
////     for (Pair<Point,Point> connection : neighborConns) {
////       Point x = Tuple.get1(connection);
////       Point y = Tuple.get2(connection);
////       double slope = Math.abs(x.actualScore - y.actualScore) / x.distance(y);
////       double weight = x.distance(y) / (distance(x) + distance(y));
////       totalSlope += slope * weight;
////       totalWeight += weight;
////     }
////     double avgSlope = totalSlope / totalWeight;
////   }
//// 
////   // predicted.dispersion = slopeToUse * (harmonicMeanDist + 0.01 * magicDist / cov);
////   predicted.dispersion = slopeToUse * (harmonicMeanDist + (24 * cov));
//// 
////   // System.out.printf("b %f  s %f  d %f  s * d %f\n", 
////   //   predicted.baseVal, slopeToUse, harmonicMeanDist, slopeToUse * harmonicMeanDist);
//// }
//
//// void calcSmoothedScore(Set<Point> tested, double smoothingFactor, Neighborhood justSuccessfulNbhood) {
////   double scoreTotal = 0.0, weightTotal = 0.0;
////   for (Point p2 : justSuccessfulNbhood.get(this)) {
////     if (p2.actualScore != null) {
////       double smoothedInverseDist = 1.0 / (smoothingFactor + distance(p2));
////       scoreTotal += smoothedInverseDist * p2.actualScore;
////       weightTotal += smoothedInverseDist;
////     }
////   }
////   predicted.smoothedScore = scoreTotal / weightTotal;
////   predicted.scoreSmoothedDiff = Math.abs(actualScore - predicted.smoothedScore);
//// }
//// 
//// /*
////  *
////  */
//// static void calcSmoothedScores(Set<Point> tested, double smoothingFactor, Neighborhood justSuccessfulNbhood)
//// {
////   double epsilon = 0.0000001;
////   if (smoothingFactor < epsilon) {
////     assert(false);
////   }
////   for (Point p1 : tested) {
////     if (p1.actualScore != null) {
////       p1.calcSmoothedScore(tested, smoothingFactor, justSuccessfulNbhood);
////     }
////   }
//// }
//// 
//// double simpleLinearModel(Set<Point> tested, Neighborhood nbhood, boolean allFailedMode) {
////   if (allFailedMode) {
////     
////   }
////   else {
////     Collection<Point> neighbors = nbhood.wouldBeNeighbors(this);
////     for (Point neighbor : neighbors) {
////       Collection<Point> secondaries = nbhood.get(neighbors);
////       for (Point secondary : secondaries) {
////         if (Mesh.zBetween(this, secondary, neighbor)) {
////           // extrapolation case
////         }
////         else {
////           // interpolation case
////         }
////       }
////     }
////   }
////   return 1.0;
//// }
//// 
//// /*
////  * This is the "base" heuristic that just builds a very simple model of the
////  * score function based on the tested points and.  In particular, it gives the
////  * arithmetic mean value of all the tested points, weighted by the inverse
////  * distance from the given candidate point.
////  */
//// void interpolatedScoreHeuristic(Set<Point> selected, boolean allFailedMode)
//// {
////   // double weight = 0.0;
////   double scoreTotal = 0.0;
////   double invDistTotal = 0.0;
////   // initInverseDistances(selected, allFailedMode);
////   if (allFailedMode) {
////     for (Point other : selected) {
////       double score = Double.NaN;
////       if (other.failureFactor == null) {
////         // FIXME: this should be calculated from the data somehow
////         score = 1000.0;
////       }
////       else {
////         score = 1.0 / other.failureFactor;
////       }
////       // weight += niDistance(other) * score;
////       double invDist = 1.0 / distance(other);
////       scoreTotal    += invDist * score;
////       invDistTotal  += invDist;
////     }
////   }
////   else {
////     for (Point other : selected) {
////       if (other.actualScore != null) {
////         double score = other.actualScore;
////         double invDist = 1.0 / distance(other);
////         scoreTotal    += invDist * score;
////         invDistTotal  += invDist;
////       }
////     }
////   }
////   // predicted.baseVal = weight;
////   predicted.baseVal = scoreTotal / invDistTotal;
//// }
//// 
//// void interpolatedScoreHeuristic2(Set<Point> selected, boolean allFailedMode)
//// {
////   double closestDist = Double.POSITIVE_INFINITY;
////   double closestScore = Double.NaN;
////   if (allFailedMode) {
////     for (Point other : selected) {
////       double score = Double.NaN;
////       if (other.failureFactor == null) {
////         // FIXME: this should be calculated from the data somehow
////       }
////       else {
////         double dist = distance(other);
////         if (dist < closestDist) {
////           closestDist = dist;
////           closestScore = 1.0 / other.failureFactor;
////         }
////       }
////     }
////   }
////   else {
////     for (Point other : selected) {
////       if (other.actualScore != null) {
////         double dist = distance(other);
////         if (dist < closestDist) {
////           closestDist = dist;
////           closestScore = other.actualScore;
////         }
////       }
////     }
////   }
////   predicted.baseVal = closestScore;
//// }
//
//// /* Calculates the harmonic mean of the distances between this point and the
////  * points in the set passed in.  The result is saved in a field. */
//// void calcHarmonicMean(Set<Point> points)
//// {
////   int n = 0;
////   double invDistTotal = 0.0;
//// 
////   for (Point other : points) {
////     /* Only consider points that have been tested */
////     if (app.getObjectiveReading(other) != null) {
////       n++;
////       invDistTotal += 1 / distance(other);
////     }
////   }
//// 
////   predicted.harmonicMeanDistance = new Double(n / invDistTotal);
//// }
//// 
//// public double getHarmonicMeanDist() { return predicted.harmonicMeanDistance.doubleValue(); }
//// 
//// /* Calculates the sum of the inverse distances between this point and the
////  * points in the set "points".  The result is saved in a field for reuse. */
//// void initInverseDistances(Set<Point> points, boolean allFailedMode)
//// {
////   double d = 0.0;
////   for (Point r : points) {
////     // only count points for which testing is complete
////     if (app.getObjectiveReading(r) != null || (r.failureFactor != null && allFailedMode))
////       d += 1.0 / distance(r);
////   }
////   predicted.inverseDistancesTotal = new Double(d);
//// }
//// 
//// double getInverseDistances() { return predicted.inverseDistancesTotal.doubleValue(); }
//// 
//// /* Normalized inverse distance */
//// double niDistance(Point other)
//// {
////   double n = (1.0 / distance(other)) / getInverseDistances();
////   if (new Double(n).isNaN()) {
////     System.out.println("nid d="+distance(other)+" i="+getInverseDistances());
////   }
////   return n;
//// }
//// 
//
//// double localInaccuracyRate(Sensor sensor, Neighborhood nbhood)
//// {
////   double scoreTotal   = 0.0;
////   double invDistTotal = 0.0;
////   for (Point neighbor : justSuccessfulNbhood.wouldBeNeighbors(this)) {
////     assert(neighbor.actualScore != null);
////     double invDist = 1.0 / distance(neighbor);
////     scoreTotal    += neighbor.predicted.inaccuracyRate * invDist;
////     invDistTotal  += invDist;
////   }
////   return scoreTotal / invDistTotal;
//// }
//
//// void allFailedHeuristics(Neighborhood allNbhood, Collection<Point> tested, double slope, double cov)
//// {
////   double modelPrediction    = modelHeuristic(true, allNbhood);
////   double untestedPrediction = untestedRegionHeuristic(tested, slope, cov);
////   if ((new Double(modelPrediction)).isNaN()) {
////     System.out.printf("bad!\n");
////     System.exit(1);
////   }
////   // System.out.printf("Foo %f\n", predicted.baseVal);
////   predicted.finalValue = modelPrediction + untestedPrediction;
//// }
//
//// double untestedRegionHeuristic(Collection<Point> tested, double refSlope, double cov)
//// {
////   double minDist = Double.POSITIVE_INFINITY;
////   for (Point pt : tested) {
////     minDist = Math.min(minDist, distance(pt));
////   }
////   // System.out.printf("maxScore: %10f  cov: %10f  minDist: %10f  refDist: %10f\n", maxScore, cov, minDist, refDist);
////   return refSlope * Math.pow(cov, 1.0) * minDist;
//// }
//
//// boolean hasSuccessfulNeighbor(Neighborhood nbhood)
//// {
////   Collection<Point> neighbors = nbhood.wouldBeNeighbors(this);
////   boolean foundSuccess = false;
////   int numSuccessful = 0, numFailed = 0;
////   for (Point n : neighbors) {
////     if (n.actualScore != null) {
////       if (n.failureFactor != null) {
////         System.out.printf("\n\nWHAT??? %f %f\n\n", n.actualScore, n.failureFactor);
////       }
////       foundSuccess = true;
////       numSuccessful++;
////       // break;
////     }
////     else if (n.failureFactor != null) {
////       numFailed++;
////     }
////     else {
////       assert(false);
////     }
////   }
////   // System.out.printf("number of neighbors: %d   s:%d    f:%d   fs:%b\n",
////   //    neighbors.size(), numSuccessful, numFailed, foundSuccess);
////   return numSuccessful > numFailed;
////   // return foundSuccess;
//// }
//   
