package si.fri.algotest.entities;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
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
  public static final String ID_ALGatorID  = "ALGatorID";          // String
  public static final String ID_Families   = "ComputerFamilies";   // Family[]

  private ArrayList<EComputerFamily> families;
  
  public EGlobalConfig() {  
   super(ID_ResultParameter, 
	 new String [] {ID_Families});
  
   setRepresentatives(ID_ALGatorID);
  }
  
  public EGlobalConfig(File fileName) {
    this();
    initFromFile(fileName);
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
      if (families.isEmpty()) {
        EComputerFamily cef = new EComputerFamily("{\"FamilyID\":\"F0\",\"Computers\":[{\"ComputerID\":\"C0\",\"Capabilities\":[\"AEE_EM\",\"AEE_CNT\",\"AEE_JVM\"]}]}");
        families.add(cef);
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
