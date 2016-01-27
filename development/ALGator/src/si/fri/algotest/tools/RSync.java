package si.fri.algotest.tools;

import java.io.File;
import java.io.IOException;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public class RSync {
  
  /**
   * Makes the destDir to be a mirror of srcDir.
   */
  public static int mirror(String srcDir, String destDir) {
    // to ensure that the files of srcDir will be copied into destDir 
    // (instead of copying a folder srcDir as a subfolder in destDir)
    if (!srcDir.endsWith("/")) srcDir += "/";        
    
    // if destination folder does not exist -> algator creates an empty folder
    File ddir = new File(destDir);
    if (!ddir.exists()) ddir.mkdirs();
    
    String[] cmd = new String[]{"rsync", "-a", srcDir, destDir};
    ProcessBuilder pb = new ProcessBuilder(cmd);

    int val = -1;
    try {
      Process p = pb.start();
      val       = p.waitFor();
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, e.toString());
    }
    return val;
  }
  
  public static boolean rsyncExists() {
    String[] cmd = new String[]{"rsync", "-h"};
    ProcessBuilder pb = new ProcessBuilder(cmd);
    int val = -1;
    try {
      Process p = pb.start();
      val = p.waitFor();
    } catch (IOException | InterruptedException e) {}
    
    return val == 0;    
  }  
}
