
import java.util.Arrays;

/**
 *
 * @author tomaz
 */
public class QuicksortSortAlgorithm extends SortAbsAlgorithm {

  void quickSort(int[] arr, int left, int right) {

    int i = left, j = right, tmp;

    int pivot = arr[(left + right) / 2];

    /* partition */
    while (i <= j) {
      
      //@COUNT{CMP, 1}
      while (arr[i] < pivot) {
        //@COUNT{CMP, 1}
        i++;
      }

      //@COUNT{CMP, 1}
      while (arr[j] > pivot) {
        //@COUNT{CMP, 1}
        j--;
      }

      if (i <= j) {
        //@COUNT{SWAP, 1}
        tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
        i++;j--;
      }

    };

    /* recursion */
    if (left < j) {
      //@COUNT{CALL, 1}
      quickSort(arr, left, j);
    }

    if (i < right) {
      //@COUNT{CALL, 1}
      quickSort(arr, i, right);
    }

  }

  public void execute(int[] data) {
    //@COUNT{CALL, 1}
    quickSort(data, 0, data.length - 1);
  }
}
