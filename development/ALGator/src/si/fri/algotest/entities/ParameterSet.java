package si.fri.algotest.entities;

import java.io.Serializable;
import java.util.ArrayList;
import si.fri.algotest.global.ATLog;

/**
 * A set of parameters.
 * @author tomaz
 */
public class ParameterSet implements Serializable {
  private ArrayList<EParameter> parameters;

  public ParameterSet() {
    parameters = new ArrayList<>();
  }
  
  
  public ParameterSet(ParameterSet parameters) {
    this();
    
    for (int i = 0; i < parameters.size(); i++) {
      try {
	addParameter((EParameter) parameters.getParameter(i).clone(), true);
      } catch (CloneNotSupportedException ex) {
	ATLog.log("Can't clone (EParameter)", 2);
      }
    } 
  }
  
  
  /**
   * A parameter is added to the set if it does not exist or if replaceExisting==true.  
   * @param parameter 
   */
  public void addParameter(EParameter parameter, boolean replaceExisting) {
    if (parameters.contains(parameter)) {
      if (replaceExisting) {
        parameters.remove(parameter);
	parameters.add(parameter);
      }
    } else {
      parameters.add(parameter);
    }
  }
  
  public void addParameters(ParameterSet pSet, boolean replaceExisting) {
    for(EParameter parameter : pSet.parameters)
      addParameter(parameter, replaceExisting);
  }
  
  
  public EParameter getParameter(int i) {
    if (i<parameters.size())
      return parameters.get(i);
    else
      return null;
  }
  
  public int size() {
    return parameters.size();
  }
  
  public EParameter getParamater(String name) {
    for (int i = 0; i < parameters.size(); i++) {
      if (parameters.get(i).getField(Entity.ID_NAME).equals(name))
	return parameters.get(i);
    }
    return null;
  }

  @Override
  public String toString() {
    String result="";
    for (int i = 0; i < parameters.size(); i++) {
      if (!result.isEmpty()) result+="; ";
      EParameter p = parameters.get(i);
      result += p.getField(Entity.ID_NAME) + "=" + p.getField(EParameter.ID_Value);
    }
    return result;
  }
  
  public String toString(String [] order, boolean includeFieldNames, String delim) {
    String result = "";
    
    String localOrder [] = order.clone();
    
    int numPar = localOrder.length;
    if (numPar > EResultDescription.FIXNUM && parameters.contains(EResultDescription.getErrorParameter(""))) {
        numPar = EResultDescription.FIXNUM;
        localOrder[numPar++] = EResultDescription.errorParName;
    }
    
    for (int i = 0; i < numPar; i++) {
      EParameter p = null;
    
      // find a parameter with name==localOrder[i]
      for (int j = 0; j < parameters.size(); j++) {
        if (parameters.get(j).getField(Entity.ID_NAME).equals(localOrder[i])) {
          p = parameters.get(j);
	  break;
        }
      }
      
      if (p!=null) {      
       if (includeFieldNames)
         result += p.getField(EParameter.ID_NAME) + "=";
       
       Object value = p.getField(EParameter.ID_Value);
       if (value instanceof String) {
         value = ((String)value).replaceAll(delim, " ").replaceAll("\n", " ");
       }
       
       result += value + delim;
      } else
        result += delim;
    }
     
    // strip <code>delim</code> at the end of line
    return result.substring(0,result.length()-delim.length());
  }
  
}
