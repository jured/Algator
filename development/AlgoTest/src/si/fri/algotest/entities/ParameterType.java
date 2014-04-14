package si.fri.algotest.entities;

/**
 * A type of a result parameter. 
 * @author tomaz
 */
public enum ParameterType {
  UNKNOWN {
    public String toString() {
      return "unknown";
    }    
  }, 
  
  TIMER{
    public String toString() {
      return "timer";
    }    
  }, 
  
  COUNTER{
    public String toString() {
      return "counter";
    }    
  }, 
  
  INT {
    public String toString() {
      return "int";
    }  
  }, 
  
  DOUBLE {
    public String toString() {
      return "double";
    }
  },
  
  STRING {
    public String toString() {
      return "string";
    }
  };
    
  static ParameterType getType(String typeDesc) {
    for (ParameterType rst : ParameterType.values())
      if (typeDesc.equals(rst.toString())) 
	return rst;
    return UNKNOWN;
  }
  
}
