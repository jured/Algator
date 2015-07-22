package si.fri.adeserver;

import java.util.Date;
import si.fri.algotest.entities.Entity;

/**
 * ADE task. ADEServer holds an array of ADE tasks to be executed by AEE.
 * @author tomaz
 */
public class ADETask extends Entity {
  
  public static final String ID_ADETask    = "ADETask";
  
  // Fields
  public static final String ID_TaskID     = "TaskID";     // int
  public static final String ID_Project    = "Project";    // String
  public static final String ID_Algorithm  = "Algorithm";  // String
  public static final String ID_Testset    = "Testset";    // String
  public static final String ID_MType      = "MType";      // String
  
  public static final String ID_Status           = "Status";           // String
  public static final String ID_StatusDate       = "StatusDate";       // String
  public static final String ID_AssignedComputer = "AssignedComputer"; // String
  
  
  public ADETask() {
    super(ID_ADETask, 
	 new String [] {ID_TaskID, ID_Project, ID_Algorithm, ID_Testset, ID_MType, ID_Status, ID_StatusDate, ID_AssignedComputer});
   
    setRepresentatives(ID_Project, ID_Algorithm, ID_Testset, ID_MType);
  }
  
  public ADETask(String project, String algorithm, String testset, String mType) {
    this();
    int taskID = ADEGlobal.getNextTaskID();
    set(ID_TaskID,     taskID);
    set(ID_Project,    project);
    set(ID_Algorithm,  algorithm);
    set(ID_Testset,    testset);
    set(ID_MType,      mType);
    
    setTaskStatus(TaskStatus.QUEUED, "none");
  }
 
    
  ADETask(String json) {
    this();
    initFromJSON(json);
  }

  public void setTaskStatus(TaskStatus status, String computer) {
    set(ID_Status,     status.toString());
    set(ID_StatusDate, Long.toString(new Date().getTime())); 
    
    if (computer != null)
      set(ID_AssignedComputer, computer);
  }
  
  public TaskStatus getTaskStatus() {
    String status = getField(ID_Status);
    if (status != null)
      return TaskStatus.getTaskStatus(status);
    else
      return TaskStatus.UNKNOWN;
  }
  
  @Override
  public String toString() {
    return getField(ID_TaskID)    + ADEGlobal.STRING_DELIMITER +
           getField(ID_Project)   + ADEGlobal.STRING_DELIMITER +
           getField(ID_Algorithm) + ADEGlobal.STRING_DELIMITER +
           getField(ID_Testset)   + ADEGlobal.STRING_DELIMITER +
           getField(ID_MType);
  }
  
  public String toStringPlus() {
    return toString() + "  [Status: "+ get(ID_Status) + "  Assigned computer: " + get(ID_AssignedComputer) + "]";
  }
  
}
