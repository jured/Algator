package si.fri.algotest.entities;

import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.global.ExecutionStatus;

/**
 * 
 * @author tomaz
 */
public class EResult extends Entity {
  /**
   * The numnber of fileds added to every result file.
   * Currently: 4 (the name of the algorithm, the testset, the test and pass (DONE/FAILED))
   */
  public static final int FIXNUM = 4;
  public static final String algParName      = "Algorithm"; 
  public static final String tstParName      = "Testset"; 
  public static final String testIDParName   = "TestID";   // Unique identificator of a test within a testset  
  public static final String passParName     = "Pass";     // DONE if algorithem finished within the given time limit, FAILED otherwise
  public static final String errorParName    = "Error";    // if an error occures, this parameter contains error message
  
  // unique sequence number of a test in a tabel (id of table row)
  public static final String testNoParName   = "ID";     

  // Entity identifier
  public static final String ID_Result   = "Result";
  
  // Fields
  public static final String ID_ParOrder        = "ParameterOrder";   // String []
  public static final String ID_IndOrder        = "IndicatorOrder";   // String []
  public static final String ID_parameters      = "Parameters";       // EVariable []
  public static final String ID_indicators      = "Indicators";       // EVariable []
  
  
  // variables of this testset
  private VariableSet variables;
  
   public EResult() {
     
     super(ID_Result, 
	 new String [] {ID_ParOrder, ID_IndOrder, ID_parameters, ID_indicators});
         set(ID_parameters, new JSONArray());
         set(ID_indicators, new JSONArray());
  }
  
  public EResult(File fileName) {
    this();
    initFromFile(fileName);

  }
  
  public EResult(String json) {
    this();
    initFromJSON(json);
  }
  
   /**
   * Method return an String array obtained from corresponding field. 
   */
  public VariableSet getVariables() {
    if (variables != null)
      return variables;
    
    try {
      VariableSet result = new VariableSet();
      
      // add FIXNUM default parameters ...
      result.addVariable(getAlgorithmNameParameter("/"), true);
      result.addVariable(getTestsetNameParameter("/"), true);
      result.addVariable(getTestIDParameter("/"), true);
      // ... and the execution status indicator
      result.addVariable(getExecutionStatusIndicator(ExecutionStatus.UNKNOWN), false);
      
      JSONArray ja = getField(ID_parameters);
      // add parameters ...
      if (ja != null) for (int i = 0; i < ja.length(); i++) {
	JSONObject jo = ja.getJSONObject(i);
	result.addVariable(new EVariable(jo.toString()), true);
      }
      // ... and indicators
      ja = getField(ID_indicators);
      if (ja != null) for (int i = 0; i < ja.length(); i++) {
	JSONObject jo = ja.getJSONObject(i);
	result.addVariable(new EVariable(jo.toString()), true);
      }
            
      // Add all undefined indicators - default type for undefined indicators is INT
      String [] indicators = getStringArray(ID_IndOrder);
      for (String indicatorName : indicators) {
        if (result.getVariable(indicatorName) == null)
          result.addVariable(new EVariable(indicatorName, indicatorName, VariableType.INT, 0), true);
      }
      variables = result;
      
      return result;
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_NOT_A_RESULTPARAMETER_ARRAY, ID_parameters);
      
      return new VariableSet();
    }
  }
  
  /**
   * Method returns the order of the variables to be printed in the result file. The 
   * variables are returned in the following order: defulat parameters (e.q. alg. name and testset name), 
   * test parameters (ID_ParOrder order) and  result indicators in ID_IndOrder order.
   */
  public String [] getVariableOrder() {
    String [] orderA = getStringArray(EResult.ID_ParOrder);
    String [] orderB = getStringArray(EResult.ID_IndOrder);
    if (!ErrorStatus.getLastErrorStatus().isOK()) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_RESULTDESCRIPTION, ErrorStatus.getLastErrorMessage());
      //return null;
    }

    String [] order = new String[orderA.length + orderB.length + FIXNUM];
    
    // Add "Algorithm", "TestSet" and "Pass" parameters to the set of output parameters.
    // The number of parameters added to every result line is defined in EResult.FIXNUM
    order[0] = EResult.algParName;
    order[1] = EResult.tstParName;
    order[2] = EResult.testIDParName;
    order[3] = EResult.passParName;
    
    int k = FIXNUM;
    for (int i = 0; i < orderA.length; i++) 
      order[k++] = orderA[i];
    for (int i = 0; i < orderB.length; i++) 
      order[k++] = orderB[i];
    
    return order;
  }
  
  
  
  
  /**
   * Returns a parameter that represents the algorithm name
   */
  public static EVariable getAlgorithmNameParameter(String algName) {
    return new EVariable(algParName, "Algorithm name", VariableType.STRING, algName);
  }

  /**
   * Returns a parameter that represents the testset name
   */
  public static EVariable getTestsetNameParameter(String tstName) {
    return new EVariable(tstParName, "Testset name", VariableType.STRING, tstName);
  }
  
  /**
   * Returns a indicator that represents the success of the algorithm (DONE or KILLED)
   */
  public static EVariable getExecutionStatusIndicator(ExecutionStatus status) {
    return new EVariable(passParName, "Algorithm execution status", VariableType.STRING, status.toString());
  }
  /**
   * Returns a testID paremeter
   */
  public static EVariable getTestIDParameter(String testID) {
    return new EVariable(testIDParName, "Test identificator", VariableType.STRING, testID);
  }

  /**
   * Returns an error indicator
   */
  public static EVariable getErrorIndicator(String errorMsg) {
    return new EVariable(errorParName, "Error message", VariableType.STRING, errorMsg.replaceAll("\n", " "));
  }

}
