package algator;

import jamvm.vmep.InstructionMonitor;
import jamvm.vmep.Opcode;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import si.fri.algotest.entities.EAlgorithm;
import si.fri.algotest.entities.EConfig;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.Project;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.AbsAlgorithm;
import si.fri.algotest.execute.AbstractTestSetIterator;
import si.fri.algotest.execute.ExternalExecutor;
import si.fri.algotest.execute.New;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.global.ExecutionStatus;
import si.fri.algotest.global.VMEPErrorStatus;

/**
 * VMEPExecute class is used to execute algorithm with vmep virtual machine. It 
 * executes <code>algorithm</code> on a <code>testset</code> and writes results
 * into <code>Algorithm-Testset.jvm</code> file in <code>commPath</code>. 
 * During the execution of tests, notificator writes bytes to communicaiton 
 * file (on byte for each test). This file can be used by invoker of VMEPExecute
 * to prevent halting.
 * 
 * Method main() executes algorithm and exites with exit code VMEPErrorStatus

 * @author tomaz
 */
public class VMEPExecute {

  private static String introMsg = "ALGator VMEP Executor, " + Version.getVersion();
  

  private static Options getOptions() {
    Options options = new Options();

    Option data_root = OptionBuilder.withArgName("data_root_folder")
	    .withLongOpt("data_root")
	    .hasArg(true)
	    .withDescription("use this folder as data_root; default value in $ALGATOR_DATA_ROOT" )
	    .create("d");

    Option algator_root = OptionBuilder.withArgName("algator_root_folder")
            .withLongOpt("algator_root")
            .hasArg(true)
            .withDescription("use this folder as algator_root; default value in $ALGATOR_ROOT")
            .create("r");

    options.addOption(data_root);
    options.addOption(algator_root);
    
    options.addOption("h", "help", false,
	    "print this message");    
        
    options.addOption("v0", "silent",  false, "no information on error");
    options.addOption("v1", "print",   false, "print   information on error");
    options.addOption("v2", "verbose", false, "verbose information on error");
    options.addOption("u", "usage",    false, "print usage guide");
    
    return options;
  }

  private static void printMsg(Options options) {
    System.out.println(introMsg + "\n");
    
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("algator.VMEPExecute [options] project_name algorithm_name testset_name test_number comm_folder", options);

    System.exit(0);
  }

  private static void printUsage() {
    System.out.println(introMsg + "\n");
    
    Scanner sc = new Scanner((new Analyse()).getClass().getResourceAsStream("/data/VMEPExecutorUsage.txt")); 
    while (sc.hasNextLine())
      System.out.println(sc.nextLine());
    
    System.exit(VMEPErrorStatus.OK.getValue());
  }
    
  public static void runTest(String dataRoot, String projName, String algName, 
          String testsetName, int testNumber, String commFolder, int verboseLevel) {

    // Test the project
    Project projekt = new Project(dataRoot, projName);
    if (!projekt.getErrors().get(0).equals(ErrorStatus.STATUS_OK)) {
      if (verboseLevel > 0)
        System.out.println("Invalid project name.");
      System.exit(VMEPErrorStatus.INVALID_PROJECT.getValue()); // invalid project
    }

    // Test algorithms
    if (algName == null || algName.isEmpty()) {
      if (verboseLevel > 0)
        System.out.println("Invalid algorithm name.");
      System.exit(VMEPErrorStatus.INVALID_ALGORITHM.getValue());
    }
    EAlgorithm alg = projekt.getAlgorithms().get(algName);
    if (alg == null) {
      if (verboseLevel > 0)
        System.out.println("Invalid algorithm name.");

      System.exit(VMEPErrorStatus.INVALID_ALGORITHM.getValue()); // invalid algorithm
    }

    // Test testsets
    if (testsetName == null || testsetName.isEmpty()) {
      if (verboseLevel > 0)
        System.out.println("Invalid testset name.");
      System.exit(VMEPErrorStatus.INVALID_TESTSET.getValue());
    }
    
    ETestSet testSet = projekt.getTestSets().get(testsetName);
    if (testSet == null) {
      if (verboseLevel > 0)
        System.out.println("Invalid testset name.");

      System.exit(VMEPErrorStatus.INVALID_TESTSET.getValue()); // invalid testset
    }    
            
    AbstractTestSetIterator testsetIterator = New.testsetIteratorInstance(projekt, algName);
    if (testsetIterator != null) 
      testsetIterator.setTestSet(testSet);    
    if (testsetIterator == null || !ErrorStatus.getLastErrorStatus().equals(ErrorStatus.STATUS_OK)) {
      if (verboseLevel > 0)
        System.out.println("Can not create testset iterator.");
      System.exit(VMEPErrorStatus.INVALID_ITERATOR.getValue()); // testset iterator can not be created
    }
    
    EResultDescription resultDescription = projekt.getResultDescriptions().get(MeasurementType.JVM);
    if (resultDescription == null) {
      if (verboseLevel > 0)
        System.out.println("JVM result description file does not exist.");
      System.exit(VMEPErrorStatus.INVALID_RESULTDESCRIPTION.getValue()); // JVM result descritpion does not exist
    }

    
    
    // Test testNumber
    int allTests = testsetIterator.getNumberOfTestInstances();
    if (testNumber > allTests) {
      if (verboseLevel > 0)
        System.out.println("Invalid test number.");

      System.exit(VMEPErrorStatus.INVALID_TEST.getValue()); // invalid testset   
    }
    
    String resFilename = ATGlobal.getJVMRESULTfilename(commFolder, algName, testsetName, testNumber);
    
    runAlgorithmOnATest(projekt, algName, testsetName, testNumber, resultDescription, testsetIterator, verboseLevel, resFilename);
  }

  

