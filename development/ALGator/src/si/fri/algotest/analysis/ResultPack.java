package si.fri.algotest.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ParameterSet;

/**
 * Objects of this class contain a map of results (key=something describing test 
 * (i.e. alg-testset-test or testset-test or just test)  value=ParameterSet for 
 * this test) and a resultDescrition to describe the parameters in ResultsSets)
 * @author tomaz
 */
public class ResultPack {
  private HashMap<String, ParameterSet> results;
  EResultDescription resultDescription;
  
  ArrayList<String> keyOrder;

  public ResultPack() {
    results = new HashMap<>();
    resultDescription = new EResultDescription();
    keyOrder = new ArrayList<>();
  }
  
  public void putResult(String key, ParameterSet value) {
    results.put(key, value);
    keyOrder.add(key);
  }

  public ParameterSet getResult(String key) {
    return results.get(key);
  }
  
  @Override
  public String toString() {
    String res = "";
    for (String key : results.keySet()) {
      res += key+" : "+results.get(key) + "\n";
    }
    return res;
  }
  
  
}
