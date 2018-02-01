import si.fri.algotest.entities.TestCase;

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
    // Note that we use a method intArrayToString that was defined in the basicsort.Tools
    // class; this class was attached to the project using the "ProjectJARs" property
    // in the BasicSort.atp configuration file.
    // For the details about basicsort.Tools class see proj/lib folder.
    return super.toString() + ", Data: " + basicsort.Tools.intArrayToString(arrayToSort);
  }
}
