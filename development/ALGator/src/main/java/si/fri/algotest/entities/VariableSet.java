package si.fri.algotest.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import si.fri.algotest.global.ATLog;

/**
 * A set of variables.
 * @author tomaz
 */
public class VariableSet implements Serializable, Iterable<EVariable> {

  private HashMap<String, EVariable> variables;

  public VariableSet() {
        variables = new HashMap<>();
  }
  
  public VariableSet(VariableSet variables) {
    this();
    for (EVariable var : variables.variables.values()) {
      try {
                addVariable((EVariable) var.clone(), true);
      } catch (CloneNotSupportedException ex) {
	ATLog.log("Can't clone (EVariable)", 2);
      }
    } 
  }
  
  public void addVariable(EVariable variable) {
    addVariable(variable, true);
  }
  
  /**
   * Če spremenljivke ni v množici, jo dodam; če pa  že obstaja, potem zamenjam 
   * njeno vrednost (replaceValue==true) oziroma ne naredim ničesar (replaceValue==false)
   */
  public void addVariable(EVariable variable, boolean replaceValue) {
    EVariable oldVar = variables.get(variable.getName());
    if (oldVar == null) {
      variables.put(variable.getName(), variable);
    } else if (replaceValue) {
      oldVar.set(EVariable.ID_Value, variable.get(EVariable.ID_Value));
    }
  }
  
  public void addVariables(VariableSet vSet) {
    addVariables(vSet, true);
  }
  
  public void addVariables(VariableSet vSet, boolean replaceExisting) {
    for (EVariable variable : vSet) {
      addVariable(variable, replaceExisting);
    }
  }
  
  public EVariable getVariable(int i) {
    if (i < variables.size()) {
      return variables.values().toArray(new EVariable[0])[i];
    } else {
      return null;
    }
  }
  
  public int size() {
    return variables.size();
  }
  
  public VariableSet copy() {
    VariableSet copy = new VariableSet();
    for (Map.Entry<String, EVariable> entry : variables.entrySet()) {
      try {
        copy.variables.put(entry.getKey(), (EVariable) entry.getValue().clone());
      } catch (CloneNotSupportedException ex) {
        ATLog.log("Can't clone (EVariable)", 3);
      }
    }
    return copy;
  }

  public EVariable getVariable(String name) {
    return variables.get(name);
  }

  @Override
  public String toString() {
    String result="";
    for (int i = 0; i < variables.size(); i++) {
      if (!result.isEmpty()) result+="; ";
      EVariable p = variables.get(i);
      if (p!=null)
        result += p.getName() + "=" + p.getField(EVariable.ID_Value);
    }
    return result;
  }
  
  public String toString(String [] order, boolean includeFieldNames, String delim) {
    String result = "";
    
    String localOrder [] = order.clone();

//    prej:   če je prišlo do napake, se je kot 5. rezultat (za KILLED ali FAILED) izpisalo
//            sporočilo o napaki; Sedaj 
//    sedaj:  sporočilo o napaki izpišem povsem na koncu, prej pa izpišem vrednosi vseh parametrov
//            (indikatorji bodo napačni, parametri pa bodo pravilni)
//    int numVar = localOrder.length;
//    if (numVar > EResult.FIXNUM && variables.values().contains(EResult.getErrorIndicator(""))) {
//      numVar = EResult.FIXNUM;
//      localOrder[numVar++] = EResult.errorParName;
//    }
    int numVar = localOrder.length;
    for (int i = 0; i < numVar; i++) {
      EVariable v = null;
    
      // find a variable with name==localOrder[i]
      for (String varName : variables.keySet()) {        
        EVariable curV = variables.get(varName);
        if (curV != null && curV.getName() != null && curV.getName().equals(localOrder[i])) {
          v = curV;
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
    result = result.substring(0,result.length()-delim.length());
    if (variables.values().contains(EResult.getErrorIndicator(""))) {
      result += delim + variables.get(EResult.errorParName).getValue();
    }
    return result;
  }
  
  @Override
  public Iterator<EVariable> iterator() {
      return variables.values().iterator();
  }

}
