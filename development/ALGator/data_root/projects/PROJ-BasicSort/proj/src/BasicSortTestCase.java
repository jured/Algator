
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.tools.ATTools;

/**
 * A sort-project specific TestCase
 * @author tomaz
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
