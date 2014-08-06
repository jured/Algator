package si.fri.algotest.entities;

import java.io.File;

/**
 *
 * @author tomaz
 */
public class EProject extends Entity {
  // Entity identifier
  public static final String ID_Project   ="Project";
  
  //Fields
  public static final String ID_Description           ="Description";	       // String
  public static final String ID_Author                ="Author";	       // String
  public static final String ID_Date                  ="Date";		       // String
  public static final String ID_HtmlDescFile          ="HtmlDescriptionFile";  // String
  public static final String ID_Algorithms            ="Algorithms";	       // String []
  public static final String ID_TestSets              ="TestSets";	       // String []
  public static final String ID_AlgorithmTPL          ="AlgorithmClass";       // String
  public static final String ID_TestCaseClass         ="TestCaseClass";         // String
  public static final String ID_TestSetIteratorClass  ="TestSetIteratorClass"; // String

  
  
  public EProject() {
   super(ID_Project, 
	 new String [] {ID_Description, ID_Author, ID_Date, ID_HtmlDescFile, 
	                ID_Algorithms, ID_AlgorithmTPL,
	                ID_TestSets, ID_AlgorithmTPL, ID_TestCaseClass,
			ID_TestSetIteratorClass}
	);
   setRepresentatives(ID_Author);
  }
  
  public EProject(File fileName) {
    this();
    initFromFile(fileName);
  }
  
  
  public String getProjectRootDir() {
    return (entity_rootdir != null && entity_rootdir.endsWith("proj")) ?
      entity_rootdir.substring(0, entity_rootdir.length()-5) : entity_rootdir;
  }
}
