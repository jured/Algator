package si.fri.algotest.global;

import java.io.File;
import java.util.Random;
import si.fri.algotest.entities.ELocalConfig;
import si.fri.algotest.entities.MeasurementType;

/**
 * Definition of global values use in the AT project
 *
 * @author tomaz
 */
public class ATGlobal {

  private static String ALGatorRoot     = System.getenv("ALGATOR_ROOT");
  private static String ALGatorDataRoot = System.getenv("ALGATOR_DATA_ROOT");
    
  // File extensions for AT entities
  public static final String AT_FILEEXT_project    = "atp";
  public static final String AT_FILEEXT_algorithm  = "atal";
  public static final String AT_FILEEXT_testset    = "atts";
  public static final String AT_FILEEXT_resultdesc = "atrd";
  public static final String AT_FILEEXT_query      = "atqd";

  // For the structure of the Project folder see ALGator.docx documentation
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

  public static void setALGatorRoot(String algatorRoot) {
    ALGatorRoot = algatorRoot;
  }
  public static void setALGatorDataRoot(String dataRoot) {
    ALGatorDataRoot = dataRoot;
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
   * Returns the name of the folder with compiled java sources.
   *
   * @param projectRoot
   * @return
   */
  public static String getPROJECTbin(String projectRoot) {
    return projectRoot + File.separator + ATDIR_projConfDir + File.separator + ATDIR_binDir;
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

  public static String getALGORITHMbin(String projectRoot, String algName) {
    return getALGORITHMroot(projectRoot, algName) + File.separator + ATDIR_binDir;
  }

  /**
   * Returns the name of a test set configuration file. This file is placed in
   * the projects tests folder
   */
  public static String getTESTSETfilename(String projectRoot, String testSetName) {
    return projectRoot + File.separator + ATDIR_testsDir + File.separator + testSetName + "." + AT_FILEEXT_testset;
  }

  public static String getTESTSroot(String projectRoot) {
    return projectRoot + File.separator + ATDIR_testsDir;
  }

  public static String getRESULTDESCfilename(String projectRoot, String projName, MeasurementType measurementType) {
    return projectRoot + File.separator + ATDIR_projConfDir + File.separator + projName + "-" + measurementType.getExtension() + "." + AT_FILEEXT_resultdesc;
  }

  public static String getRESULTSroot(String projectRoot) {
    return projectRoot + File.separator + ATDIR_resultsDir + File.separator + getThisComputerID();
  }

  /**
   * The name of a file containing results of the execution of the algorithm
   * {@code algName} on test set {@code testSetName}.
   */
  public static String getRESULTfilename(String projectRoot, String algName, String testSetName, MeasurementType measurementType) {
    return getRESULTSroot(projectRoot) + File.separator + algName + "-" + testSetName + "." + measurementType.getExtension();
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
      String id = config.getField(ELocalConfig.ID_COMPID);
      if (id == null || id.isEmpty())
        return "C0";
      else
        return id;
    } catch (Exception e) {
      return "C0";
    }
  }

}
