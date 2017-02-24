
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.tools.ATTools;

/**
 * A TestCase subclass for the sorting problem
 */
public class BasicSortTestCase extends TestCase {

  /**
   * An array of data to be sorted
   */
  public int [] arrayToSort;
  
  
  @Override
  public String toString() {
    return super.toString() + ", Data: " + ATTools.intArrayToString(arrayToSort);
  }
}
