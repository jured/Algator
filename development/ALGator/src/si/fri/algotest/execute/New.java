package si.fri.algotest.execute;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import si.fri.algotest.entities.EAlgorithm;
import si.fri.algotest.entities.EProject;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.Project;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.tools.ATTools;

/**
 *
 * @author tomaz
 */
public class New {

  // For a given pair (project, algorithm) we always use the same loader 
  private static final HashMap<String, URLClassLoader> classloaders = new HashMap<>();

  public static URL[] getClassPathsForAlgorithm(Project project, String algName) {
      String projBin = ATGlobal.getPROJECTbin(project.getProject().getProjectRootDir());
      String algBin = ATGlobal.getALGORITHMbin(project.getProject().getProjectRootDir(), algName);

      URL[] proJARs = ATTools.getURLsFromJARs(project.getProject().getStringArray(EProject.ID_ProjectJARs), ATGlobal.getPROJECTlib(project.getProject().getProjectRootDir()));
      URL[] algJARs = ATTools.getURLsFromJARs(project.getProject().getStringArray(EProject.ID_AlgorithmJARs), ATGlobal.getPROJECTlib(project.getProject().getProjectRootDir()));
      
      URLClassLoader parentclassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
      URL[] parentURLs = parentclassLoader.getURLs();

      URL[] urls = new URL[parentURLs.length + 2 + proJARs.length + algJARs.length];
      int stevec = 0;

      for (int j = 0; j < parentURLs.length; j++) {
          urls[stevec++] = parentURLs[j];
      }
      for (int j = 0; j < proJARs.length; j++) {
          urls[stevec++] = proJARs[j];
      }
      for (int j = 0; j < algJARs.length; j++) {
          urls[stevec++] = algJARs[j];
      }
      try {
        urls[stevec++] = new File(projBin).toURI().toURL();
        urls[stevec++] = new File(algBin).toURI().toURL();
      } catch (Exception e) {
      }
      
      return urls;
  }
  
  
  private static URLClassLoader getClassloader(Project project, String algName) {
    String key = project.getName() + "+" + algName;
    URLClassLoader result = classloaders.get(key);

    if (result == null) {
      try {
        URL[] urls = getClassPathsForAlgorithm(project, algName);
        classloaders.put(key, (result = URLClassLoader.newInstance(urls)));
      } catch (Exception e) {
        ATLog.log("Error creating class loader: " + e.toString());
      }
    }
    return result;
  }

  static AbsAlgorithm algorithmInstance(Project project, String algName, MeasurementType mType) {
    AbsAlgorithm result = null;
    try {
      URLClassLoader classLoader = getClassloader(project, algName);
      String algClassName = project.getAlgorithms().get(algName).getField(EAlgorithm.ID_MainClassName);

      if (mType.equals(MeasurementType.CNT)) {
        algClassName += ATGlobal.COUNTER_CLASS_EXTENSION;
      }

      Class algClass = Class.forName(algClassName, true, classLoader);
      result = (AbsAlgorithm) algClass.newInstance();
    } catch (Exception e) {
      ATLog.log("Can't make an instance of algorithm " + algName + ": " + e);
    }
    return result;
  }

  static AbstractTestSetIterator testsetIteratorInstance(Project project, String algName) {
    AbstractTestSetIterator result = null;
    try {
      URLClassLoader classLoader = getClassloader(project, algName);
      String testSetIteratorClassName
              = ATTools.stripFilenameExtension((String) project.getProject().getField(EProject.ID_TestSetIteratorClass));

      Class tsClass = Class.forName(testSetIteratorClassName, true, classLoader);
      result = (AbstractTestSetIterator) tsClass.newInstance();
    } catch (Exception e) {
      ATLog.log("Can't make an instance of testset iterator for project" + project.getName() + ": " + e);
    }
    return result;
  }

}
