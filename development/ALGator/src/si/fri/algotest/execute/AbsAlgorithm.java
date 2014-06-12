package si.fri.algotest.execute;

import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public abstract class AbsAlgorithm implements Cloneable {
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
