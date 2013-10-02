/*
 *
 */

import java.util.regex.*;
import com.mallardsoft.tuple.*;

public abstract class Algebraic<InValueType, InPredictionType, OutValueType, OutPredictionType>
  extends Calculated<OutValueType, OutPredictionType>
{
  protected Feature<InValueType, InPredictionType> left, right;

  protected OutPredictionType calcPrediction(ValueAndPredictionSource source, Point p)
  {
    InPredictionType p1 = left.getPrediction(source, p);
    InPredictionType p2 = right.getPrediction(source, p);
    // if (name.equals("betterThanBest")||name.equals("appRunTime"))
      // {System.out.printf("Algebraic prediction for %s %s %s\n", name, p1, p2);}
    // if (p1 != null && p2 != null) {
      return calcAlgebraicPrediction(p1, p2);
    // }
    // if (this instanceof Comparison) {
    //   if (false) { System.out.printf("l=%s r=%s\n", p1, p2); }
    // }
    // return null;
  }

  protected OutValueType calcValue(ValueSource source, Point p)
  {
    InValueType v1 = left.getValue(source, p);
    InValueType v2 = right.getValue(source, p);
    return calcAlgebraicValue(v1, v2);
  }

  abstract protected OutPredictionType calcAlgebraicPrediction(InPredictionType p1, InPredictionType p2);
  abstract protected OutValueType calcAlgebraicValue(InValueType v1, InValueType v2);

  protected Algebraic(Feature<InValueType, InPredictionType> l, Feature<InValueType, InPredictionType> r, String n)
  {
    super(n);
    assert(l != null);
    assert(r != null);
    left = l;
    right = r;
  }

  public int compareTo(Feature f)
  {
    if (f == null) { return 1; }
    else {
      int magicDiff = magicNumber() - f.magicNumber();
      if (magicDiff == 0) {
        Algebraic other = null;
        try { other = (Algebraic)f; }
        catch (ClassCastException e) { assert(false); }
        int n = name.compareTo(other.getName());
        if (n == 0) {
          int l = left.compareTo(other.left);
          if (l == 0) {
            return right.compareTo(other.right);
          }
          else {
            return l;
          }
        }
        else {
          return n;
        }
      }
      return magicDiff;
    }
  }

  // public static Pair<Sensor, String> parseSensor(Application app, String sensorSpec, Integer spreadsheetCol)
  // {
  //   System.out.printf("parsing \"%s\"\n", sensorSpec);
  //   Pattern wordPat = Pattern.compile("\\w*");
  //   Matcher wordMatcher = wordPat.matcher(sensorSpec.trim());
  //   if (wordMatcher.find()) {
  //     String firstWord = wordMatcher.group();
  //     System.out.printf("  parsing \"%s\"\n", firstWord);
  //     if (firstWord.toUpperCase().equals("TIMES")) {
  //       String timesSpec = sensorSpec.substring(wordMatcher.end()+1).trim();
  //       System.out.printf("    parsing TIMES  \"%s\"\n", timesSpec);
  //       if (timesSpec.startsWith("-")) {
  //         String minusDash = timesSpec.substring(timesSpec.indexOf("-")+1).trim();
  //         Matcher nameMatcher = wordPat.matcher(minusDash);
  //         if (nameMatcher.find()) {
  //           String sensorName = nameMatcher.group();
  //           String minusName  = minusDash.substring(nameMatcher.end()).trim();
  //           System.out.printf("      parsing TIMES  %s  \"%s\"\n", sensorName, minusName);
  //           if (minusName.startsWith("(")) {
  //             String minusParen = minusName.substring(minusName.indexOf("(")+1).trim();
  //             Pair<Sensor, String> firstSensorPlusRemainder = parseSensor(app, minusParen, null);
  //             Sensor firstSensor    = Tuple.get1(firstSensorPlusRemainder);
  //             String remainingSpec  = Tuple.get2(firstSensorPlusRemainder);
  //             if (remainingSpec.startsWith(",")) {
  //               String minusComma = remainingSpec.substring(remainingSpec.indexOf(",")+1).trim();
  //               Pair<Sensor, String> secondSensorPlusRemainder = parseSensor(app, minusComma, null);
  //               Sensor secondSensor = Tuple.get1(secondSensorPlusRemainder);
  //               String suffixSpec   = Tuple.get2(secondSensorPlusRemainder);
  //               if (suffixSpec.startsWith(")")) {
  //                 String minusCloseParen = suffixSpec.substring(suffixSpec.indexOf(")")+1).trim();
  //                 System.out.printf("  building a times sensor(%s, %s)\n", firstSensor, secondSensor);
  //                 Sensor ts = new TimesSensor(firstSensor, secondSensor, sensorName, spreadsheetCol);
  //                 return Tuple.from(ts, minusCloseParen);
  //                 // return Tuple.from("foo", minusCloseParen);
  //               }
  //               else {
  //                 System.err.printf("Bad TIMES spec %s\n", sensorSpec);
  //                 System.exit(-1);
  //               }
  //             }
  //             else {
  //               System.err.printf("Bad TIMES spec %s\n", sensorSpec);
  //               System.exit(-1);
  //             }
  //           }
  //           else {
  //             System.err.printf("Bad TIMES spec %s\n", sensorSpec);
  //             System.exit(-1);
  //           }
  //         }
  //         else {
  //           System.err.printf("Bad TIMES spec %s\n", sensorSpec);
  //           System.exit(-1);
  //         }
  //       }
  //       else {
  //         System.err.printf("Bad TIMES spec %s\n", sensorSpec);
  //         System.exit(-1);
  //       }
  //     }
  //     else {
  //       Sensor sensorRef = app.lookupSensor(firstWord);
  //       if (sensorRef == null) {
  //         System.err.printf("Reference to undefined sensor \"%s\"\n", firstWord);
  //         System.exit(1);
  //       }
  //       String remainingSpec = sensorSpec.substring(wordMatcher.end());
  //       System.out.printf("    Parsed reference to sensor %s\n", sensorRef);
  //       return Tuple.from(sensorRef, remainingSpec);
  //     }
  //   }
  //   else {
  //     return Tuple.from(null, sensorSpec);
  //   }
  //   System.err.printf("Bad TIMES spec %s\n", sensorSpec);
  //   System.exit(-1);
  //   return null;
  // }
}
