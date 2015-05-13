package algator;

import si.fri.algotest.execute.*;
import java.net.URL;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import si.fri.algotest.timer.Timer;

import java.io.File;
import si.fri.algotest.entities.EConfig;
import si.fri.algotest.entities.MeasurementType;

/**
 * This class is used to execute a given algorithm. The main method in this class is the run() method, which
 * reads the algorithm (and input parameters that are included in the algortihm's testcase) and runs this 
 * algorithm n times (where n is one of the parameters in the testcase).
 * This class is used to execute algorithm in a separate JVM. The typical usage of the run method is as follows:
 * 1) write algorithm to a file in a tmpFolder
 * 2) execute "java algator.ExternExecute tmpFolder"
 * 3) during the execution of this JVM check for time limits; if this limits are exceeded, kill the JVM and finish,
 *    otherwise collect the results and finish
 * 
 * @author tomaz
 */
public class ExternalExecute {
  
  private static String introMsg = "ALGator ExternalExecute, " + Version.getVersion();
  
  
  private static Options getOptions() {
    Options options = new Options();
    
    options.addOption("v", "verbose", false, "print additional information");
    
    return options;
  }
  
  private static void printMsg(Options options) {
    System.out.println(introMsg + "\n");
    
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("algator.ExternalExecut [options] <path>", options);

    System.exit(0);
  }
  
  
  /**
   * Executes a given algorithmm on a given testcase several times. For each execution
   * the clean copy of algorithm instance (which includes the testcase instance)
   * is loaded from file. The execution times are stored in local array and are
   * written to the final algorithm's array at the end of all executions. 
   * The final version of algorithm instance (which includes the result parameters
   * in the testCase and timer parameters in the timer array) is written to file.
   */
  public static void run(String tmpFolderName, boolean verbose) {
    AbsAlgorithm curAlg = ExternalExecutor.getAlgorithmFromFile(tmpFolderName, ExternalExecutor.SER_ALG_TYPE.TEST);
    if (curAlg == null) return;
    
    // urls that are used to load an algorithm
    URL [] urls = ExternalExecutor.getURLsFromFile(tmpFolderName, ExternalExecutor.SER_ALG_TYPE.TEST);
    
    // run the test timesToExecute-times and save time to the times[] array
    long[][] times     = curAlg.getExecutionTimes();
    int timesToExecute = curAlg.getTimesToExecute();
    
    // clear the contents of the communication file
    ExternalExecutor.initCommunicationFile(tmpFolderName);
    
    for (int i = 0; curAlg!=null && i < timesToExecute; i++) {

      if (verbose) 
        System.out.printf("%5d ", i);
      
      Counters.resetCounters();
      curAlg.timer = new Timer();

      curAlg.timer.start();
      curAlg.run();
      curAlg.timer.stop();
      
      // adds one byte to the communication file to signal a succesfull execution
      ExternalExecutor.addToCommunicationFile(tmpFolderName);

      for (int tID = 0; tID < Timer.MAX_TIMERS; tID++) {
        times[tID][i] = curAlg.timer.time(tID);
      }
  
      // read the clean version of the algorithm from the file for the next execution
      if (i < timesToExecute - 1)
        curAlg = ExternalExecutor.getAlgorithmFromFile(tmpFolderName, ExternalExecutor.SER_ALG_TYPE.TEST);
    }
    
    if (verbose) 
      System.out.println("");
    
    if (curAlg != null) {
      // save execution times ...
      for (int i = 0; i < timesToExecute; i++) {
        for (int tID = 0; tID < Timer.MAX_TIMERS; tID++)  {
          curAlg.setExectuionTime(tID, i, times[tID][i]);
          
          if (verbose && tID==0)
            System.out.printf("%5d", times[tID][i]);
        }
      }
      
      // ... and counters
      curAlg.setCounters(Counters.getCounters());
      
      System.out.println("");
      ExternalExecutor.saveAlgToFile(urls, curAlg, tmpFolderName, ExternalExecutor.SER_ALG_TYPE.RESULT);
    }
  }
  
  /**
   * Runs the executor using external JVM. If execution is succesfull, the created process is returned
   * else the errorMessage is returned.
   * @param folderName
   * @param verbose
   * @return 
   */
  public static Object runWithExternalJVM(String folderName, MeasurementType mType, boolean verbose) {    
    try {
      ///* For real-time execution (classPath=..../ALGator.jar)
      String classPath = Version.getClassesLocation();
      //*/
    
      //*   In debug mode (running ALGator from NetBeans) getClassLocation() returns
         // a path to "classes" folder which is not enough to execute ALGator.
         // To run ALGator in debug mode, we add local ALGator distribution
      if (!classPath.contains("ALGator.jar"))
        classPath += File.pathSeparator +  "dist/ALGator.jar";
      //*/
    
      String jvmCommand = "java";
      if (mType.equals(MeasurementType.JVM)) {
        String vmepCmd = EConfig.getConfig().getField(EConfig.ID_VMEP);
        String vmepCP  = EConfig.getConfig().getField(EConfig.ID_VMEPClasspath);
        if (!vmepCmd.isEmpty()) 
          jvmCommand = vmepCmd;
        if (!vmepCP.isEmpty())
          classPath += File.pathSeparator + vmepCP;
      }
    
      String[] command = {jvmCommand, "-cp", classPath, "-Xss1024k", "algator.ExternalExecute", folderName};
      ProcessBuilder probuilder = new ProcessBuilder( command );
    
      return probuilder.start();      
    } catch (Exception e) {
      return e.toString();
    }
  }
  
  public static void main(String[] args) {       
    Options options = getOptions();

    String path = "";
    boolean verbose = false;
    
    CommandLineParser parser = new BasicParser();
    try {
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("v")) {
	verbose = true;
      }
      
      String[] curArgs = line.getArgs();
      if (curArgs.length != 1) {
	printMsg(options);
      }
      path = curArgs[0];
      
    } catch (Exception ex) {
      printMsg(options);
    }
    
    run(path, verbose);
  }
  
}
