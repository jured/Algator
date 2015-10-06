package si.fri.algotest.global;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logiram vse dogajanje. Ob zagonu programa vse morebitne napake pri preverjanju parametrov zapisujem v 
 * algator.log. Sporočila povezana z izvajanjem algoritma pa zapisujem v datoteko P-A-T-em.history. 
 * 
 * @author tomaz
 */
public class ATLog {
  
  public static final String logFile = "algator.log";
  
  public static final int LOG_TARGET_OFF    = 0;
  public static final int LOG_TARGET_STDOUT = 1;
  public static final int LOG_TARGET_FILE   = 2;

  private static int logTarget     = LOG_TARGET_STDOUT; // default
  private static int lastLogTarget = LOG_TARGET_STDOUT;
    
  // maximal error size in bytes (to truncate very long error messages)
  private static int MAX_ERROR_SIZE = 2048;

  // filename for algator specific logging
  private static String algatorLogFileName = "";
  
  // filename for Project-Algorithm-Testset-EM specific logging
  private static String pateFilename = "";
  public static void setPateFilename(String pateFN) {
    pateFilename = pateFN;
    
    // ob priklopu na datoteko izpišem ločilo, da so logi jasno ločeni eden od drugega
    try (PrintWriter pw = new PrintWriter(new FileWriter(new File(pateFN), true))) {
      pw.println("-------------------------------------------------------------");
    } catch (Exception e) {}
  }
  
  
  public static void disableLog() {
    lastLogTarget = logTarget;
    logTarget = LOG_TARGET_OFF;
  }
  public static void enableLog(){
    logTarget = lastLogTarget;
  }
  
  public static void setLogTarget(int target) {
    logTarget = target;
    lastLogTarget = logTarget;    
  }
  
  /**
   * Parameter kam ima pomen le v primeru, da je logLevel & LOG_LEVEL_FILE != 0 (torej zapisujem v file).
   * parameter določa, v kateri file zapisujem - v algator.log (kam=1) ali v P-A-T-em.history (kam=2) 
   * ali oboje (kam=3).
   */
  public static void log(String msg, int kam) {
    if (logTarget == LOG_TARGET_OFF) return;
    
    msg = msg.substring(0, Math.min(msg.length(), MAX_ERROR_SIZE));

    String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    String logMsg = String.format("%15s: %s", date, msg);
    
    // loggint to stdout
    if ((logTarget & LOG_TARGET_STDOUT) != 0)
      System.out.println(logMsg);

    // loging to file
    if ((logTarget & LOG_TARGET_FILE) != 0) {
      if (algatorLogFileName.isEmpty())
        algatorLogFileName = ATGlobal.getAlgatorLogFilename();
      
      if ((kam & 1) != 0) 
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(algatorLogFileName), true))) {
          pw.println(logMsg);
        } catch (Exception e) {} // nothing can be done
          
      if ((kam & 2) != 0) 
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(pateFilename), true))) {
          pw.println(logMsg);
        } catch (Exception e) {} // nothing can be done          
    }      
  }
  
}