  /**
   * 
   */
  public static void runAlgorithmOnATest(
    Project project, String algName, String testsetName, int testNumber, EResultDescription resultDesc,
          AbstractTestSetIterator testsetIterator, int verboseLevel, String resFilename) {
        
    
    // the order of parameters to be printed
    String[] order = resultDesc.getParamsOrder();
    String delim   = resultDesc.getField(EResultDescription.ID_Delim);

    ParameterSet result = new ParameterSet();
    result.addParameter(EResultDescription.getAlgorithmNameParameter(algName), true);
    result.addParameter(EResultDescription.getTestsetNameParameter(testsetName), true);
    result.addParameter(EResultDescription.getTestIDParameter("Test"+testNumber), true); // if testCase won't initialize, a testcase ID is giver here 
    result.addParameter(EResultDescription.getExecutionStatusParameter(ExecutionStatus.UNKNOWN), true);
    
    // An error that appears as a result of JVM error is not caught by the following catch; however, the finally block
    // is always executed. If in finally block success is false, then an JVM error has occured and must be reported
    boolean success = false;
    try {
      // delete the content of the output file
      new FileWriter(resFilename).close();

      if (testsetIterator.readTest(testNumber)) {        
        
        TestCase testCase = testsetIterator.getCurrent();    
        AbsAlgorithm curAlg = New.algorithmInstance(project, algName, MeasurementType.JVM);
        curAlg.init(testCase); 
        
        // add test ID (if execution fails, result should contain correct testID parameter)
        result.addParameter(testCase.getParameters().getParamater(EResultDescription.testIDParName), true);
           
        if (verboseLevel == 2) {
          System.out.printf("Project: %s, Algorithm: %s, TestSet: %s, Test: %d\n", project.projectName, algName, testsetName, testNumber);
          System.out.println("********* Before execution       *********************************************");
          System.out.println(testCase);
        }
        
        InstructionMonitor instrMonitor = new InstructionMonitor();
        instrMonitor.start();                    
        curAlg.run();          
        instrMonitor.stop();

        result = curAlg.done();
        
        if (verboseLevel == 2) {
          System.out.println("********* After execution        *********************************************");
          System.out.println(testCase);
        }

        if (verboseLevel == 2) 
          System.out.println("********* Bytecode commands used *********************************************");
                
        // write results to the result set.
        ParameterSet pSet = resultDesc.getParameters();
        int[] instFreq=instrMonitor.getCounts();
        for(int i=0;i<instFreq.length;i++){
          String pName = Opcode.getNameFor(i);
          if (pSet.getParamater(pName) != null) {
            result.addParameter(new EParameter(pName, "", ParameterType.INT, instFreq[i]), true);
          }
          if (verboseLevel == 2 && instFreq[i]!=0)
            System.out.print(pName + " ");
        }  
        if (verboseLevel == 2)
            System.out.println("");
        
        
        result.addParameter(EResultDescription.getExecutionStatusParameter(ExecutionStatus.DONE), true);
        result.addParameter(EResultDescription.getAlgorithmNameParameter(algName), true);
        result.addParameter(EResultDescription.getTestsetNameParameter(testsetName), true);
      } else {
        result.addParameter(EResultDescription.getExecutionStatusParameter(ExecutionStatus.FAILED), true);
        result.addParameter(EResultDescription.getErrorParameter("Invaldi testset or test."), true);
      }
      success = true;
      
    } catch (IOException e) {
      result.addParameter(EResultDescription.getExecutionStatusParameter(ExecutionStatus.FAILED), true);
      result.addParameter(EResultDescription.getErrorParameter(e.toString()), true);

      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_RUN, e.toString());

      if (verboseLevel == 1)
        System.out.println(e);
      
    } finally {
      if (!success) {
         result.addParameter(EResultDescription.getErrorParameter("Unknown JVM error"), true);
      }
      try (PrintWriter pw = new PrintWriter(new FileWriter(resFilename, true))) {                  
          pw.println(result.toString(order, false, delim));
      } catch (IOException e) {
        if (verboseLevel == 1)
          System.out.println(e);
      }
    }
  }
  
  /**
   * Runs the algorithm using vmep virtual machine. If execution is succesfull, the 
   * created process is returned else the error message is returned as a String
   */
  public static Object runWithVMEP(String project_name, String alg_name, String testset_name,
          int testID, String commFolder, String data_root, boolean verbose) {    
    try {
      ///* For real-time execution (classPath=..../ALGator.jar)
      String classPath = Version.getClassesLocation();
      //*/
    
      //*   In debug mode (when running ALGator with NetBeans) getClassLocation() returns
         // a path to "classes" folder which is not enough to execute ALGator.
         // To run ALGator in debug mode, we add local ALGator distribution
      if (!classPath.contains("ALGator.jar"))
        classPath += File.pathSeparator +  "dist/ALGator.jar";
      //*/
      
      String jvmCommand = "java";
      String vmepCmd = EConfig.getConfig().getField(EConfig.ID_VMEP);
      String vmepCP  = EConfig.getConfig().getField(EConfig.ID_VMEPClasspath);
      if (!vmepCmd.isEmpty()) 
        jvmCommand = vmepCmd;
      if (!vmepCP.isEmpty())
          classPath += File.pathSeparator + vmepCP;
            
      String[] command = {jvmCommand, "-cp", classPath, "-Xss1024k", "algator.VMEPExecute", 
        project_name, alg_name, testset_name, Integer.toString(testID), commFolder, "-d", data_root, verbose ? "-v2" : "-v0"};
      
      
      ProcessBuilder probuilder = new ProcessBuilder( command );
    
      return probuilder.start();      
    } catch (Exception e) {
      return e.toString();
    }
  }
  
  /**
   * Used to run the system. Parameters are given trought the arguments
   *
   * @param args
   */
  public static void main(String args[]) {
    Options options = getOptions();

    CommandLineParser parser = new BasicParser();
    try {
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("h")) {
	printMsg(options);
      }

      if (line.hasOption("u")) {
        printUsage();
      }

      String[] curArgs = line.getArgs();
      if (curArgs.length != 5) {
	printMsg(options);
      }

      String projectName   = curArgs[0];
      String algorithmName = curArgs[1];
      String testsetName   = curArgs[2];
      String testNumberS   = curArgs[3];
      String commFolder    = curArgs[4];

      String algatorRoot = System.getenv("ALGATOR_ROOT");
      if (line.hasOption("algator_root")) {
        algatorRoot = line.getOptionValue("algator_root");        
      }
      ATGlobal.setALGatorRoot(algatorRoot);
      
      String dataRoot = System.getenv("ALGATOR_DATA_ROOT");
      if (line.hasOption("data_root")) {
	dataRoot = line.getOptionValue("data_root");
      }
      ATGlobal.setALGatorDataRoot(dataRoot);      
      
      int testNumber = 1; // the first test in testset is the default test to run
      try {
        testNumber = Integer.parseInt(testNumberS);
      } catch (Exception e) {}
      
      int verboseLevel = 0;
      if (line.hasOption("print")) verboseLevel = 1;
      if (line.hasOption("verbose")) verboseLevel = 2;

      ATLog.setLogLevel(ATLog.LOG_LEVEL_OFF);
      if (verboseLevel > 0) {
        ATLog.setLogLevel(ATLog.LOG_LEVEL_STDOUT);
      }

      if (verboseLevel > 0)
        System.out.println(introMsg + "\n");
      
      // Notify to the caller (message: JVM has started) 
      ExternalExecutor.initCommunicationFile(commFolder);
      ExternalExecutor.addToCommunicationFile(commFolder);
      
      
      runTest(dataRoot, projectName, algorithmName, testsetName, testNumber, commFolder, verboseLevel);

    } catch (ParseException ex) {
      printMsg(options);
    }
  }


}
