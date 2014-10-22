
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.AbsAlgorithm;
import si.fri.algotest.global.ErrorStatus;


/**
 *
 * @author ...
 */
public abstract class MatrixMulAbsAlgorithm extends AbsAlgorithm {

  MatrixMulTestCase mmTestCase;
  
  int [][] resultC;

  @Override
  public ErrorStatus init(TestCase test) {
    if (test instanceof MatrixMulTestCase) {
      mmTestCase = (MatrixMulTestCase) test;
      
      // prepare space for the result
      int n = mmTestCase.A != null ? mmTestCase.A.length : 0;
      resultC = new int[n][n];
      
      return ErrorStatus.STATUS_OK;
    } else
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_PERFORM_TEST, "Invalid test:" + test);
  }
  
  @Override
  public void run() {
    execute(mmTestCase.A, mmTestCase.B, resultC);
  }
  
  @Override
  public ParameterSet done() {
    ParameterSet result = new ParameterSet(mmTestCase.getParameters());

    String ok = matrixEquals(mmTestCase.C,resultC) ? "OK" : "NOK";
    EParameter passPar = new EParameter("Check", "", ParameterType.STRING, ok);
    result.addParameter(passPar, true);
    
    return result;
  }   
  
  private boolean matrixEquals(int [][] A, int [][] B) {
    if (A == null || B == null) return false;
    if (A.length == 0) return false;
    
    if (A.length != B.length || A[0].length != B[0].length) return false;
    
    for (int i = 0; i < A.length; i++) {
      for (int j = 0; j < A[i].length; j++) {
        if (A[i][j] != B[i][j]) return false;
      }
    }
    return true;
  }
  
  // 
  protected abstract void execute(int [][] A, int [][] B, int [][] C);
}



/*
 * A version of this class that is given to the end-user
 
public abstract class MatrixMulAbsAlgorithm {
  // TODO: copy-paste the method signiture from above
  protected abstract void execute(int [][] A, int [][] B, int [][] C);
}

* 
*/
