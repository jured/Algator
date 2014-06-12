import si.fri.algotest.entities.TestCase;

/**
 * 
 * @author ...
 */
public class MatrixMulTestCase extends TestCase {

  int n;
  
  // three matrices (C = A * B)
  int [][] A;
  int [][] B;
  int [][] C;
  
  
   @Override
  public String toString() {
    String dataA, dataB, dataC; dataA = dataB = dataC = "";
    for (int i = 0; i < 5; i++) {
      if (i < n) {
        dataA += (!dataA.isEmpty() ? ", ":"") + A[0][i];
	dataB += (!dataB.isEmpty() ? ", ":"") + B[0][i];
        dataC += (!dataC.isEmpty() ? ", ":"") + C[0][i];
      }
    }
    String matrika = " %c = [%s ...] ";
    return "N = " + A.length + String.format(matrika, 'A', dataA)
	    + String.format(matrika, 'B', dataB) + String.format(matrika, 'C', dataC);
  }
}
