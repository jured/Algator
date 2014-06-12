import si.fri.algotest.execute.Counters;
/**
 *
 * @author tomaz
 */
public class BubblesortSortAlgorithm_COUNT extends SortAbsAlgorithm {

  public void execute(int[] data) {
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data.length-1; j++) {
        Counters.addToCounter("CMP",  1);
        if (data[j] > data[j+1]) {
          Counters.addToCounter("SWAP",  1);
          int tmp = data[j];
          data[j] = data[j+1];
          data[j+1] = tmp;
        }
      }
    } 
  }
}
