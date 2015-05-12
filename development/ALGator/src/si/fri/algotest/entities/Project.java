package si.fri.algotest.entities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.global.ErrorStatus;

/**
 * A class that combines all the  information about the project (EProject, all
 * EAlgorithms and ETestSets).
 * @author tomaz
 */
public class Project {
  
  // the data_root folder name ...
  public String dataRoot;
  // .. and the project name
  public String projectName;
  
  
  private TreeMap<String, EAlgorithm> algorithms;
  private TreeMap<String, ETestSet>   testsets;
  private HashMap<MeasurementType, EResultDescription>   resultDescriptions;
	  
  private EProject eProject;
  
  /**
   * If an error occures when reading EProject file, the first entry of this list is
   * ERROR_INVALID_PROJECT. Otherwise, the first entry is STATUS_OK. Other entries
   * represent error that occure while loading algorithm, datasets and resultdescriptions.
   */
  ArrayList<ErrorStatus> errors;
 
  public Project(String dataRoot, String projectName) {
    this.dataRoot    = dataRoot;
    this.projectName = projectName;
    
    errors = new ArrayList();
    
    algorithms = new TreeMap();
    testsets   = new TreeMap();
    resultDescriptions = new HashMap();
    
    // read the eProject
    String projFilename = ATGlobal.getPROJECTfilename(dataRoot, projectName);
    eProject = new EProject(new File(projFilename));
    if (!ErrorStatus.getLastErrorStatus().isOK()) {
      errors.add(ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_PROJECT, ""));
      return;
    }
 
    errors.add(ErrorStatus.STATUS_OK);
    
    // read the algorithms
    String [] algNames = eProject.getStringArray(EProject.ID_Algorithms);
    for(String algName : algNames) {   
      String algFilename = ATGlobal.getALGORITHMfilename(eProject.getProjectRootDir(), algName);
      EAlgorithm eAlgorithm = new EAlgorithm(new File(algFilename));
      if (ErrorStatus.getLastErrorStatus().isOK()) {
	algorithms.put(algName, eAlgorithm);
      } else
	errors.add(ErrorStatus.getLastErrorStatus()); 
    }
    
    // read the testsets
    String [] tsNames = eProject.getStringArray(EProject.ID_TestSets);
    for(String tsName : tsNames) {   
      String tsFilename = ATGlobal.getTESTSETfilename(eProject.getProjectRootDir(), tsName);
      ETestSet eTestset = new ETestSet(new File(tsFilename));
      if (ErrorStatus.getLastErrorStatus().isOK()) {
	testsets.put(tsName, eTestset);
      } else
	errors.add(ErrorStatus.getLastErrorStatus()); 

    }
    
    // read the resultDescriptions
    for(MeasurementType mType : MeasurementType.values()) {
      String rdFilename = ATGlobal.getRESULTDESCfilename(eProject.getProjectRootDir(), projectName, mType);
      if (!mType.equals(MeasurementType.EM)) // only for EM type an error message is shown; 
        ATLog.disableLog();
      EResultDescription eResrulDescription = new EResultDescription(new File(rdFilename));
      ATLog.enableLog();
      if (ErrorStatus.getLastErrorStatus().isOK()) {
        resultDescriptions.put(mType, eResrulDescription);
      } else {
   	  errors.add(ErrorStatus.getLastErrorStatus()); 
      }
    }
  }

  public EProject getProject() {
    return eProject;
  }
  
  public ArrayList<ErrorStatus> getErrors() {
    return errors;
  }
  
  public String getName() {
    return (eProject==null) ? "?" : eProject.getName();
  }
  
  public TreeMap<String, EAlgorithm> getAlgorithms() {
    return algorithms;
  }
  public TreeMap<String, ETestSet> getTestSets() {
    return testsets;
  }

  public HashMap<MeasurementType, EResultDescription> getResultDescriptions() {
    return resultDescriptions;
  }
  
  
  /**
   * Returns an array of test parameters for this project
   * @return 
   */
  public String[] getTestParameters() {
    if (resultDescriptions == null || resultDescriptions.get(MeasurementType.EM) == null) 
      return new String[0];
    else
      return resultDescriptions.get(MeasurementType.EM).getStringArray(EResultDescription.ID_TestParOrder);
  }
  
    /**
   * Returns an array of result parameters for this project (merged from all atrd files)
   * @return 
   */
  public String[] getResultParameters() {
    ArrayList<String> params = new ArrayList();
    
    for (EResultDescription eRedDesc : resultDescriptions.values()) {
      String [] tParams = eRedDesc.getStringArray(EResultDescription.ID_ResultParOrder);
      for (String param : tParams) 
        if (!params.contains(param))
          params.add(param);
    }
    
    return (String []) params.toArray(new String[0]);
  }

}
