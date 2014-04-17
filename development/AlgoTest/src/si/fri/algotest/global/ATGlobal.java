package si.fri.algotest.global;

import java.io.File;
import si.fri.algotest.entities.MeasurementType;

/**
 * Definition of global values use in the AT project
 *
 * @author tomaz
 */
public class ATGlobal {

  public static String ALGatorDataRoot = System.getenv("ALGATOR_DATA_ROOT");

  // File extensions for AT entities
  public static final String AT_FILEEXT_project = "atp";
  public static final String AT_FILEEXT_algorithm = "atal";
  public static final String AT_FILEEXT_testset = "atts";
  public static final String AT_FILEEXT_resultdesc = "atrd";
  public static final String AT_FILEEXT_query = "atqd";

  // For the structure of the Project folder see ALGator.docx documentation
  private static final String ATDIR_projects = "projects";

  private static final String ATDIR_projRootDir = "PROJ-%s";
  private static final String ATDIR_projConfDir = "proj";
  private static final String ATDIR_srcDir = "src";
  private static final String ATDIR_binDir = "bin";
  private static final String ATDIR_testsDir = "tests";
  private static final String ATDIR_resultsDir = "results";
  private static final String ATDIR_algsDir = "algs";
  private static final String ATDIR_algDir = "ALG-%s";
  private static final String ATDIR_queryDir = "queries";

  /**
   * Returns the root of the project
   *
   * @param root root for all projects
   * @param projName project name
   * @return
   */
  public static String getPROJECTroot(String root, String projName) {
    return root + File.separator + ATDIR_projects + File.separator + String.format(ATDIR_projRootDir, projName);
  }

  /**
   * Returns the name of the project configuration file
   *
   * @param root root for all projects
   * @param projName project name
   */
  public static String getPROJECTfilename(String root, String projName) {
    return getPROJECTroot(root, projName) + File.separator
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

  public static String getRESULTDESCfilename(String projRoot, String projName, MeasurementType measurementType) {
    return projRoot + File.separator + ATDIR_projConfDir + File.separator + projName + "-" + measurementType.getExtension() + "." + AT_FILEEXT_resultdesc;
  }

  public static String getRESULTSroot(String projectRoot) {
    return projectRoot + File.separator + ATDIR_resultsDir;
  }

  /**
   * The name of a file containing results of the execution of the algorithm
   * {@code algName} on test set {@code testSetName}.
   */
  public static String getRESULTfilename(String projectRoot, String algName, String testSetName, MeasurementType measurementType) {
    return getRESULTSroot(projectRoot) + File.separator + algName + "-" + testSetName + "." + measurementType.getExtension();
  }

  public static String getQUERIESroot(String projectRoot) {
    return projectRoot + File.separator + ATDIR_queryDir;
  }

  public static String getQUERYfilename(String projectRoot, String query) {
    return getQUERIESroot(projectRoot) + File.separator + query + "." + AT_FILEEXT_query;
  }

}
