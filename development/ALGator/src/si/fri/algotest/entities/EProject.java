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
  public static final String ID_Description           = "Description";	        // String
  public static final String ID_HtmlDescFile          = "HtmlDescFile";         // Filename
  public static final String ID_Author                = "Author";	        // String
  public static final String ID_Date                  = "Date";		        // String
  public static final String ID_AlgDescHTML           = "AlgDescHTML";          // Filename
  public static final String ID_AlgTechDescHTML       = "AlgTechDescHTML";      // Filename
  public static final String ID_Algorithms            = "Algorithms";	        // String []
  public static final String ID_TestSetDescHTML       = "TestSetDescHTML";      // Filename
  public static final String ID_TestSetTechDescHTML   = "TestSetTechDescHTML";  // Filename  
  public static final String ID_TestSets              = "TestSets";	        // String []
  public static final String ID_ProjectJARs           = "ProjectJARs";          // Filename[]
  public static final String ID_AlgorithmJARs         = "AlgorithmJARs";        // Filename[]
  public static final String ID_EMExecFamily          = "EMExecFamily";         // String
  public static final String ID_CNTExecFamily         = "CNTExecFamily";        // String
  public static final String ID_JVMExecFamily         = "JVMExecFamily";        // String

//  Depricated. In current version, java files MUST be named according to the following :
//     AlgorithmClass        === <project_name>AbstractAlgorithm.java
//     TestCaseClass         === <project_name>TestCase.java
//     TestSetIteratorClass  === <project_name>TestSetIterator.java
//
//  Names of these classes are returned by methods getAlgorithmClassname(), getTestCaseClassname() 
//    and getTestSetIteratorClassName() 
//
//  public static final String ID_AlgorithmClass        = "AlgorithmClass";       // String
//  public static final String ID_TestCaseClass         = "TestCaseClass";        // String
//  public static final String ID_TestSetIteratorClass  = "TestSetIteratorClass"; // String
  
  
  
  
  public EProject() {
   super(ID_Project, 
	 new String [] {ID_Description, ID_HtmlDescFile, ID_Author, ID_Date,  
	                ID_Algorithms, ID_AlgDescHTML, ID_AlgTechDescHTML,
                        ID_TestSets, ID_TestSetDescHTML, ID_TestSetTechDescHTML,
                        ID_ProjectJARs, ID_AlgorithmJARs, 
                        ID_EMExecFamily, ID_CNTExecFamily, ID_JVMExecFamily}
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

    
  public String getProjectFamily(MeasurementType mt) {
    String myFamily = "";
    myFamily = getField(getMeasurementTypeFieldID(mt));
    if (myFamily == null) myFamily = "";
    return myFamily;
  }
  
  /**
   * Sets the [mtype]ExecFamily field and saves settings to file.
   * @param mType
   * @param family
   * @param override 
   */
  public void setFamilyAndSave(MeasurementType mType, String family, boolean override) {
    String myFamily = getProjectFamily(mType);    
    
    if (override || myFamily == null || myFamily.isEmpty()) {
      String familyFieldID = getMeasurementTypeFieldID(mType);
      set(familyFieldID, family);
      saveEntity();
    }
  }    
  
  // Returns fieldID for given mesatrement type.
  public static String getMeasurementTypeFieldID(MeasurementType mt) {
    switch (mt) {
        case EM:
          return EProject.ID_EMExecFamily; 
        case CNT:
          return EProject.ID_CNTExecFamily; 
        case JVM:
          return EProject.ID_JVMExecFamily; 
    }
    return "?";
  }
  

  // Names of java classes for the project
  public String getAbstractAlgorithmClassname() {
    return getName() + "AbsAlgorithm";
  }
  public String getTestCaseClassname() {
    return getName() + "TestCase";
  }
  public String getTestSetIteratorClassName() {
    return getName() + "TestSetIterator";
  }
}
