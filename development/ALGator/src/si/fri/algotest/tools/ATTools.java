package si.fri.algotest.tools;

import si.fri.algotest.global.ErrorStatus;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.commons.io.FileUtils;
import si.fri.algotest.entities.EProject;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.Project;
import si.fri.algotest.execute.AbstractTestSetIterator;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;

/**
 *
 * @author tomaz
 */
public class ATTools {
  // nuber of attempt to create dir
  private static final int TEMP_DIR_ATTEMPTS = 10;
  
  
  
  /**
   * Creates a temporary directory.
   * @param baseDir
   * @return 
   */
  public static File createTempDir(String baseDir) {
    String baseName = "tmp" + System.currentTimeMillis() ;

    File tempDir = new File(baseDir, baseName);
    if (tempDir.mkdir()) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK,  "");
      return tempDir;
    }
    
    ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_COPY, "Failed to create directory " + tempDir.getAbsolutePath());
    return null;
  }
  
  
  /**
   * Copies the source .java files to the working directory and compiles them into .class files
   * 
   * @param sources the list of source files
   * @param workingDir the working directory to copy and compile to
   * @return {@code ERROR_CANT_COPY} if files can't be copied to the working directory, 
   * {@code ERROR_CANT_COMPILE} if files can't be compiled, otherwise {@code STATUS_OK}. 
   */
  //DELA, a brez class path-a
  public static ErrorStatus compileOld(String sourcePath,  String [] sources, File workingDir) {
    int i=0;
    String [] filesToCompile = new String[sources.length];
    for (String filename: sources) {
      File inFile  = new File(sourcePath + File.separator + filename);
      File outFile = new File(workingDir + File.separator + filename);
      try {
	FileUtils.copyFile(inFile, outFile);
	filesToCompile[i++] = outFile.getAbsolutePath();
      } catch (Exception e) {
	return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_COPY, e.toString());
      }
    }

    
    String errors = "";
    
    OutputStream os = new ByteArrayOutputStream();
    JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    
    int error_status = javac.run(null, os, os, filesToCompile);
    
    if (error_status != 0) {
      String error = os.toString().replaceAll(workingDir.getAbsolutePath(), "File: ");
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_COMPILE, error);
    }
    return ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK, "");
  }
  
  /**
   * 
   * @param sourcePath path to source files
   * @param sources a list of sources (with .java extension)
   * @param destPath a path for compiled files
   * @param classpaths a list of paths to bo included in classpath
   * @param msg message substring to be displayed in log file
   * @return 
   */
  public static ErrorStatus compile(String srcPath,  String [] sources, String destPath, 
	  String [] classpaths, String jars, String msg) {
    // a path has to be at least 10 charaters long to prevent errors (i.e deleting root folder)
    if (destPath.length() < 10) 
      return ErrorStatus. ERROR_INVALID_DESTPATH;
    File destPathF = new File(destPath);
    // if folder exists, delete it ...
    if (destPathF.exists())
      destPathF.delete();
    // ... and create a new one
    destPathF.mkdirs();
    
    if (!destPathF.exists())
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_CREATEDIR, destPath);
    
    // build a classpath
    StringBuilder sb = new StringBuilder();
    URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
    for (URL url : urlClassLoader.getURLs())
      sb.append(url.getFile()).append(File.pathSeparator);
    for(String cp: classpaths)
      sb.append(cp).append(File.pathSeparator);
    
    // add a project/algorithm specific jars
    sb.append(File.pathSeparator).append(jars);
    
    ArrayList<File> srcFiles = new ArrayList(); int i=0;
    for(String src : sources)
      srcFiles.add(new File(srcPath + File.separator + src));
    
    List<String> options = new ArrayList<String>();
    options.add("-classpath");  // -classpath <path>      Specify where to find user class files
    options.add(sb.toString());
    options.add("-d");          // -d <directory>         Specify where to place generated class files
    options.add(destPath);
    
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Writer wos = new OutputStreamWriter(os);
    
    JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = javac.getStandardFileManager(null /* diagnosticlistener */, null, null);
    JavaCompiler.CompilationTask task = javac.getTask(wos, fileManager, null /* diagnosticlistener */, options, null, fileManager.getJavaFileObjectsFromFiles(srcFiles));
    
    Boolean compileOK = task.call();
     
    if (!compileOK) {
      String error = os.toString();
      error = error.replaceAll(srcPath + File.separator, "");
      
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_COMPILE, error);
    }
    return ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK, String.format("Compiling %s  - done.", msg));
  }
  
  
    public static ETestSet getFirstTestSetFromProject(String root, String projName) {
    String projRoot     = ATGlobal.getPROJECTroot(root, projName);
    String projFilename = ATGlobal.getPROJECTfilename(root, projName);
    
    EProject   eProject = new EProject(new File(projFilename)); 
    if (ErrorStatus.getLastErrorStatus() != ErrorStatus.STATUS_OK) 
      return null;

    String [] eTestSetNames = eProject.getStringArray(EProject.ID_TestSets);
    if (eTestSetNames.length < 1) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "No testsets defined in a project");
      return null;
    }
    
    String testSetFilename = ATGlobal.getTESTSETfilename(projRoot, eTestSetNames[0]);
    ETestSet testSet = new ETestSet(new File(testSetFilename));
    if (ErrorStatus.getLastErrorStatus() != ErrorStatus.STATUS_OK)
      return null;
    else
      return testSet;
  }
  
  public static void iterateAndPrintTests(AbstractTestSetIterator it) {
    try {
      while(it.hasNext()) {
	it.readNext();
        System.out.println(it.getCurrent());
      }
      it.close();
    } catch (Exception e) {
      ATLog.log(e.toString());
    }
  }
  
  /**
   * Wrapper for resultsAreUpToDate(String, String , String, int) method. 
   */
  public static boolean resultsAreUpToDate(Project project, String algName, String testsetName) {
    if (project==null) return false;
    ETestSet eTestSet = project.getTestSets().get(testsetName);
    
    int instances = eTestSet.getFieldAsInt(ETestSet.ID_N);

    return resultsAreUpToDate(project.getProject().getProjectRootDir(), algName, testsetName, instances);
  }
    
    
  /**
   * Checks for the existance of the result file for given algorithm. If the file does not exist
   * or if testset file is newer than the results file or if it contains less than 
   * expectedNumberOfInstances lines, method returns false, otherwise true.
   */
  public static boolean resultsAreUpToDate(String projRoot, String algName, String testsetName, int expectedNumberOfInstances) {
    String resFilename     = ATGlobal.getRESULTfilename(projRoot, algName, testsetName, MeasurementType.EM);
    String testsetFilename = ATGlobal.getTESTSETfilename(projRoot, testsetName);
    
    File resFile = new File(resFilename);
    if (!resFile.exists()) return false;
    
    File testFile = new File(testsetFilename);
    
    if (FileUtils.isFileNewer(testFile, resFile))
      return false;
    
    try {
      int numberOfInstances = 0;
      Scanner sc = new Scanner(resFile);
      while(sc.hasNextLine()) {
	numberOfInstances++;
	String line = sc.nextLine();
      }
      sc.close();
	
      return (numberOfInstances == expectedNumberOfInstances);
    } catch (Exception e) {
      return false;
    }
  }
  
  
  public static boolean isSourceNewer(String srcDir, String binDir, String [] srcNames) {
    for (String srcName : srcNames) {
      File f1 = new File(srcDir + File.separator + srcName + ".java");
      File f2 = new File(binDir + File.separator + srcName + ".class");
      try {
        if (FileUtils.isFileNewer(f1, f2)) return true; 
      } catch (Exception e) {
	// this happens, for example, if bin folder does not exist
        return true;
      }
    }
    return false;
  }
  
  
  
  /**
   * Returns null if all the sources exists or the name of the missing source
   */
  public static String sourcesExists(String srcDir, String [] srcs) {
    for (String srcName : srcs) {
      File f = new File(srcDir + File.separator + srcName + ".java");
      if (!f.exists()) return f.getAbsolutePath();
    }
    return null;
  }
  
  /**
   * Extracts the path (without the filename) from the file
   */
  public static String extractFilePath(File file) {
    String fileName = file.getAbsolutePath();
    int pos = fileName.lastIndexOf(File.separator);
    return pos != -1 ? fileName.substring(0,pos) : fileName;
  } 
  
  
  
  /**
   * Extracts the prefix of the filename (the name of the file without the path and file extension)
   */
  public static String extractFileNamePrefix(File file) {
    String fileName = file.getAbsolutePath();
    
    // get the name of the file (without the path)
    int pos = fileName.lastIndexOf(File.separator);
    fileName = pos != -1 ? fileName.substring(pos+1) : fileName;
    
    pos = fileName.lastIndexOf(".");
    return (pos != -1) ? fileName.substring(0,pos) : fileName;
  } 

  /**
   * Strips the file extension
   */
  public static String stripFilenameExtension(String fileName) {        
    int pos = fileName.lastIndexOf(".");
    return (pos != -1) ? fileName.substring(0,pos) : fileName;
  } 

  
  /**
   * Returns true is an aray is sorted
   * @param order 1 ... increasing order, -1 ... decreasing order
   * @return 
   */
  public static boolean isArraySorted(int tab[], int order) {
    for (int i = 0; i < tab.length-1; i++) {
      if (order * tab[i] > order * tab[i+1]) return false;
    }
    return true;
  }
  
  /**
   * Returns a string representation of an array (first 10 elements)
   */
  public static <E> String arrarToString(E [] array) {
    if (array == null) return "null";
    
    int i;
    String result = "";
    for (i = 0; i < Math.min(10, array.length); i++) {
      if (i>0) result+=",";
      result += array[i].toString();
    }
    if (i<array.length)
      result += ", ... (" + array.length + " elements)";
    return "[" + result + "]";
  }
  
  public static String intArrayToString(int [] array) {
    if (array == null) return "null"; 
	    
    Integer [] tab = new Integer[array.length];
    for(int i=0; i<tab.length; i++)
      tab[i] = array[i];
    return arrarToString(tab);
  }   
  
  
  /**
   * Builds a list of JARs in the following format: jar_0:jar_1:..., where
   * jar_i is i-th element of jars prepended with tha path
   *  
   */
  public static String buildJARList(String [] jars, String path) {
    String result = "";
    for (String jar : jars) {
      result += (result.isEmpty() ? "" : File.pathSeparator) + path + File.separator + jar;
    }
    return result;
  }
  
  public static URL[] getURLsFromJARs(String [] jars, String path) {
    try {
      URL [] urls = new URL[jars.length]; int i=0;
      for (String jar : jars) {
        urls[i++] = new File(path + File.separator + jar).toURI().toURL();
      }
      return urls;
    } catch (Exception e) {
      return new URL[0];
    }
  }
}
