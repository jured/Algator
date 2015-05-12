package si.fri.algotest.execute;

import java.io.Serializable;
import java.util.HashMap;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.timer.Timer;

/**
 *
 * @author tomaz
 */
public abstract class AbsAlgorithm implements Cloneable, Serializable {
  
  // This data is needed by ExternalExecutor to determine the type of execution.
  private MeasurementType mType;
  
  public Timer timer;                 // timer to measure the execution time of the current test
  private long[][] executoinTimes;    // the times of execution for all executions of the current test
  
  // values of counters after the execution of algorithm<
  private HashMap<String, Integer> counters = new HashMap();
  
  
  public AbsAlgorithm() {
    timer = new Timer();
    executoinTimes = new long[0][0];
    counters = new HashMap<>();
  }
  
  
  public int getTimesToExecute() {
    if (executoinTimes != null && executoinTimes.length > 0)
      return executoinTimes[0].length;
    else return 0;
  }
  
  public void setTimesToExecute(int timesToExecute) {
    executoinTimes = new long[Timer.MAX_TIMERS][timesToExecute];
  }
    
  public long [][] getExecutionTimes() {
    return executoinTimes;
  }
  
  public void setExectuionTime(int timer, int executionID, long time) {
    if (timer < executoinTimes.length && executionID < executoinTimes[timer].length)
      executoinTimes[timer][executionID] = time;
  }
 
  public HashMap<String, Integer> getCounters() {
    return counters;
  }
  
  public void setCounters(HashMap<String, Integer> counters) {
    this.counters = (HashMap<String, Integer>) counters.clone();
  }

  public MeasurementType getmType() {
    return mType;
  }

  public void setmType(MeasurementType mType) {
    this.mType = mType;
  }
  
  
  
  /**
   * Extract data from {@code test} and prepare them in the form to be simply used
   * when running execute method of [Alg][Project]AbsAlgorithm.execute().
   */
  public abstract ErrorStatus init(TestCase test);
  
  /**
   * Ececute the [Alg][Project]AbsAlgorithm.execute() method with the prepared data. 
   * The time-overhead of this method (time used before and after calling the execute()  
   * method) should be as small as possible, since the execution time of this method 
   * is measured as execution time of the algorithm.
   */
  public abstract void run();
  
  /**
   * This metod is called after the method run() to collect the result data and to 
   * verify the correctness of the solution. 
   */
  public abstract ParameterSet done();
  
  
  @Override
  public Object clone() throws CloneNotSupportedException {
   return super.clone();
  }
}
