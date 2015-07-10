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
  
  public static final int ADEPort = 21221;
  
  private static final String TASK_SERVER_FOLDER  = "tasks";
  private static final String TASK_STATUS_FOLDER  = "status";
  private static final String TASK_ID_FILENAME    = "task.number";
  private static final String TASK_LIST_FILENAME  = "task.list";

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
  
}
