package si.fri.algotest.execute;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
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
  
  
  /*
  Opomba: Prvotna verzija programa je uporabljala metodo getCLassLoader(projekt, algoritm),
    ki je za vsak par projekt-algoritm ustvarila NOV classloader. To se ni obneslo, saj je 
    vmep pri tem javljal napako: ClassCastException (kot da bi bila AbstractTestsetIterator
    in npr. SortTestsetIterator naložena z drugim nalagalnikom). Javanska verzija programa 
    (če se algator požene z običajno javo) je delala brez problemov. 
    Da sem odpravil to težavo, ves čas uporabljam ISTI nalagalnik, le classpath mu po potrebi
    dopolnjujem. V množici pathsAdded si zapomnim, katere poti sem že dodal in jih ob nadaljnjih 
    izvajanjih ne dodajam ponovno (brez tega je program delal bistveno bolj počasi).
  */
  
  private static HashSet<String> pathsAdded = new HashSet<String>();
  private static ClassLoader getClassloader(URL [] urls) throws Exception {    
    Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
    method.setAccessible(true);
    for (int i = 0; i < urls.length; i++) {
      if (!pathsAdded.contains(urls[i].toString())) {
        method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{urls[i]});
        pathsAdded.add(urls[i].toString());
      }
    }
    return ClassLoader.getSystemClassLoader();
  }
  
  /**
   * Metoda se trenutno ne uporablja - glej komentar pri metodi getClassLoader(URL [] url)
   */
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

  public static AbsAlgorithm algorithmInstance(Project project, String algName, MeasurementType mType) {
    AbsAlgorithm result = null;
    try {
      // ... glej opombo zgoraj pri getClassLoader
      // ClassLoader classLoader = getClassloader(project, algName);
      ClassLoader classLoader = getClassloader(getClassPathsForAlgorithm(project, algName));
      
      String algClassName = project.getAlgorithms().get(algName).getField(EAlgorithm.ID_MainClassName);

      if (mType.equals(MeasurementType.CNT)) {
        algClassName += ATGlobal.COUNTER_CLASS_EXTENSION;
      }

      Class algClass = Class.forName(algClassName, true, classLoader);
      result = (AbsAlgorithm) algClass.newInstance();
      
      result.setmType(mType);
    } catch (Exception e) {
      ATLog.log("Can't make an instance of algorithm " + algName + ": " + e);
    }
    return result;
  }

  public static AbstractTestSetIterator testsetIteratorInstance(Project project, String algName) {
    AbstractTestSetIterator result = null;
    try {
      // ... glej opombo zgoraj pri getClassLoader
      //URLClassLoader classLoader = getClassloader(project, algName);
      ClassLoader classLoader = getClassloader(getClassPathsForAlgorithm(project, algName));
      
      String testSetIteratorClassName
              = ATTools.stripFilenameExtension((String) project.getProject().getField(EProject.ID_TestSetIteratorClass));
      Class tsClass = Class.forName(testSetIteratorClassName, true, classLoader);
      
      result = (AbstractTestSetIterator) tsClass.newInstance();
    } catch (Exception e) {
      ATLog.log("Can't make an instance of testset iterator for project " + project.getName() + ": " + e);
    }
    return result;
  }

}
