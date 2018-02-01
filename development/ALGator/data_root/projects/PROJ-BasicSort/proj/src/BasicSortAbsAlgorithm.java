
import si.fri.algotest.entities.EVariable;
import si.fri.algotest.entities.VariableSet;
import si.fri.algotest.entities.VariableType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.AbsAlgorithm;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public abstract class BasicSortAbsAlgorithm extends AbsAlgorithm {

  BasicSortTestCase sortTestCase;
  
  @Override
  public TestCase getTestCase() {
    return sortTestCase;
  }

  @Override
  public ErrorStatus init(TestCase test) {
    if (test instanceof BasicSortTestCase) {
      sortTestCase = (BasicSortTestCase) test;
      return ErrorStatus.STATUS_OK;
    } else {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_PERFORM_TEST, "Invalid test:" + test);
    }
  }

  @Override
  public void run() {
    execute(sortTestCase.arrayToSort);
  }

  @Override
  public VariableSet done() {
    VariableSet result = new VariableSet(sortTestCase.getParameters());
     
    // for details about the basicsort.Tools class see a comment in the method BasicSortTestCase.toString()
    boolean checkOK = basicsort.Tools.isArraySorted(sortTestCase.arrayToSort, 1);
    
    EVariable passPar = new EVariable("Check", "", VariableType.STRING, checkOK ? "OK" : "NOK");
    result.addVariable(passPar, true);

    return result;
  }  
  

  protected abstract void execute(int[] numbers);
}
