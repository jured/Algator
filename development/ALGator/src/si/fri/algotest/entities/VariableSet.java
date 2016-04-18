package si.fri.algotest.entities;

import java.io.Serializable;
import java.util.ArrayList;
import si.fri.algotest.global.ATLog;

/**
 * A set of variables.
 * @author tomaz
 */
public class VariableSet implements Serializable {
  private ArrayList<EVariable> variables;

  public VariableSet() {
    variables = new ArrayList<>();
  }
  
  
  public VariableSet(VariableSet variables) {
    this();
    
    for (int i = 0; i < variables.size(); i++) {
      try {
	addVariable((EVariable) variables.getVariable(i).clone(), true);
      } catch (CloneNotSupportedException ex) {
	ATLog.log("Can't clone (EVariable)", 2);
      }
    } 
  }
  
  
  
  public void addVariable(EVariable variable) {
    addVariable(variable,true);
  }
  
  /**
   * Če spremenljivke ni v množici, jo dodam; če pa  že obstaja, potem zamenjam 
   * njeno vrednost (replaceValue==true) oziroma ne naredim ničesar (replaceValue==false)
   */
  public void addVariable(EVariable variable, boolean replaceValue) {
    if (variables.contains(variable)) {
      if (replaceValue) {
        EVariable oldVar = null;
        for (EVariable eVariable : variables) {
          if (eVariable.equals(variable)) {
            oldVar = eVariable;
            break;
          }
        }
        if (oldVar != null) {
          oldVar.set(EVariable.ID_Value, variable.get(EVariable.ID_Value));
        }
      }
    } else {
      variables.add(variable);
    }
  }
  
  public void addVariables(VariableSet vSet) {
    addVariables(vSet, true);
  }
  
  public void addVariables(VariableSet vSet, boolean replaceExisting) {
    for(EVariable variable : vSet.variables)
      addVariable(variable, replaceExisting);
  }
  
  
  public EVariable getVariable(int i) {
    if (i<variables.size())
      return variables.get(i);
    else
      return null;
  }
  
  public int size() {
    return variables.size();
  }
  
  public EVariable getVariable(String name) {
    for (int i = 0; i < variables.size(); i++) {
      if (variables.get(i).getName().equals(name))
	return variables.get(i);
    }
    return null;
  }

  @Override
  public String toString() {
    String result="";
    for (int i = 0; i < variables.size(); i++) {
      if (!result.isEmpty()) result+="; ";
      EVariable p = variables.get(i);
      result += p.getName() + "=" + p.getField(EVariable.ID_Value);
    }
    return result;
  }
  
  public String toString(String [] order, boolean includeFieldNames, String delim) {
    String result = "";
    
    String localOrder [] = order.clone();
    
    int numVar = localOrder.length;
    if (numVar > EResult.FIXNUM && variables.contains(EResult.getErrorIndicator(""))) {
        numVar = EResult.FIXNUM;
        localOrder[numVar++] = EResult.errorParName;
    }
    
    for (int i = 0; i < numVar; i++) {
      EVariable v = null;
    
      // find a variable with name==localOrder[i]
      for (int j = 0; j < variables.size(); j++) {
        if (variables.get(j).getName().equals(localOrder[i])) {
          v = variables.get(j);
	  break;
        }
      }
      
      if (v!=null) {      
       if (includeFieldNames)
         result += v.getName() + "=";
       
       
        Object value = v.getValue();
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
