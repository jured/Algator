package si.fri.algotest.entities;

import java.io.Serializable;

/**
 *
 * @author tomaz
 */
public enum MeasurementType implements Serializable {
  EM {
    @Override
    public String toString() {
      return "Regular mesurement of parameters and timers";
    }
    
  }, 
  
  JVM {
    @Override
    public String toString() {
      return "Measurement of JVM parameters";
    }
    
  }, 
  
  CNT {
    @Override
    public String toString() {
      return "Measurement of user defined counters";
    }
    
  };

  /**
   * Returns the extension to the result file for a  measurement of a given type
   * (results/AlgName-TestSetname.extension)
   */
  public String getExtension() {
    switch (this) {
      case EM:  return "em";
      case JVM: return "jvm";
      case CNT: return "cnt";	
    }
    return "";
  }
}
