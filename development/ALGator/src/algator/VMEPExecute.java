package algator;

import jamvm.vmep.InstructionMonitor;
import jamvm.vmep.Opcode;
import java.io.File;
import java.io.FileWriter;
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
import si.fri.algotest.execute.Notificator;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.tools.ATTools;

/**
 * VMEPExecute class is used to execute algorithm with jamvm virtual machine. It 
 * executes <code>algorithm</code> on a <code>testset</code> and writes results
 * into <code>Algorithm-Testset.jvm</code> file in <code>commPath</code>. 
 * During the execution of tests, notificator writes bytes to communicaiton 
 * file (on byte for each test). This file can be used by invoker of VMEPExecute
 * to prevent halting.
 * 
 * Method main() executes algorithm and exites with exit code
 *   0 ... if execution was successful
 *   1 ... problems with jamvm (java.lang.UnsatisfiedLinkError, ...)
 *   2 ... invalid project
 *   3 ... invalid algorithm
 *   4 ... invalid testset
 *   5 ... invalid test number
 *   6 ... problems with testset iterator
 *   7 ... result description file does not exist
 * 
 * @author tomaz
 */
public class VMEPExecute {

  private static String introMsg = "ALGator VMEP Executor, " + Version.getVersion();
  

  private static Options getOptions() {
    Options options = new Options();

    Option data_root = OptionBuilder.withArgName("data_root_folder")
	    .withLongOpt("data_root")
	    .hasArg(true)
	    .withDescription("use this folder as data_root; default value in $ALGATOR_DATA_ROOT")
	    .create("d");

    options.addOption(data_root);
    
    options.addOption("h", "help", false,
	    "print this message");    
        
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
    
    System.exit(0);
  }
    
  public static void runTest(String dataRoot, String projName, String algName, 
          String testsetName, int testNumber, String commFolder, int verboseLevel) {

    // Test the project
    Project projekt = new Project(dataRoot, projName);
    if (!projekt.getErrors().get(0).equals(ErrorStatus.STATUS_OK)) {
      if (verboseLevel > 0)
        System.out.println("Invalid project name.");
      System.exit(2); // invalid project
    }

    // Test algorithms
    if (algName == null || algName.isEmpty()) {
      if (verboseLevel > 0)
        System.out.println("Invalid algorithm name.");
      System.exit(3);
    }
    EAlgorithm alg = projekt.getAlgorithms().get(algName);
    if (alg == null) {
      if (verboseLevel > 0)
        System.out.println("Invalid algorithm name.");

      System.exit(3); // invalid algorithm
    }

    // Test testsets
    if (testsetName == null || testsetName.isEmpty()) {
      if (verboseLevel > 0)
        System.out.println("Invalid testset name.");
      System.exit(4);
    }
    
    ETestSet testSet = projekt.getTestSets().get(testsetName);
    if (testSet == null) {
      if (verboseLevel > 0)
        System.out.println("Invalid testset name.");

      System.exit(4); // invalid testset
    }    
            
    AbstractTestSetIterator testsetIterator = New.testsetIteratorInstance(projekt, algName);
    if (testsetIterator == null) {
      if (verboseLevel > 0)
        System.out.println("Can not create testset iterator.");
      System.exit(5); // testset iterator can not be created
    }
    testsetIterator.setTestSet(testSet);
    
    // Test testNumber
    int allTests = testsetIterator.getNumberOfTestInstances();
    if (testNumber > allTests) {
      if (verboseLevel > 0)
        System.out.println("Invalid test number.");

      System.exit(6); // invalid testset      
    }


    EResultDescription resultDescription = projekt.getResultDescriptions().get(MeasurementType.JVM);
    if (resultDescription == null) {
      if (verboseLevel > 0)
        System.out.println("JVM result description file does not exist.");
      System.exit(7); // JVM result descritpion does not exist
    }

    String resFilename = ATGlobal.getRESULTfilename(".", algName, testsetName, MeasurementType.JVM);
    resFilename = ATTools.extractFileNamePrefix(new File(resFilename)) + "-" + testNumber + ".jvm";
    resFilename = commFolder + File.separator + resFilename;       // ... and add commFolder as a path
    
    runAlgorithmOnTest(projekt, algName, testsetName, testNumber, resultDescription, testsetIterator, verboseLevel, resFilename);
  }

  

  /**
   * 
   */
  public static void runAlgorithmOnTest(
    Project project, String algName, String testsetName, int testNumber, EResultDescription resultDesc,
          AbstractTestSetIterator testsetIterator, int verboseLevel, String resFilename) {
        
    
    // the order of parameters to be printed
    String[] order = resultDesc.getParamsOrder();
    String delim   = resultDesc.getField(EResultDescription.ID_Delim);

    ParameterSet result = new ParameterSet();
    
    if (verboseLevel == 2)
      System.out.println("Running test...");
    
    try {
      // delete the content of the output file
      new FileWriter(resFilename).close();

      if (testsetIterator.readTest(testNumber)) {

       if (verboseLevel == 2)
        System.out.println("Getting current...");
        
        
        TestCase testCase = testsetIterator.getCurrent();    
        AbsAlgorithm curAlg = New.algorithmInstance(project, algName, MeasurementType.JVM);
        curAlg.init(testCase); 
        
        if (verboseLevel == 2) {
          System.out.println("Test " + testNumber);
          System.out.println("Before execution: ");
          System.out.println(testCase);
        }
        
        InstructionMonitor instrMonitor = new InstructionMonitor();
        instrMonitor.start();                    
        curAlg.run();          
        instrMonitor.stop();

        result = curAlg.done();
        
        if (verboseLevel == 2) {
          System.out.println("After execution: ");
          System.out.println(testCase);
        }
                    
        // write results to the result set.
        ParameterSet pSet = resultDesc.getParameters();
        int[] instFreq=instrMonitor.getCounts();
        for(int i=0;i<instFreq.length;i++){
          String pName = Opcode.getNameFor(i);
          if (pSet.getParamater(pName) != null) {
            result.addParameter(new EParameter(pName, "", ParameterType.INT, instFreq[i]), true);
          }
          if (verboseLevel == 2 && instFreq[i]!=0)
            System.out.println(pName + " ");
          
          if (verboseLevel == 2)
            System.out.println("");
        }  
        result.addParameter(EResultDescription.getPassParameter(true), true);
        result.addParameter(EResultDescription.getTestIDParameter("test"+testNumber), true);
        result.addParameter(EResultDescription.getAlgorithmNameParameter(algName), true);
        result.addParameter(EResultDescription.getTestsetNameParameter(testsetName), true);

        PrintWriter pw = new PrintWriter(new FileWriter(resFilename, true));                  
          pw.println(result.toString(order, false, delim));
        pw.close();          
      }
      
    } catch (Exception e) {
      result.addParameter(EResultDescription.getPassParameter(false), true);

      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_RUN, e.toString());
      if (verboseLevel == 2)
        System.out.println(e);
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

      String dataRoot = System.getenv("ALGATOR_DATA_ROOT");
      if (line.hasOption("data_root")) {
	dataRoot = line.getOptionValue("data_root");
      }
      ATGlobal.ALGatorDataRoot = dataRoot;
      
      
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
