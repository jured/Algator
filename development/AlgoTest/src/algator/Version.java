package algator;

/**
 *
 * @author tomaz
 */
public class Version {
  private static String version = "0.6";
  private static String date    = "April 2014";
  
  public static String getVersion() {
    return String.format("version %s (%s)", version, date);
  }
}
