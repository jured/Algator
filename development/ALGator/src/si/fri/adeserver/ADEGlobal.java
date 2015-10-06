package si.fri.adeserver;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import si.fri.algotest.global.ATGlobal;

/**
 *
 * @author tomaz
 */
public class ADEGlobal {
  
  public static final int ADEPort = 12321;
  
  /*
   * REQUESTS
   */
  public static final String REQ_WHO            = "WHO";            // no parameters
  public static final String REQ_LIST           = "LIST";           // no parameters
  public static final String REQ_ADD_TASK       = "ADDTASK";        // parameters: project_name algorithm_name testset_name measurement_type
  public static final String REQ_GET_NEXT_TASK  = "GETNEXTTASK";    // parameters: computerID
  public static final String REQ_COMPLETE_TASK  = "COMPLETETASK";   // parameters: taskID
  public static final String REQ_STATUS         = "STATUS";         // no parameters
  public static final String REQ_TASK_STATUS    = "TASKSTATUS";     // parameters: taskID
  public static final String REQ_PROJ_STATUS    = "PROJECTSTATUS";  // parameters: projectName
  public static final String REQ_QUERY_RES      = "GETQUERYRESULT"; // parameters: projectName
  
  
  

  /**
   * TaskServer healthy checking question and answer
   */
  public static final String REQ_CHECK_Q        = "hello?";
  public static final String REQ_CHECK_A        = "TaskServer status: OK";
  
  /*
   * ERROR string  (the error message between client/server always starts with this string
   */
  public static final String ERROR_PREFIX  = "ERROR:: ";
  
  // the answer string, if on tasks is available for a given computer
  public static final String NO_TASKS = "NONE AVAILABLE";
  
  
  public static final String ERROR_INVALID_NPARS     = "Invalid number of parameters";
  public static final String ERROR_ERROR_CREATE_TASK = "Error occured when creating a task";
  
  // if a string holds more than one information, data if separated by STRING_DELIMITER
  public static final String STRING_DELIMITER  = " ";
  
  
  private static final String TASKSERVER_LOG_FOLDER  = "taskserver";
  private static final String TASK_ID_FILENAME       = "task.number";
  private static final String TASK_LIST_FILENAME     = "task.list";
  private static final String TASKSERVER_LOG_FILENAME           = "taskserver.log";
  

  public static String getTaskServerLogFolder() {
    String adeFolderName = ATGlobal.getLogFolder() + File.separator + TASKSERVER_LOG_FOLDER;
    File adeFolder       = new File(adeFolderName);
    if (!adeFolder.exists())
      adeFolder.mkdirs();
    
    return adeFolderName;
  }
    
  public static String getADETasklistFilename() {
    return  getTaskServerLogFolder() + File.separator + TASK_LIST_FILENAME;
  }

  public static String getTaskserverLogFilename() {
    return getTaskServerLogFolder() + File.separator + TASKSERVER_LOG_FILENAME;
  }
 
  public static int getNextTaskID() {
    try {
     File folder = new File(getTaskServerLogFolder());
     File file   = new File(folder, TASK_ID_FILENAME);
     
     int taskID = 0;
     if (file.exists()) {
       try (Scanner sc = new Scanner(file)) {
         taskID = sc.nextInt();
       }
     }
     try (PrintWriter pw = new PrintWriter(file)) {
       pw.println(++taskID);   
     }
     return taskID;
    } catch (Exception e) {
      return 0;
    }
  } 
  
  public static boolean isError(String msg) {
    return msg.startsWith(ERROR_PREFIX);
  }
  public static String getErrorString(String errorMsg) {
    return ERROR_PREFIX + errorMsg;
  }
  public static String getErrorMsg(String errorMsg) {
    if (isError(errorMsg)) 
      return errorMsg.substring(ERROR_PREFIX.length());
    else
      return "";
  }
  
}
