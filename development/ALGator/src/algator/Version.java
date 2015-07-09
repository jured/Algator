package algator;

import si.fri.algotest.global.ATGlobal;

/**
 *
 * @author tomaz
 */
public class Version {
  private static String version = "0.6.102";
  private static String date    = "Maj 2015";
  
  public static String getVersion() {
    return String.format("version %s (%s)", version, date);
  }
  
  /**
   * Method returns the location of the classes of this project, i.e. the location of the JAR
   * file, if the program was executed from JAR, or the root folder of project's classes otherwise  
   */
  public static String getClassesLocation() {
    try {
      return Version.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    } catch (Exception e) {
      return "";
    }
  }
  
  public static void main(String[] args) {
    System.out.printf("ALGator, %s, build %s\n", getVersion(), ATGlobal.getBuildNumber());
    System.out.println("AlgatorRoot:     " + ATGlobal.getALGatorRoot());
    System.out.println("AlgatorDataRoot: " + ATGlobal.getALGatorDataRoot());
  }
}
