package si.fri.algotest.entities;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author tomaz
 */
public class EVariable extends Entity  implements Serializable {
  // Entity identifier
  public static final String ID_Variable   = "Variable";
  
  // Fields
  public static final String ID_Desc     = "Description";
  public static final String ID_Type     = "Type";
  public static final String ID_Subtype  = "Subtype";
  public static final String ID_Value    = "Value";
  

  private VariableType type;
  private String subtype; 
  
  public EVariable() {
   super(ID_Variable, 
	 new String [] {ID_Desc, ID_Type, ID_Subtype, ID_Value});
  
   setRepresentatives(ID_Value);
  }
  
  public EVariable(File fileName) {
    this();
    initFromFile(fileName);
    
    setTypeAndSubtype();
  }
  
  public EVariable(String json) {
    this();
    initFromJSON(json);

    setTypeAndSubtype();
  }

  public EVariable(String name, VariableType type, Object value) {
    this(name, "", type, value);
  }
    
  public EVariable(String name, String desc, VariableType type, Object value) {
    this();
    
    setName(name);
    set(ID_Desc, desc);
    set(ID_Value, value);

    this.type = type;
    if (type != null)
      set(ID_Type, type.toString());
  }
  
  @Override
  public void set(String fieldKey, Object object) {
    if (!fieldKey.equals(ID_Value))
      super.set(fieldKey, object); 
    else {
      try {
        switch (this.type) { 
	  case INT: case TIMER:
	    fields.put(fieldKey, Integer.parseInt((String) object));
	    break; 
	  case DOUBLE:
	    fields.put(fieldKey, Double.parseDouble((String) object));
	    break;
	  default:
	    fields.put(fieldKey, object);
	}
      } catch (Exception e) {
	fields.put(fieldKey, object);
      }
    }
  }
  
  
  /**
   * Method detects a type of a parameter and sets the values of the {@code type} 
   * field and its {@code subtype} field.
   */
  private void setTypeAndSubtype() {
    String typeDesc = getField(ID_Type);
    this.type = VariableType.getType(typeDesc);   
    
    String subty = getField(ID_Subtype);
    if (subty!= null && !subty.isEmpty())
      this.subtype = subty;
  }

  public VariableType getType() {
    return type;
  }

  public String getSubtype() {
    return subtype;
  }
  
  
  public Object getValue() {
    Object value = get(ID_Value);
    
    // getValue() method for parameters of type DOUBLE returns a value with limited number of decimals (given in subtype) 
    if (type.equals(VariableType.DOUBLE)) {
      int decimals = 2;
      try {
        decimals = Integer.parseInt(subtype);        
      } catch (Exception e) {}
      
      try {
        Double d = null;
        if (value instanceof String)
          d = Double.parseDouble((String) value);
        else if (value instanceof Double)
          d = (Double) value;
        else return value;
        
        double potenca = Math.pow(10, decimals);
        value = Math.round(d * potenca)/potenca;
      } catch (NumberFormatException e) {
        return value; 
      }      
    }
    return value;
  }

    
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof EVariable && ((EVariable)obj).getName().equals(this.getName()));
  }  
}
