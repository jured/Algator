package si.fri.algotest.entities;

import java.io.File;
import si.fri.algotest.global.ATGlobal;

/**
 *
 * @author tomaz
 */
public class ELocalConfig extends Entity {
  public static final String    DEFAULT_TASK_SERVER_NAME = "localhost";
  
  // a single instance of a local configuration file
  private static ELocalConfig config;
  
  // Entity identifier
  public static final String ID_Query             = "Config";  
  
  //Fields
  private static final String ID_FAMILYID          = "FamilyID";	        // String
  private static final String ID_COMPID            = "ComputerID";	// String
  public  static final String ID_VMEP              = "VMEP";	        // String
  public  static final String ID_VMEPClasspath     = "VMEPClasspath";    // String
  public  static final String ID_TaskServerName    = "TaskServerName";   // String
  
  
  public ELocalConfig() {
   super(ID_Query, 
	 new String [] {ID_FAMILYID, ID_COMPID, ID_VMEP, ID_VMEPClasspath, ID_TaskServerName});
   setRepresentatives(ID_FAMILYID, ID_COMPID);
  }
  
  public ELocalConfig(File fileName) {
    this();
    initFromFile(fileName);
  }
 
  public static ELocalConfig getConfig() {
    if (config == null) {
      config = new ELocalConfig(new File(ATGlobal.getLocalConfigFilename()));
    }
    return config;
  }  
  
  public String getComputerID() {
    String family   = getField(ID_FAMILYID);
    String computer = getField(ID_COMPID);
    if (family != null && !family.isEmpty() && computer != null && !computer.isEmpty())
      return  family+ "." + computer;
    else return "";
  }
  
  public String getTaskServerName() {
    String taskServerName = getField(ID_TaskServerName);
    if (taskServerName == null || taskServerName.isEmpty())
      taskServerName = DEFAULT_TASK_SERVER_NAME;
    return taskServerName;
  }
}
