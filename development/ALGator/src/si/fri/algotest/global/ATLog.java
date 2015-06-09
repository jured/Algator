package si.fri.algotest.global;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author tomaz
 */
public class ATLog {
  
  public static final String logFile = "at.log";
  
  public static final int LOG_LEVEL_OFF    = 0;
  public static final int LOG_LEVEL_STDOUT = 1;
  public static final int LOG_LEVEL_FILE   = 2;

  private static int logLevel = LOG_LEVEL_STDOUT;
  
  private static PrintWriter pw;
  private static int lastLogLevel = LOG_LEVEL_OFF;
  
  // maximal error size in bytes (to truncate very long error messages)
  private static int MAX_ERROR_SIZE = 2048;

  
  public static void disableLog() {
    lastLogLevel = logLevel;
    logLevel = LOG_LEVEL_OFF;
  }
  public static void enableLog(){
    logLevel = lastLogLevel;
  }
  
  public static void setLogLevel(int level) {
    logLevel = level;
    lastLogLevel = logLevel;
    
    if (logLevel == LOG_LEVEL_FILE) {
      try {
	pw = new PrintWriter(new File(logFile));
      } catch (Exception e) {
	pw = null;
      }
    } else {
      pw = null;
    }
  }
  
  public static void log(String msg) {
    if (logLevel == LOG_LEVEL_OFF) return;
    
    msg = msg.substring(0, Math.min(msg.length(), MAX_ERROR_SIZE));

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();
    String logMsg = String.format("%15s: %s", dateFormat.format(date), msg);
    

    switch (logLevel) {
      case LOG_LEVEL_STDOUT:
	System.out.println(logMsg);
	break;
      case LOG_LEVEL_FILE:
	pw.println(logMsg);
	break;
    }
  }
  
}
