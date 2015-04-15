package si.fri.algotest.entities;

/**
 *
 * @author tomaz
 */
public enum GraphType {

  UNKNOWN {
    public String toString() {
      return "unknown";
    }    
  }, 
  
  LINE{
    public String toString() {
      return "line";
    }    
  }, 

  STAIR{
    public String toString() {
      return "stair";
    }    
  }, 
  
  BAR{
    public String toString() {
      return "bar";
    }    
  }, 
  
  BOX{
    public String toString() {
      return "box";
    }    
  },
  
  PIE{
    public String toString() {
      return "pie";
    }    
  },
  
  AREA{
    public String toString() {
      return "area";
    }    
  },  
  
  DONUT{
    public String toString() {
      return "donut";
    }    
  };
  
  static GraphType getType(String typeDesc) {
    for (GraphType type : GraphType.values())
      if (typeDesc.equalsIgnoreCase(type.toString())) 
	return type;
    return UNKNOWN;
  }
}
