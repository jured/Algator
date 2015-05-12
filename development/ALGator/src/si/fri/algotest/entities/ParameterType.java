package si.fri.algotest.entities;

/**
 * A type of a result parameter.
 *
 * @author tomaz
 */
public enum ParameterType {

  UNKNOWN, TIMER, COUNTER, INT, DOUBLE, STRING;

  @Override
  public String toString() {
    switch (this) {
      case UNKNOWN:
        return "unknown";
      case TIMER:
        return "timer";
      case COUNTER:
        return "counter";
      case INT:
        return "int";
      case DOUBLE:
        return "double";
      case STRING:
        return "string";
      default:
        return "/unknown/";
    }
  }

  static ParameterType getType(String typeDesc) {
    for (ParameterType rst : ParameterType.values()) {
      if (typeDesc.equals(rst.toString())) {
        return rst;
      }
    }
    return UNKNOWN;
  }

  /**
   * The default value for parameters of this type
   */
  public Object getDefaultValue() {
    switch (this) {
      case TIMER:
      case COUNTER:
      case INT:
        return 0;
      case DOUBLE:
        return 0.0;
      case STRING:
        return "";
    }
    return null;
  }

}
