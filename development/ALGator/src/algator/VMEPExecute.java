package algator;

import jamvm.vmep.InstructionMonitor;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
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
 *   5 ... problems with testset iterator
 *   6 ... result description file does not exist
 * 
 * @author tomaz
 */
public class VMEPExecute {

  private static String introMsg = "ALGator VMEP Executor, " + Version.getVersion();
  

  /**
   * This notificator notifies to stdout and to communication file
   */
  private static Notificator getNotificator(final String alg, final String testSet, 
          final String comFolder, final boolean verbose) {
    return new Notificator() {
      
      { // anonimous constructor
        ExternalExecutor.initCommunicationFile(comFolder);
      }
      
      public void notify(int i) {
        if (verbose)
          System.out.println(String.format("[%s, %s]: test %d out of %d done.", alg, testSet, i, this.getN()));
        
        ExternalExecutor.addToCommunicationFile(comFolder);
      }
    };
  }


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
    options.addOption("v", "verbose", false, "print additional information on error");
    options.addOption("u", "usage", false, "print usage guide");
    
    return options;
  }

  private static void printMsg(Options options) {
    System.out.println(introMsg + "\n");
    
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("algator.VMEPExecute [options] project_name algorithm_name testset_name comm_folder", options);

    System.exit(0);
  }

  private static void printUsage() {
    System.out.println(introMsg + "\n");
    
    Scanner sc = new Scanner((new Analyse()).getClass().getResourceAsStream("/data/VMEPExecutorUsage.txt")); 
    while (sc.hasNextLine())
      System.out.println(sc.nextLine());
    
    System.exit(0);
  }
    
  public static void runTestset(String dataRoot, String projName, String algName, 
          String testsetName, String commFolder, boolean verbose) {

    // Test the project
    Project projekt = new Project(dataRoot, projName);
    if (!projekt.getErrors().get(0).equals(ErrorStatus.STATUS_OK)) {
      if (verbose)
        System.out.println("Invalid project name.");
      System.exit(2); // invalid project
    }

    // Test algorithms
    if (algName == null || algName.isEmpty()) {
      if (verbose)
        System.out.println("Invalid algorithm name.");
      System.exit(3);
    }
    EAlgorithm alg = projekt.getAlgorithms().get(algName);
    if (alg == null) {
      if (verbose)
        System.out.println("Invalid algorithm name.");

      System.exit(3); // invalid algorithm
    }

    // Test testsets
    if (testsetName == null || testsetName.isEmpty()) {
      if (verbose)
        System.out.println("Invalid testset name.");
      System.exit(4);
    }
    ETestSet testSet = projekt.getTestSets().get(testsetName);
    if (testSet == null) {
      if (verbose)
        System.out.println("Invalid testset name.");

      System.exit(4); // invalid testset
    }
        
    AbstractTestSetIterator testsetIterator = New.testsetIteratorInstance(projekt, algName);
    if (testsetIterator == null) {
      if (verbose)
        System.out.println("Can not create testset iterator.");
      System.exit(5); // testset iterator can not be created
    }
    testsetIterator.setTestSet(testSet);

    EResultDescription resultDescription = projekt.getResultDescriptions().get(MeasurementType.JVM);
    if (resultDescription == null) {
      if (verbose)
        System.out.println("JVM result description file does not exist.");
      System.exit(6); // JVM result descritpion does not exist
    }

    String resFilename = ATGlobal.getRESULTfilename(".", algName, testsetName, MeasurementType.JVM);
    resFilename = ATTools.extractFileNamePrefix(new File(resFilename)) + ".jvm";
    resFilename = commFolder + File.separator + resFilename;       // ... and add commFolder as a path
    
    Notificator notificator = getNotificator(algName, testsetName, commFolder, verbose);
    notificator.setNumberOfInstances(testsetIterator.getNumberOfTestInstances());
    
    iterateTestSetAndRunAlgorithm(projekt, algName, testsetName, resultDescription, testsetIterator, notificator, resFilename);
  }

  

  /**
   * 
   */
  public static void iterateTestSetAndRunAlgorithm(
    Project project, String algName, String testsetName, EResultDescription resultDesc,
          AbstractTestSetIterator testsetIterator, Notificator notificator, String resFilename) {
    
    
    // the order of parameters to be printed
    String[] order = resultDesc.getParamsOrder();
    String delim   = resultDesc.getField(EResultDescription.ID_Delim);
    
    int tsID = 0;
    try {
      // delete the content of an output file
      new FileWriter(resFilename).close();
      
      while (testsetIterator.hasNext()) {
        testsetIterator.readNext();

        notificator.notify(++tsID);

        TestCase testCase = testsetIterator.getCurrent();
        ParameterSet result;
        try {
          AbsAlgorithm curAlg = New.algorithmInstance(project, algName, MeasurementType.JVM);
          curAlg.init(testCase); 
        
          InstructionMonitor instrMonitor = new InstructionMonitor();
          
          long time = System.currentTimeMillis();
          
          curAlg.run();
          time = System.currentTimeMillis() - time;
          
          instrMonitor.start();
          result = curAlg.done();
          instrMonitor.stop();
          
          result.addParameter(new EParameter("Tmin", "", ParameterType.INT, time), true);

          // TODO: dodaj JVM PARAMETRE v result
          
          result.addParameter(EResultDescription.getPassParameter(true), true);
          
        } catch (Exception e) {
          result = new ParameterSet();
          result.addParameter(EResultDescription.getPassParameter(false), true);
        }
        result.addParameter(EResultDescription.getAlgorithmNameParameter(algName), true);
        result.addParameter(EResultDescription.getTestsetNameParameter(testsetName), true);

        PrintWriter pw = new PrintWriter(new FileWriter(resFilename, true));                  
          pw.println(result.toString(order, false, delim));
        pw.close();          
      }
      testsetIterator.close();
      
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_RUN, e.toString());
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
      if (curArgs.length != 4) {
	printMsg(options);
      }

      String projectName   = curArgs[0];
      String algorithmName = curArgs[1];
      String testsetName   = curArgs[2];
      String commFolder    = curArgs[3];

      String dataRoot = System.getenv("ALGATOR_DATA_ROOT");
      if (line.hasOption("data_root")) {
	dataRoot = line.getOptionValue("data_root");
      }
      ATGlobal.ALGatorDataRoot = dataRoot;
      
      boolean verbose = line.hasOption("verbose");
      ATLog.setLogLevel(ATLog.LOG_LEVEL_OFF);
      if (verbose) {
        ATLog.setLogLevel(ATLog.LOG_LEVEL_STDOUT);
      }

      if (verbose)
        System.out.println(introMsg + "\n");
      
      runTestset(dataRoot, projectName, algorithmName, testsetName, commFolder, verbose);

    } catch (ParseException ex) {
      printMsg(options);
    }
  }


}
