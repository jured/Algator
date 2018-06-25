package si.fri.algotest.execute;

import si.fri.algotest.global.ExecutionStatus;

/**
 * A notificator is a class used to comunicate between ATSystem and ATExecutor. 
 * Each time the executor executes a test, it notifies the Notificator by calling
 * the notify method. 
 * @author tomaz
 */
public abstract class Notificator {
  // number of instances
  private int n = 0;
  
  // the number of task (used to write status messages)
  protected int taskID = 0;

  public Notificator() {
  }
  
  public Notificator(int n) {
    this.n=n;
  }
  
  public void setNumberOfInstances(int n) {
    this.n = n;
  }
  
  public int getN() {
    return this.n;
  }
  
  public void setTaskID(int taskID) {
    this.taskID = taskID;
  }
  
  /**
   * Called when i-th test (out of n) is finished
   * @param i 
   */
  public abstract void notify(int i, ExecutionStatus status);
}
