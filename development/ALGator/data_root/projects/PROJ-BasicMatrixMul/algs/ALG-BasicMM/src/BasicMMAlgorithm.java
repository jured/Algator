
public class BasicMMAlgorithm extends MatrixMulAbsAlgorithm {

  protected void execute(int [][] A, int [][] B, int [][] C) {
    for (int i = 0; i < A.length; i++) {
      for (int j = 0; j < A.length; j++) {
        for (int k = 0; k < A.length; k++) {
          C[i][j] += A[i][k] * B[k][j];
        }
      }
    }
  }
}