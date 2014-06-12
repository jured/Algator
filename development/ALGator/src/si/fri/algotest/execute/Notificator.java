package si.fri.algotest.execute;

/**
 * A notificator is a class used to comunicate between ATSystem and ATExecutor. 
 * Each time the executor executes a test, it notifies the Notificator by calling
 * the notify method. 
 * @author tomaz
 */
public abstract class Notificator {
  // number of instances
  private int n = 0;

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
  
  /**
   * Called when i-th test (out of n) is finished
   * @param i 
   */
  public abstract void notify(int i);
}
