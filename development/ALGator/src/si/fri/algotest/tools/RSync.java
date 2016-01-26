package si.fri.algotest.tools;

import java.io.IOException;

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
    
    String[] cmd = new String[]{"rsync", "-a", srcDir, destDir};
    ProcessBuilder pb = new ProcessBuilder(cmd);

    int val = -1;
    try {
      Process p = pb.start();
      val       = p.waitFor();
    } catch (Exception e) {}
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
