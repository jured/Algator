
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.AbsAlgorithm;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.tools.ATTools;


/**
 *
 * @author tomaz
 */
public abstract class SortAbsAlgorithm extends AbsAlgorithm {

  SortTestCase sortTestCase;

  @Override
  public ErrorStatus init(TestCase test) {
    if (test instanceof SortTestCase) {
      sortTestCase = (SortTestCase) test;
      return ErrorStatus.STATUS_OK;
    } else
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_PERFORM_TEST, "Invalid test:" + test);
  }
  
  @Override
  public void run() {
    execute(sortTestCase.arrayToSort);
  }

  
  @Override
  public ParameterSet done() {
    ParameterSet result = new ParameterSet(sortTestCase.getParameters());
    
    EParameter passPar = new EParameter("Check", "", ParameterType.STRING, 
	 ATTools.isArraySorted(sortTestCase.arrayToSort, 1) ? "OK" : "NOK");
    result.addParameter(passPar, true);
          
    return result;
  }   

  protected abstract void execute(int [] tabela);
}



/*
 * A version of this class that is given to the end-user
 
public abstract class SortAbsAlgorithm {
  protected abstract void execute(int [] tabela);
}

* 
*/
