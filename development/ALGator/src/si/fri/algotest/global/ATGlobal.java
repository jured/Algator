package si.fri.algotest.global;

import java.io.File;
import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;
import si.fri.algotest.entities.ELocalConfig;
import si.fri.algotest.entities.MeasurementType;

/**
 * Definition of global values use in the AT project
 *
 * @author tomaz
 */
public class ATGlobal {

  private static String ALGatorRoot      = System.getenv("ALGATOR_ROOT");
  private static String ALGatorDataRoot  = System.getenv("ALGATOR_DATA_ROOT");
  private static String ALGatorDataLocal = System.getenv("ALGATOR_DATA_LOCAL");
  
  
  public static int logTarget = 1;    // stdout
  public static int verboseLevel = 1; // print some information
  
    
  // File extensions for AT entities
  public static final String AT_FILEEXT_project    = "atp";
  public static final String AT_FILEEXT_algorithm  = "atal";
  public static final String AT_FILEEXT_testset    = "atts";
  public static final String AT_FILEEXT_resultdesc = "atrd";
  public static final String AT_FILEEXT_query      = "atqd";

  // For the structure of the Project folder see ALGator.docx documentation
  private static final String ATDIR_data_local     = "data_local";
  private static final String ATDIR_data_root      = "data_root";
  private static final String ATDIR_projects       = "projects";  
  private static final String ATDIR_projRootDir    = "PROJ-%s";
  private static final String ATDIR_projConfDir    = "proj";
  private static final String ATDIR_srcDir         = "src";
  private static final String ATDIR_binDir         = "bin";
  private static final String ATDIR_testsDir       = "tests";
  private static final String ATDIR_resultsDir     = "results";
  private static final String ATDIR_algsDir        = "algs";
  private static final String ATDIR_algDir         = "ALG-%s";
  private static final String ATDIR_queryDir       = "queries";
  private static final String ATDIR_queryOutput    = "output";
  
  private static final String ATDIR_logDir         = "log";
  private static final String ATDIR_algatorLOGfile = "algator.log";
  private static final String ATDIR_taskLogDir     = "tasks";
  
  private static final String ATDIR_tmpDir         = "tmp";

  
  private static final String ATDIR_localConfigDir    = "local_config";
  private static final String LOCAL_CONFIG_FILENAME   = "config.atlc";  
  private static final String ATDIR_globalConfigDir   = "global_config";
  private static final String GLOBAL_CONFIG_FILENAME  = "config.atgc";  
  
  private static final String ATDIR_libDir         = "lib";
  
  public static final String COUNTER_CLASS_EXTENSION = "_COUNT"; 
  
  
  
  public static String getALGatorRoot() {
    String result = ALGatorRoot;
    if (result == null || result.isEmpty())
      result = System.getProperty("user.dir", "");
    return result;
  }
  
  public static String getALGatorDataRoot() {
    String result = ALGatorDataRoot;
    if (result == null || result.isEmpty())
      result = getALGatorRoot() + File.separator + ATDIR_data_root;
    return result;
  }

  public static String getALGatorDataLocal() {
    String result = ALGatorDataLocal;
    if (result == null || result.isEmpty())
      result = getALGatorRoot() + File.separator + ATDIR_data_local;
    return result;
  }
  
  
  public static void setALGatorRoot(String algatorRoot) {
    ALGatorRoot = algatorRoot;
  }
  public static void setALGatorDataRoot(String dataRoot) {
    ALGatorDataRoot = dataRoot;
  }
  public static void setALGatorDataLocal(String dataLocal) {
    ALGatorDataLocal = dataLocal;
  }

  
  public static String getLogFolder() {
    String folderName = getALGatorDataRoot() + File.separator + ATDIR_logDir;
    File folder = new File(folderName);
    if (!folder.exists())
      folder.mkdir();
    return folderName;
  }

