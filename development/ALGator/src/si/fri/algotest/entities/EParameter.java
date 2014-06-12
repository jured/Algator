package si.fri.algotest.entities;

import java.io.File;

/**
 *
 * @author tomaz
 */
public class EParameter extends Entity {
  // Entity identifier
  public static final String ID_ResultParameter   = "Parameter";
  
  // Fields
  public static final String ID_Name     = "Name";
  public static final String ID_Desc     = "Description";
  public static final String ID_Type     = "Type";
  public static final String ID_Subtype  = "Subtype";
  public static final String ID_Value    = "Value";
  

  private ParameterType type;
  private String subtype; 
  
  public EParameter() {
   super(ID_ResultParameter, 
	 new String [] {ID_Name, ID_Desc, ID_Type, ID_Subtype, ID_Value});
  }
  
  public EParameter(File fileName) {
    this();
    initFromFile(fileName);
    
    setRepresentatives(ID_Name, ID_Value);
    setTypeAndSubtype();
  }
  
  public EParameter(String json) {
    this();
    initFromJSON(json);

    setRepresentatives(ID_Name, ID_Value);
    setTypeAndSubtype();
  }

  public EParameter(String name, String desc, ParameterType type, Object value) {
    this();
    
    set(ID_Name, name);
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
    this.type = ParameterType.getType(typeDesc);   
    
    String subty = getField(ID_Subtype);
    if (subty!= null && !subty.isEmpty())
      this.subtype = subty;
  }

  public ParameterType getType() {
    return type;
  }

  public String getSubtype() {
    return subtype;
  }
  

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof EParameter && ((EParameter)obj).getField(ID_Name).equals(this.getField(ID_Name)));
  }  
}