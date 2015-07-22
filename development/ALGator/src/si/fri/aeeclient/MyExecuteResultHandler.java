package si.fri.aeeclient;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;

/**
 * See also http://commons.apache.org/proper/commons-exec/xref-test/org/apache/commons/exec/TutorialTest.html
 * @author tomaz
 */
public class MyExecuteResultHandler extends DefaultExecuteResultHandler {

  private ExecuteWatchdog watchdog;

  public MyExecuteResultHandler(final ExecuteWatchdog watchdog) {
    this.watchdog = watchdog;
  }

  public MyExecuteResultHandler(final int exitValue) {
    super.onProcessComplete(exitValue);
  }

  @Override
  public void onProcessComplete(final int exitValue) {
    super.onProcessComplete(exitValue);
    System.out.println("[resultHandler] The document was successfully printed ...");
  }

  @Override
  public void onProcessFailed(final ExecuteException e) {
    super.onProcessFailed(e);
    if (watchdog != null && watchdog.killedProcess()) {
      System.err.println("[resultHandler] The print process timed out");
    } else {
      System.err.println("[resultHandler] The print process failed to do : " + e.getMessage());
    }
  }
}