  public static String getTaskLogFolder() {
    String folderName = getLogFolder() + File.separator + ATDIR_taskLogDir;
    File folder = new File(folderName);
    if (!folder.exists())
      folder.mkdir();
    return folderName;    
  }
  
  public static String getAlgatorLogFilename() {
    return getLogFolder() + File.separator + ATDIR_algatorLOGfile;
  }
  
  public static String getTaskStatusFilename(String project, String algorithm, String testset, String mtype) {
    return getTaskLogFolder() + File.separator + 
       String.format("%s-%s-%s-%s.status", project, algorithm, testset, mtype);
  }

  public static String getTaskHistoryFilename(String project, String algorithm, String testset, String mtype) {
    return getTaskLogFolder() + File.separator + 
       String.format("%s-%s-%s-%s.history", project, algorithm, testset, mtype);
  }

  /**
   * Extracts and returns the data root folder from the project root folder.
   * Example: /ALGATOR_ROOT/data_root/projects/PROJ-Sorting -> /ALGATOR_ROOT/data_root
   */
  public static String getDataRootFromProjectRoot(String projRoot) {
    //     /projects/PROJ-
    String middleStr = File.separator + ATDIR_projects + File.separator + String.format(ATDIR_projRootDir, "");
    int pos = projRoot.lastIndexOf(middleStr);
    return (pos != -1 ? projRoot.substring(0, pos) : projRoot);
  }
  
  /**
   * Returns the root of the project
   *
   * @param data_root root for all projects
   * @param projName project name
   * @return
   */
  public static String getPROJECTroot(String data_root, String projName) {
    return data_root + File.separator + ATDIR_projects + File.separator + String.format(ATDIR_projRootDir, projName);
  }

  /**
   * Returns the name of the project configuration file
   *
   * @param data_root root for all projects
   * @param projName project name
   */
  public static String getPROJECTfilename(String data_root, String projName) {
    return getPROJECTroot(data_root, projName) + File.separator
            + ATDIR_projConfDir + File.separator + projName + "." + AT_FILEEXT_project;
  }

  /**
   * Returns the name of the folder with template file(s) and other java
   * sources.
   *
   * @param projectRoot
   * @return
   */
  public static String getPROJECTsrc(String projectRoot) {
    return projectRoot + File.separator + ATDIR_projConfDir + File.separator + ATDIR_srcDir;
  }

  /**
   * Returns the name of the folder with compiled project's java sources.   
   * This folder is subfolder of DATA_LOCAL folder
   */
  public static String getPROJECTbin(String projectName) {
    return getPROJECTroot(getALGatorDataLocal(), projectName) + File.separator 
            + ATDIR_projConfDir + File.separator + ATDIR_binDir;
  }

  
  public static String getPROJECTlib(String projectRoot) {
    return projectRoot + File.separator + ATDIR_projConfDir + File.separator + ATDIR_libDir;
  }

  
  public static String getALGORITHMroot(String projectRoot, String algName) {
    return projectRoot + File.separator + ATDIR_algsDir + File.separator
            + String.format(ATDIR_algDir, algName);
  }

  public static String getALGORITHMfilename(String projectRoot, String algName) {
    return getALGORITHMroot(projectRoot, algName) + File.separator + algName + "." + AT_FILEEXT_algorithm;
  }

  public static String getALGORITHMsrc(String projectRoot, String algName) {
    return getALGORITHMroot(projectRoot, algName) + File.separator + ATDIR_srcDir;
  }

  // Algorithm's bin folder is a DATA_LOCAL subfolder
  public static String getALGORITHMbin(String projectName, String algName) {
    return getALGORITHMroot(getPROJECTroot(getALGatorDataLocal(), projectName), algName) + File.separator + ATDIR_binDir;
  }

  
  /************* TESTS *+++++++++++++++++++++*/
  public static String getTESTSroot(String data_root, String projectName) {
    String projectRoot = getPROJECTroot(data_root, projectName);
    return projectRoot + File.separator + ATDIR_testsDir;
  }
  /**
   * Returns the name of a test set configuration file. This file is placed in
   * the projects tests folder
   */
  public static String getTESTSETfilename(String data_root, String projectName, String testSetName) {
    return getTESTSroot(data_root, projectName) + File.separator + testSetName + "." + AT_FILEEXT_testset;
  }

  
  
