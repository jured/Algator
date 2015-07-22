package si.fri.aeeclient;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import si.fri.algotest.global.ATGlobal;

/**
 *
 * @author tomaz
 */
public class AEEGlobal {
    
  private static final String TASK_STATUS_FOLDER         = "client";
  private static final String TASK_CLIENT_LOG_FILENAME   = "taskclient.log";

  public static String getTaskStatusFolder() {
    String aeeFolderName = ATGlobal.getALGatorRoot() + File.separator + TASK_STATUS_FOLDER;
    File aeeFolder       = new File(aeeFolderName);
    if (!aeeFolder.exists())
      aeeFolder.mkdirs();
    
    return aeeFolderName;
  }
  
  public static String getTaskLogFilename() {
    return getTaskStatusFolder()+ File.separator + TASK_CLIENT_LOG_FILENAME;
  }
  
}
