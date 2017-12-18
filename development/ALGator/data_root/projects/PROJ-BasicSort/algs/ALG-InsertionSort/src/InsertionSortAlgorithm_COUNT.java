import si.fri.algotest.execute.Counters;
/**
 * The InsertionSort algorithm; in worst it needs case n-square operations
 * while in best case it can "sort" data in linear time (if data is already sorted)
 * @author tomaz
 */
public class InsertionSortAlgorithm_COUNT extends BasicSortAbsAlgorithm {

   public void execute(int[] data) {
     int j;
     
     for (int i = 1; i < data.length; i++) {
       j=i;
       Counters.addToCounter("CMP",  1);
       while (j>0 && data[j]<data[j-1]) {
         Counters.addToCounter("CMP",  1);

         Counters.addToCounter("SWAP",  1);
         int tmp   = data[j];
         data[j]   = data[j-1];
         data[j-1] = tmp;
         j--;
       }
     }
   }

}

