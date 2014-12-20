package si.fri.algotest.entities;

import java.io.File;
import java.util.ArrayList;

/**
 * A TestSet entity. 
 * 
 * @author tomaz
 */
public class ETestSet extends Entity {  
  // Entity identifier
  public static final String ID_TestSet       ="TestSet";
  
  //Fields
  public static final String ID_ShortName     ="ShortName";	  // String
  public static final String ID_Desc          ="Description";     // String
  public static final String ID_HTMLDescFile  ="HTMLDescFile";    // Filename  
  public static final String ID_N             ="N";		  // Integer
  public static final String ID_TestRepeat    ="TestRepeat";	  // Integer
  public static final String ID_TimeLimit     ="TimeLimit";	  // Integer  
  public static final String ID_QuickTest     ="QuickTest";	  // Boolean
  public static final String ID_TestSetFiles  ="TestSetFiles";	  // String []
  public static final String ID_DescFile      ="DescriptionFile"; // Filename  
  
   
  public ETestSet() {
   super(ID_TestSet, 
	 new String [] {ID_ShortName, ID_Desc, ID_HTMLDescFile, ID_N,  
                        ID_TestRepeat, ID_TimeLimit, ID_QuickTest, ID_TestSetFiles, ID_DescFile}
	);
  }
  
  public ETestSet(File fileName) {
    this();
    initFromFile(fileName);
    
    setRepresentatives(ID_ShortName, ID_Desc);
  }
  
  
  /**
   * The value of the "DescriptionFile" field
   */
  public String getTestSetDescriptionFile() {
    return getField(ID_DescFile);
  }
}