  /************* RESULTS *+++++++++++++++++++++*/
  public static String getRESULTDESCfilename(String projectRoot, String projName, MeasurementType measurementType) {
    return projectRoot + File.separator + ATDIR_projConfDir + File.separator + projName + "-" + measurementType.getExtension() + "." + AT_FILEEXT_resultdesc;
  }

  public static String getRESULTSroot(String projectRoot, String computerID) {
    return projectRoot + File.separator + ATDIR_resultsDir + File.separator + computerID;
  }    

  /**
   * The name of a file containing results of the execution of the algorithm
   * {@code algName} on test set {@code testSetName}.
   */
  public static String getRESULTfilename(String projectRoot, String algName, String testSetName, MeasurementType measurementType, String computerID) {
    return getRESULTSroot(projectRoot, computerID) + File.separator + algName + "-" + testSetName + "." + measurementType.getExtension();
  }


  /**
   * The name of a file on tmpFolder to hold info of one test
   * @return 
   */
  public static String getJVMRESULTfilename(String tmpDir, String algName, String testSetName, int testNumber) {
    return tmpDir + File.separator + algName + "-" + testSetName + "-"+testNumber + "." + MeasurementType.JVM.getExtension();
  }


  public static String getQUERIESroot(String projectRoot) {
    return projectRoot + File.separator + ATDIR_queryDir;
  }

  public static String getQUERYfilename(String projectRoot, String query) {
    return getQUERIESroot(projectRoot) + File.separator + query + "." + AT_FILEEXT_query;
  }
  
  public static String getQUERYOutputFilename(String projectRoot, String query, String [] params) {
    String folderName = getQUERIESroot(projectRoot) + File.separator + ATDIR_queryOutput;
    File tmpFolder = new File(folderName);
    if (!tmpFolder.exists())
      tmpFolder.mkdirs();
    
    String fileName = query; 
    if (params != null) 
      for (String param : params) {
        param = param.replaceAll("[^a-zA-Z0-9.-]", "_");
        fileName += "_"+param;
      }
    
    return folderName + File.separator + fileName;
  }
  
    
  public static String getTMProot(String data_root, String projName) {
    return getPROJECTroot(data_root, projName) + File.separator + ATDIR_tmpDir;
  }
  
  public static String getTMPDir(String data_root, String projName) {
    String folderName = getTMProot(data_root, projName) + File.separator + "alg" + (new Random()).nextLong();
    File tmpFolder = new File(folderName);
    if (!tmpFolder.exists())
      tmpFolder.mkdirs();
    
    return folderName;
  }
  
  public static String getLocalConfigFilename() {
    return getALGatorRoot() + File.separator + ATDIR_localConfigDir + File.separator + LOCAL_CONFIG_FILENAME;
  }
  
  public static String getGlobalConfigFilename() {
    return getALGatorDataRoot() + File.separator + ATDIR_globalConfigDir + File.separator + GLOBAL_CONFIG_FILENAME;
  }
  
  public static String getThisComputerID() {
    try {
      ELocalConfig config = ELocalConfig.getConfig();
      String id = config.getComputerID();
      if (id == null || id.isEmpty())
        return "C0";
      else
        return id;
    } catch (Exception e) {
      return "C0";
    }
  }
  
  
  public static final String getBuildNumber() { 
   String msg; 
   try { 
     // resource bundle that provides the build number
     ResourceBundle versionRB = ResourceBundle.getBundle("version"); 
     msg = versionRB.getString("BUILD"); 
   } catch (MissingResourceException e) { 
     msg = e.toString();
   } 
   return msg; 
  } 


}
