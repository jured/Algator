import java.io.File;
import java.util.Scanner;
import si.fri.algotest.entities.EVariable;
import si.fri.algotest.entities.EResult;
import si.fri.algotest.entities.VariableType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.DefaultTestSetIterator;
import si.fri.algotest.global.ErrorStatus;


public class BasicMatrixMulTestSetIterator extends DefaultTestSetIterator {
   
  
  @Override
  public TestCase getCurrent() {
    if (currentInputLine == null) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "No valid input!");
      return null;
    }
  
    String[] params = currentInputLine.split(":");
    if (params.length != 6) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "Invalid testset file - line " + lineNumber);
      return null;
    }

    String filePath = testSet.entity_rootdir;

    BasicMatrixMulTestCase tCase = new BasicMatrixMulTestCase();
    EVariable testIDPar = EResult.getTestIDParameter("Test-" + Integer.toString(lineNumber));
    tCase.addParameter(testIDPar);

    EVariable parameter1 = new EVariable("Name", "", VariableType.STRING, params[0]);
    EVariable parameter2 = new EVariable("Group", "", VariableType.STRING, params[1]);
    EVariable parameter3 = new EVariable("N", "", VariableType.INT, params[2]);

    tCase.addParameter(parameter1);
    tCase.addParameter(parameter2);
    tCase.addParameter(parameter3);

    tCase.A = readMatrix(filePath, params[3]);
    tCase.B = readMatrix(filePath, params[4]);
    tCase.C = readMatrix(filePath, params[5]);

    if (tCase.A == null || tCase.B == null || tCase.C == null) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "Invalid testset file - line " + lineNumber);
      return null;
    }

    tCase.n = tCase.A.length;

    return tCase;
  }

  private int[][] readMatrix(String path, String fileName) {
    try {
      Scanner sc = new Scanner(new File(path + File.separator + fileName));
      int nSq = sc.nextInt();
      int n = (int) Math.round(Math.sqrt(nSq));
      int[][] result = new int[n][n];
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          result[i][j] = sc.nextInt();
        }
      }
      return result;
    } catch (Exception e) {
      return null;
    }
  }
}
 