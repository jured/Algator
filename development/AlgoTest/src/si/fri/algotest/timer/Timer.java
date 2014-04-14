package si.fri.algotest.timer;

/**
 *
 * @author tomaz
 */
public class Timer {
  // maximu number of timers that the system supports
  public static final int MAX_TIMERS = 5;
  
  
  private long [] startTime, stopTime;

  public Timer() {
    startTime = new long[MAX_TIMERS];
    stopTime  = new long[MAX_TIMERS];

    for (int i = 0; i < MAX_TIMERS; i++) {
      startTime[i] = stopTime[i] = Long.MIN_VALUE;
    }
  }
   
  /**
   * Starts the i-th timer
   */
  public void start(int i) {
    startTime[i] = System.nanoTime();
  }

  /**
   * Starts the first (main) timer
   */
  public void start() {
    start(0);
  }

  /**
   * Stops the i-th timer
   */
  public void stop(int i) {
    stopTime[i] = System.nanoTime();
  }

  /**
   * Stops the first (main) timer
   */  
  public void stop() {
    stop(0);
  }
  
  public long time(int i) {
    if (startTime[i] != Long.MIN_VALUE && stopTime[i] != Long.MIN_VALUE)
      return (stopTime[i] - startTime[i]) / 1000;
    else
      return 0;
  }
  public long time() {
    return time(0);
  }
  
}
