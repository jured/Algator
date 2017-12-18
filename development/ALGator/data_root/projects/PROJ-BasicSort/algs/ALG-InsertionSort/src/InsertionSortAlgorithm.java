/**
 * The InsertionSort algorithm; in worst it needs case n-square operations
 * while in best case it can "sort" data in linear time (if data is already sorted)
 * @author tomaz
 */
public class InsertionSortAlgorithm extends BasicSortAbsAlgorithm {

   public void execute(int[] data) {
     int j;
     
     for (int i = 1; i < data.length; i++) {
       
       j=i;
       //@COUNT{CMP, 1}
       while (j>0 && data[j]<data[j-1]) {
         // note: if k is the number of times the while loop will iterate, the
         // comparison will be performed k+1 times; therefore the counter has 
         // to be increased before and after the "while loop" statement
         //@COUNT{CMP, 1}

         //@COUNT{SWAP, 1}
         int tmp   = data[j];
         data[j]   = data[j-1];
         data[j-1] = tmp;
         j--;
       }
     }
   }
}