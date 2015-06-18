package si.fri.algotest.entities;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONObject;
import si.fri.algotest.global.ATGlobal;

/**
 *
 * @author tomaz
 */
public class EGlobalConfig extends Entity  implements Serializable {
  // a single instance of a global configuration file
  private static EGlobalConfig config;
  
  // Entity identifier
  public static final String ID_ResultParameter   = "Config";
  
  // Fields
  public static final String ID_ALGatorID  = "ALGatorID";  // String
  public static final String ID_Families   = "Families";   // Family[]
  public static final String ID_Computers  = "Computers";  // Computer[]

  private ArrayList<EComputer>       computers;
  private ArrayList<EComputerFamily> families;
  
  public EGlobalConfig() {  
   super(ID_ResultParameter, 
	 new String [] {ID_Families, ID_Computers});
  
   setRepresentatives(ID_ALGatorID);
  }
  
  public EGlobalConfig(File fileName) {
    this();
    initFromFile(fileName);
  }
  
  
  public ArrayList<EComputer> getComputers() {
    if (computers == null) {
      computers = new ArrayList<>();      
      try {
        JSONArray ja = getField(ID_Computers);
      
      
        for (int i = 0; i < ja.length(); i++) {
	  JSONObject jo = ja.getJSONObject(i);
	  computers.add(new EComputer(jo.toString()));
        }
      } catch (Exception e) {
        // ignore, if an error occures while parsing the ID_Computers parameter, 
      }
    }    
    return computers;
  }
  

  public ArrayList<EComputerFamily> getFamilies() {
    if (families == null) {
      families = new ArrayList<>();      
      try {
        JSONArray ja = getField(ID_Families);
      
      
        for (int i = 0; i < ja.length(); i++) {
	  JSONObject jo = ja.getJSONObject(i);
	  families.add(new EComputerFamily(jo.toString()));
        }
      } catch (Exception e) {
        // ignore, if an error occures while parsing the ID_Families parameter
      }
    }    
    return families;
  }

  
  public static EGlobalConfig getConfig() {
    if (config == null) {
      config = new EGlobalConfig(new File(ATGlobal.getGlobalConfigFilename()));
    }
    return config;
  }  
}
