package si.fri.algotest.entities;

import java.io.File;
import org.json.JSONArray;
import org.json.JSONObject;
import si.fri.algotest.global.ErrorStatus;

/**
 * 
 * @author tomaz
 */
public class EResultDescription extends Entity {
  /**
   * The numnber of fileds added to every result file.
   * Currently: 4 (the name of the algorithm, the testset, the test and pass (DONE/KILLED))
   */
  public static final int FIXNUM = 4;
  public static final String algParName      = "Algorithm"; 
  public static final String tstParName      = "Testset"; 
  public static final String testIDParName   = "TestID";   // Unique identificator of a test within a testset  
  public static final String passParName     = "Pass";     // DONE if algorithem finished within the given time limit, KILLLED otherwise
  
  // unique sequence number of a test in a tabel (id of table row)
  public static final String testNoParName   = "ID";     

  // Entity identifier
  public static final String ID_ResultDescription   = "ResultDescription";
  
  // Fields
  public static final String ID_Format          = "Format";           // String
  public static final String ID_Delim           = "Delimiter";        // String
  public static final String ID_TestParOrder    = "TestParameters";   // String []
  public static final String ID_ResultParOrder  = "ResultParameters"; // String []
  public static final String ID_params          = "Parameters";       // EParameter []
  
   public EResultDescription() {
     super(ID_ResultDescription, 
	 new String [] {ID_Format, ID_Delim, ID_TestParOrder, ID_ResultParOrder, ID_params});
     set(ID_params, new JSONArray());
  }
  
  public EResultDescription(File fileName) {
    this();
    initFromFile(fileName);

  }
  
  public EResultDescription(String json) {
    this();
    initFromJSON(json);
  }
  
   /**
   * Method return an String array obtained from corresponding field. 
   */
  public ParameterSet getParameters() {
    try {
      JSONArray ja = getField(ID_params);
      ParameterSet result = new ParameterSet();
      
      // add FIXNUM default parameters
      result.addParameter(getAlgorithmNameParameter("/"), true);
      result.addParameter(getTestsetNameParameter("/"), true);
      result.addParameter(getTestIDParameter("/"), true);
      result.addParameter(getPassParameter(true), false);
      
      
      for (int i = 0; i < ja.length(); i++) {
	JSONObject jo = ja.getJSONObject(i);
	result.addParameter(new EParameter(jo.toString()), true);
      }
      return result;
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_NOT_A_RESULTPARAMETER_ARRAY, ID_params);
      
      return new ParameterSet();
    }
  }
  
  /**
   * Method returns the order of the parameters to be printed in the result file. The 
   * parameters returned in the following order: defulat parameters (e.q. alg. name and testset name), 
   * test parameters (ID_TestParOrder order) and  result parameters in ID_ResultParOrder order.
   */
  public String [] getParamsOrder() {
    String [] orderA = getStringArray(EResultDescription.ID_TestParOrder);
    String [] orderB = getStringArray(EResultDescription.ID_ResultParOrder);
    if (!ErrorStatus.getLastErrorStatus().isOK()) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_RESULTDESCRIPTION, ErrorStatus.getLastErrorMessage());
      //return null;
    }

    String [] order = new String[orderA.length + orderB.length + FIXNUM];
    
    // Add "Algorithm", "TestSet" and "Pass" parameters to the set of output parameters.
    // The number of parameters added to every result line is defined in EResultDescription.FIXNUM
    order[0] = EResultDescription.algParName;
    order[1] = EResultDescription.tstParName;
    order[2] = EResultDescription.testIDParName;
    order[3] = EResultDescription.passParName;
    
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
  public static EParameter getAlgorithmNameParameter(String algName) {
    return new EParameter(algParName, "Algorithm name", ParameterType.STRING, algName);
  }

  /**
   * Returns a parameter that represents the testset name
   */
  public static EParameter getTestsetNameParameter(String tstName) {
    return new EParameter(tstParName, "Testset name", ParameterType.STRING, tstName);
  }
  
  /**
   * Returns a parameter that represents the success of the algorithm (DONE or KILLED)
   */
  public static EParameter getPassParameter(boolean pass) {
    String passStr = pass ? "DONE" : "KILLED";
    return new EParameter(passParName, "Algorithm passed", ParameterType.STRING, passStr);
  }

  /**
   * Returns a testID paremeter
   */
  public static EParameter getTestIDParameter(String testID) {
    return new EParameter(testIDParName, "Test identificator", ParameterType.STRING, testID);
  }
}
