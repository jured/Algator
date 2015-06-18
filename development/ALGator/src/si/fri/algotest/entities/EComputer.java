package si.fri.algotest.entities;

import java.io.File;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author tomaz
 */
public class EComputer extends Entity  implements Serializable {
  // Entity identifier
  public static final String ID_ResultParameter   = "Computer";
  
  // Fields
  public static final String ID_ComputerID   = "ComputerID";   // String
  public static final String ID_FamilyID     = "FamilyID";     // String
  public static final String ID_Name         = "Name";         // String
  public static final String ID_Desc         = "Description";  // String
  public static final String ID_CompIP       = "CompIP";       // String
  public static final String ID_Capabilities = "Capabilities"; // CompCap[]

  
  // The capabilities of this computer
  private Set<ComputerCapability> computerCapabilities;
  
  public EComputer() {  
   super(ID_ResultParameter, 
	 new String [] {ID_ComputerID, ID_FamilyID, ID_Name, ID_Desc, ID_CompIP, ID_Capabilities});
  
   setRepresentatives(ID_ComputerID, ID_FamilyID, ID_Name, ID_Desc, ID_CompIP, ID_Capabilities);
  }
  
  public EComputer(String json) {
    this();
    initFromJSON(json);
  }

  public Set<ComputerCapability> getCapabilities() {    
    if (computerCapabilities == null) {
      TreeSet result = new TreeSet();
      String [] cap = getStringArray(ID_Capabilities);
      for (int i = 0; i < cap.length; i++) 
        result.add(ComputerCapability.getComputerCapability(cap[i])); 
    
      computerCapabilities = result;
    }
    return computerCapabilities;
  }
}
