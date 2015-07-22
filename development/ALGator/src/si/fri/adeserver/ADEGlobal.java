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
  public static final String REQ_ADD_TASK       = "addTask";
  public static final String REQ_GET_NEXT_TASK  = "getNextTask";
  public static final String REQ_COMPLETE_TASK  = "completeTask";
  public static final String REQ_STATUS         = "status";

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
  
  
  private static final String TASK_SERVER_FOLDER  = "server";
  private static final String TASK_STATUS_FOLDER  = "tasks";
  private static final String TASK_ID_FILENAME    = "task.number";
  private static final String TASK_LIST_FILENAME  = "task.list";
  private static final String LOG_FILENAME        = "taskserver.log";
  

  public static String getTaskServerFolder() {
    String adeFolderName = ATGlobal.getALGatorDataRoot() + File.separator + TASK_SERVER_FOLDER;
    File adeFolder       = new File(adeFolderName);
    if (!adeFolder.exists())
      adeFolder.mkdirs();
    
    return adeFolderName;
  }
  
  public static String getTaskStatusFolder() {
    String folderName = getTaskServerFolder() + File.separator + TASK_STATUS_FOLDER;
    File folder = new File(folderName);
    if (!folder.exists())
      folder.mkdir();
    return folderName;
  }
  
  public static String getTaskStatusFilename(int idt) {
    return getTaskStatusFolder() + File.separator + idt;
  }
  
  public static String getADETasklistFilename() {
    return  getTaskServerFolder() + File.separator + TASK_LIST_FILENAME;
  }

  public static String getLogFilename() {
    return getTaskServerFolder() + File.separator + LOG_FILENAME;
  }
 
  public static int getNextTaskID() {
    try {
     File folder = new File(getTaskServerFolder());
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
