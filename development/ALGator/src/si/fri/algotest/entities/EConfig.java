package si.fri.algotest.entities;

import java.io.File;
import si.fri.algotest.global.ATGlobal;



/**
 *
 * @author tomaz
 */
public class EConfig extends Entity {
  
  private static EConfig config;
  
  // Entity identifier
  public static final String ID_Query             = "Config";  
  
  //Fields
  public static final String ID_COMPID            = "CompID";	     // String
  public static final String ID_VMEP              = "VMEP";	     // String
  public static final String ID_VMEPClasspath     = "VMEPClasspath"; // String
  
  
  public EConfig() {
   super(ID_Query, 
	 new String [] {ID_COMPID, ID_VMEP, ID_VMEPClasspath});
   setRepresentatives(ID_COMPID);
  }
  
  public EConfig(File fileName) {
    this();
    initFromFile(fileName);
  }
 
  public static EConfig getConfig() {
    if (config == null) {
      config = new EConfig(new File(ATGlobal.getCONFIGfilemane()));
    }
    return config;
  }  
}
