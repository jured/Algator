package si.fri.adeserver;

import si.fri.algotest.entities.Entity;

/**
 * ADE task. ADEServer holds an array of ADE tasks to be executed by AEE.
 * @author tomaz
 */
public class ADETask extends Entity {
  
  public static final String ID_ADETask    = "ADETask";
  
  // Fields
  public static final String ID_TaskID     = "TaskID";    // int
  public static final String ID_Project    = "Project";   // String
  public static final String ID_Algorithm  = "Algorithm"; // String
  public static final String ID_Testset    = "Testset";   // String
  public static final String ID_MType      = "MType";     // String
  
  
  // unique id of this task
  private int tID;
  
  public ADETask() {
    super(ID_ADETask, 
	 new String [] {ID_Project, ID_Algorithm, ID_Testset, ID_MType});
   
    setRepresentatives(ID_Project, ID_Algorithm, ID_Testset, ID_MType);
  }
  
  public ADETask(String project, String algorithm, String testset, String mType) {
    this();
    
    set(ID_TaskID,    ADEGlobal.getNextTaskID());
    set(ID_Project,   project);
    set(ID_Algorithm, algorithm);
    set(ID_Testset,   testset);
    set(ID_MType,     mType);
  }
 
    
  ADETask(String json) {
    this();
    initFromJSON(json);
  }
}
