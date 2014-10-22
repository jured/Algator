import si.fri.algotest.execute.Counters;

import java.util.Arrays;

/**
 *
 * @author tomaz
 */
public class QuicksortSortAlgorithm_COUNT extends SortAbsAlgorithm {

  void quickSort(int[] arr, int left, int right) {

    int i = left, j = right, tmp;

    int pivot = arr[(left + right) / 2];

    /* partition */
    while (i <= j) {
      
      Counters.addToCounter("CMP",  1);
      while (arr[i] < pivot) {
        Counters.addToCounter("CMP",  1);
        i++;
      }

      Counters.addToCounter("CMP",  1);
      while (arr[j] > pivot) {
        Counters.addToCounter("CMP",  1);
        j--;
      }

      if (i <= j) {
        Counters.addToCounter("SWAP",  1);
        tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
        i++;j--;
      }

    };

    /* recursion */
    if (left < j) {
      Counters.addToCounter("CALL",  1);
      quickSort(arr, left, j);
    }

    if (i < right) {
      Counters.addToCounter("CALL",  1);
      quickSort(arr, i, right);
    }

  }

  public void execute(int[] data) {
    Counters.addToCounter("CALL",  1);
    quickSort(data, 0, data.length - 1);
  }
}

