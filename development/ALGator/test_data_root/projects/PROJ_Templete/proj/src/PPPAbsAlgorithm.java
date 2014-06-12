
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.AbsAlgorithm;
import si.fri.algotest.global.ErrorStatus;


/**
 *
 * @author ...
 */
public abstract class PPPAbsAlgorithm extends AbsAlgorithm {

  PPPTestCase pppTestCase;

  @Override
  public ErrorStatus init(TestCase test) {
    if (test instanceof PPPTestCase) {
      pppTestCase = (PPPTestCase) test;
      return ErrorStatus.STATUS_OK;
    } else
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_PERFORM_TEST, "Invalid test:" + test);
  }
  
  @Override
  public void run() {
    // execute(pppTestCase.arrayToSort);
  }

  
  @Override
  public ParameterSet done() {
    ParameterSet result = new ParameterSet(pppTestCase.getParameters());

    // TODO: set the output parameters
    // EParameter passPar = new EParameter("...", "", ..., ...);
    // result.addParameter(passPar, true);
          
    return result;
  }   

  // TODO: define parameters for the execute method
  //protected abstract void execute(int [] tabela);
  
}



/*
 * A version of this class that is given to the end-user
 
public abstract class PPPAbsAlgorithm {
  // TODO: copy-paste the method signiture from above
  protected abstract void execute(int [] tabela);
}

* 
*/
