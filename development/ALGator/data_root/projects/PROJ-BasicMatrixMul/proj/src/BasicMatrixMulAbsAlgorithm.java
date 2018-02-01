
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
public abstract class BasicMatrixMulAbsAlgorithm extends AbsAlgorithm {

  BasicMatrixMulTestCase mmTestCase;
  
  int [][] resultC;

  @Override
  public TestCase getTestCase() {
    return mmTestCase;
  }

  @Override
  public ErrorStatus init(TestCase test) {
    if (test instanceof BasicMatrixMulTestCase) {
      mmTestCase = (BasicMatrixMulTestCase) test;
      
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
  public VariableSet done() {
    VariableSet result = new VariableSet(mmTestCase.getParameters());

    String ok = matrixEquals(mmTestCase.C,resultC) ? "OK" : "NOK";
    EVariable passPar = new EVariable("Check", "", VariableType.STRING, ok);
    result.addVariable(passPar, true);
    
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
