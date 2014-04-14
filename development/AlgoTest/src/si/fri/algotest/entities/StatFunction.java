package si.fri.algotest.entities;

import java.util.ArrayList;

/**
 *
 * @author tomaz
 */
public enum StatFunction {

  UNKNOWN {
            public String toString() {
              return "unknown";
            }
          },
  MIN {
            public String toString() {
              return "MIN";
            }
          },
  MAX {
            public String toString() {
              return "MAX";
            }
          },
  AVG {
            public String toString() {
              return "AVG";
            }
          },
  SUM {
            public String toString() {
              return "SUM";
            }

          },
  
    FIRST {
            public String toString() {
              return "FIRST";
            }

          },
    LAST {
            public String toString() {
              return "LAST";
            }

          };

  public static StatFunction getStatFunction(String desc) {
    for (StatFunction sfnc : StatFunction.values()) {
      if (desc.equals(sfnc.toString())) {
        return sfnc;
      }
    }
    return UNKNOWN;
  }

  /**
   * Calculates the value of given {@code funcion} mapped over {@code vallues}.
   */
//  public static long getFunctionValue(StatFunction function, long[] values) {
//    if (values == null || values.length == 0) {
//      return -1;
//    }
//
//    long val = values[0];
//    switch (function) {
//      case MIN:
//      case MAX:
//        for (int i = 1; i < values.length; i++) {
//          if ((function.equals(MIN) && values[i] < val) || (function.equals(MAX) && values[i] > val)) {
//            val = values[i];
//          }
//        }
//        return val;
//      case SUM:
//      case AVG:
//        for (int i = 1; i < values.length; i++) {
//          val += values[i];
//        }
//        if (function.equals(SUM)) {
//          return val;
//        } else {
//          return val / values.length;
//        }
//      default:
//        return -1;
//    }
//  }

  public static Number getFunctionValue(StatFunction function, ArrayList<? extends Comparable> values) {
    if (values == null || values.size() == 0 || !(values.get(0) instanceof Number)) {
      return -1;
    }

    switch (function) {
      case FIRST:
        return (Number) values.get(0);
      case LAST:
        return (Number) values.get(values.size()-1);
      case MIN:
      case MAX:
        Comparable val = values.get(0);
        for (int i = 1; i < values.size(); i++) {
          if ((function.equals(MIN) && values.get(i).compareTo(val) < 0) || (function.equals(MAX) && values.get(i).compareTo(val) > 0)) {
            val = values.get(i);
          }
        }
        return (Number) val;
      case SUM:
      case AVG:
        try {
          double valN = ((Number) values.get(0)).doubleValue();
          for (int i = 1; i < values.size(); i++) {
            valN += ((Number) values.get(i)).doubleValue();
          }

          if (function.equals(AVG)) {
            valN /= values.size();
          }
          if (values.get(0) instanceof Integer) {
            return new Double(valN).intValue();
          } else if (values.get(0) instanceof Long) {
            return new Double(valN).longValue();
          } else {
            return valN;
          }
        } catch (Exception e) {
          // this exception will probably occur only if values are not Numbers
          return -1;
        }
      default:
        return -1;
    }
  }

}
