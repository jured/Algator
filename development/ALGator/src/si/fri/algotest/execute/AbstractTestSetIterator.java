package si.fri.algotest.execute;

import java.io.IOException;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.TestCase;

/**
 *
 * @author tomaz
 */
public abstract class AbstractTestSetIterator  {

  protected ETestSet  testSet;

  /** 
   * If the default constructor is used, a setTestSet method has to be called after the instantination.
   */
  public AbstractTestSetIterator() {
  }
  
  public AbstractTestSetIterator(ETestSet testSet) {
    this.testSet = testSet;   
    
    initIterator();
  }
  
  public void setTestSet(ETestSet testSet) {
    this.testSet = testSet;
    
    initIterator();
  }
  
  // Initiates the iterator (i.e. opens files, set envoronment, ...) so that
  // the first call to hasNext and next methods will iterate through the test set data
  public abstract void initIterator();
  
  /**
   * Returns true if this iterator has next test case 
   */
  public abstract boolean hasNext();
  
  /**
   * Reads the next test case (as a raw data)
   */
  public abstract void readNext();
  
  
  /**
   * Reads the i-th test case (as a raw data) and returns true if no error occures
   * or false otherwise (i.e. if i > number_of_all_tests).
   */
  public abstract boolean readTest(int testNumber);
  
  
  /**
   * Creates a new TestCase object fro a raw data read by readNext() mathod. 
   * Consecutive calls to getCurrent method must return different objects constructed
   * from the same input data.
   */
  public abstract TestCase getCurrent();
  
  /**
   * Closes the iterator sopurce.
   */
  public abstract void close() throws IOException;
  
  public int getNumberOfTestInstances() {
    return (this.testSet != null) ? (Integer) testSet.getFieldAsInt(ETestSet.ID_N) : 0;
  }

}
