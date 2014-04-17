package si.fri.algotest.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import si.fri.algotest.entities.StatFunction;

/**
 *
 * @author tomaz
 */
public class TableData {

  private static String delim = ";";

  public ArrayList<String> header;
  public ArrayList<ArrayList<Object>> data;

  public TableData() {
    header = new ArrayList<>();
    data = new ArrayList<>();
  }

  private String add(String prefix, String sufix, String delim) {
    if (prefix.isEmpty()) {
      return sufix;
    } else {
      return prefix + delim + sufix;
    }
  }

  private Comparator<ArrayList<Object>> getComparator(final int fieldNo, String type) {
    final int predznak = (type.equals("-") || type.equals("<")) ? -1 : 1;
    
    switch (type) {
      case ">": case "<":
        return new Comparator<ArrayList<Object>>() {
          @Override
          public int compare(ArrayList<Object> o1, ArrayList<Object> o2) {
            try {
              return predznak * ((String) o1.get(fieldNo)).compareTo((String) o2.get(fieldNo));
            } catch (Exception e) {
              return 0;
            }
          }
        };
        
      default: // "+", "-", or anything else
        return new Comparator<ArrayList<Object>>() {
          @Override
          public int compare(ArrayList<Object> o1, ArrayList<Object> o2) {
            try {
              return predznak * new Double(((Number) o1.get(fieldNo)).doubleValue()).compareTo(((Number) o2.get(fieldNo)).doubleValue());
            } catch (Exception e) {
              return 0;
            }
          }
        };
    }
  }

  public Object [][] getDataAsArray() {
    if (data==null) return new Object[0][0];
    Object[][] dataArray = new Object[data.size()][];
    for (int i = 0; i < data.size(); i++) {
      Object[] row = new Object[data.get(i).size()];
      for (int j = 0; j < data.get(i).size(); j++) {
        row[j] = data.get(i).get(j);
      }
      dataArray[i] = row;
    }
    return dataArray;
  }
  
  /**
   * Get the i-th column out of two-dimencional araylist
   */
  private <E> ArrayList<E> getColumn(ArrayList<ArrayList<Object>> group, int col, E type) {
    ArrayList<E> result = new ArrayList<>();
    if (!group.isEmpty() && group.get(0).size() > col) {
      for (ArrayList<Object> line : group) {
        result.add((E)line.get(col));
      }
    }
    return result;
  }
  
  /**
   * Find a statistical function for a given field in a given groupBy string. If 
   * a function for given field is not prescribed, the default function is returned. 
   * If default is not given, FIRST is returned.  
   */
  private static StatFunction getFunctionForField(String groupBy, String field) {
    StatFunction result = StatFunction.FIRST;
    
    StatFunction defaultFunc = StatFunction.UNKNOWN;
    
    String [] fields = groupBy.split(";");
    // fields[0] = groupBy field name; fields[1, 2, ...] = stat. funcs. for fields
    for (int i = 1; i < fields.length; i++) {
      if (fields[i].contains(":")){
        String [] stat = fields[i].trim().split(":");
        if (stat[0].equals(field)) {
          defaultFunc = StatFunction.getStatFunction(stat[1]);
          break;
        }
      } else {
        defaultFunc = StatFunction.getStatFunction(fields[i].trim());
      }
    }
    if (!defaultFunc.equals(StatFunction.UNKNOWN))
      result = defaultFunc;
    
    return result;
  }
   
  /**
   * Sqeezes group  - from array of lines it returns one line that contains 
   * a summary of all lines; a function that is used to summarize each column
   * can be given in groupBy parameter; default function: FIRST.
   */
  private ArrayList<Object> squeezeGroup(ArrayList<ArrayList<Object>> group, String groupBy) {
    ArrayList<Object> result = new ArrayList<>();
    for (int i = 0; i < header.size(); i++) {
      String colName = header.get(i);
      ArrayList<Integer> values = getColumn(group,i, 1);
      StatFunction function = getFunctionForField(groupBy, colName);
      Object rezultat = StatFunction.getFunctionValue(function,values);
      result.add(rezultat);
    }
    
    return result;
  }
  
  /**
   * Group data by a given field
   */
  public void groupBy(String groupby) {
    if (data.isEmpty() || groupby == null || groupby.isEmpty()) return;
    
    String [] gb = groupby.split(";");
    String field = gb[0];
    
    int fieldNo = header.indexOf(field);
    if (fieldNo == -1) {
      return;
    }
    
    // detect if the values of this filed are Srtings -> adjust sorting
    String type = "+";
    try {
      // check the first entry in this column
      if (data.get(0).get(fieldNo) instanceof String)
        type = ">";
    } catch (Exception e) {}
    sort(field+":"+type);
    
    ArrayList<ArrayList<Object>> newData = new ArrayList<>();

    ArrayList<ArrayList<Object>> group;
    
    int kjeVData=0;
    while(kjeVData < data.size()) {
      group = new ArrayList<>();
      group.add(data.get(kjeVData++));
      
      // put into a group all elements that are equal to the first group element
      while(kjeVData < data.size() && data.get(kjeVData).get(fieldNo).equals(group.get(0).get(fieldNo))) 
        group.add(data.get(kjeVData++));
      
      newData.add(squeezeGroup(group, groupby));
    }
    data = newData;
  }
  
  /**
   * Sort data array
   *
   * @param criteria sorting criteria (e.g. N:+)
   */
  public void sort(String criteria) {
    if (criteria == null || criteria.isEmpty()) {
      return;
    }

    String[] sC = criteria.split(":");
    String field = sC[0];
    String type = (sC.length > 1 ? sC[1] : "+");

    int fieldNo = header.indexOf(field);
    if (fieldNo == -1) {
      return;
    }
    data.sort(getComparator(fieldNo, type));
  }

  @Override
  public String toString() {
    String result = "";
    for (int i = 0; i < header.size(); i++) {
      result = add(result, header.get(i), delim);
    }

    for (int i = 0; i < data.size(); i++) {
      String vrstica = "";
      for (int j = 0; j < data.get(i).size(); j++) {
        vrstica = add(vrstica, data.get(i).get(j).toString(), delim);
      }
      result = add(result, vrstica, "\n");
    }
    return result;
  }
  
}
