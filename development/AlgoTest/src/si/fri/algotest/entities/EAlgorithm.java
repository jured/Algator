package si.fri.algotest.entities;

import java.io.File;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public class EAlgorithm extends Entity {
  // Entity identifier
  public static final String ID_Algorithm   ="Algorithm";  
  
  //Fields
  public static final String ID_ShortName      ="ShortName";      // String
  public static final String ID_Description    ="Description";    // String
  public static final String ID_Author         ="Author";	  // String
  public static final String ID_Date           ="Date";	          // String
  public static final String ID_Classes        ="Classes";        // String []
  
  public EAlgorithm() {
   super(ID_Algorithm, 
	 new String [] {ID_ShortName, ID_Description, ID_Author,
	                ID_Date, ID_Classes});
  }
  
  public EAlgorithm(File fileName) {
    this();
    initFromFile(fileName);
    setRepresentatives(ID_ShortName, ID_Author);
  } 
  
  public ErrorStatus copyAndComplile(String workingDir) {
    ErrorStatus curES = ErrorStatus.STATUS_OK;
    
    return curES;
  }
}
