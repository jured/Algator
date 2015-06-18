package si.fri.algotest.entities;

import java.io.File;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author tomaz
 */
public class EComputerFamily extends Entity  implements Serializable {
  // Entity identifier
  public static final String ID_ResultParameter   = "Family";
  
  // Fields
  public static final String ID_FamilyID     = "FamilyID";     // String
  public static final String ID_Name         = "Name";         // String
  public static final String ID_Desc         = "Description";  // String
  public static final String ID_Platform     = "Platform";     // String
  public static final String ID_Hardware     = "Hardware";     // String
  public static final String ID_SystemType   = "SystemType";   // String  (32 or 64)
  

  
  public EComputerFamily() {
   super(ID_ResultParameter, 
	 new String [] {ID_FamilyID, ID_Name, ID_Desc, ID_Platform, ID_Hardware, ID_SystemType});
  
   setRepresentatives(ID_FamilyID, ID_Name, ID_Desc);
  }
  
  
  public EComputerFamily(String json) {
    this();
    initFromJSON(json);
  }
}
