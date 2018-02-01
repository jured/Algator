import si.fri.algotest.execute.Counters;
/**
 *
 * @author tomaz
 */
public class InsertionSortAlgorithm_COUNT extends BasicSortAbsAlgorithm {
  @Override
  public void execute(int[] data) {
    for(int i=1; i<data.length; i++) {
      int j=i-1;

      if (j>=0) 
        Counters.addToCounter("CMP",  1);
      while(j>=0 && data[j]>data[j+1]) {
      	Counters.addToCounter("CMP",  1);

        Counters.addToCounter("SWAP",  1);
        int tmp = data[j];
        data[j]=data[j+1];
        data[j+1]=tmp;
        j--;
      }
    }
  } 
}

