import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.DefaultTestSetIterator;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.tools.ATTools;
import si.fri.algotest.global.ErrorStatus;


/**
 *
 * @author ...
 */
public class MatrixMulTestSetIterator extends DefaultTestSetIterator {
   
  
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

    MatrixMulTestCase tCase = new MatrixMulTestCase();
    EParameter testIDPar = EResultDescription.getTestIDParameter("Test-" + Integer.toString(lineNumber));
    tCase.addParameter(testIDPar);

    EParameter parameter1 = new EParameter("Name", "", ParameterType.STRING, params[0]);
    EParameter parameter2 = new EParameter("Group", "", ParameterType.STRING, params[1]);
    EParameter parameter3 = new EParameter("N", "", ParameterType.INT, params[2]);

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
  
  // TEST
    
    public static void main(String args[]) {
    String dataroot     = "/Users/Tomaz/Dropbox/FRI/ALGator/data_root"; // a folder with the "projects" folder
    String projName     = "MatrixMul";
    
    ETestSet testSet = ATTools.getFirstTestSetFromProject(dataroot, projName);
    MatrixMulTestSetIterator stsi = new MatrixMulTestSetIterator();
    stsi.setTestSet(testSet);
    
    ATTools.iterateAndPrintTests(stsi);
  }
  
}
 