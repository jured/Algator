package algator;

import java.awt.Toolkit;
import java.io.File;
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
import si.fri.adeserver.ADEGlobal;
import si.fri.adeserver.ADETools;
import si.fri.adeserver.TaskStatus;
import si.fri.algotest.entities.EAlgorithm;
import si.fri.algotest.entities.ELocalConfig;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.Project;
import si.fri.algotest.execute.Executor;
import si.fri.algotest.execute.Notificator;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.tools.ATTools;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.global.ExecutionStatus;

/**
 *
 * @author tomaz
 */
public class Execute {

  private static String introMsg = "ALGator Execute, " + Version.getVersion();
  

  private static Options getOptions() {
    Options options = new Options();

    Option algorithm = OptionBuilder.withArgName("algorithm_name")
	    .withLongOpt("algorithm")
	    .hasArg(true)
	    .withDescription("the name of the algorithm to run; if the algorithm is not given, all the algorithms of a given project are run")
	    .create("a");

    Option testset = OptionBuilder.withArgName("testset_name")
	    .withLongOpt("testset")
	    .hasArg(true)
	    .withDescription("the name of the testset to use; if the testset is not given, all the testsets of a given project are used")
	    .create("t");

    Option measurement = OptionBuilder.withArgName("mtype_name")
	    .withLongOpt("mtype")
	    .hasArg(true)
	    .withDescription("the name of the measurement type to use (EM, CNT or JVM); if the measurement type is not given, the EM measurement type is used")
	    .create("m");
    

    Option data_root = OptionBuilder.withArgName("data_root_folder")
	    .withLongOpt("data_root")
	    .hasArg(true)
	    .withDescription("use this folder as data_root; default value in $ALGATOR_DATA_ROOT (if defined) or $ALGATOR_ROOT/data_root")
	    .create("d");
    
    Option algator_root = OptionBuilder.withArgName("algator_root_folder")
            .withLongOpt("algator_root")
            .hasArg(true)
            .withDescription("use this folder as algator_root; default value in $ALGATOR_ROOT")
            .create("r");
    
    Option taskID = OptionBuilder.withArgName("task_id")
            .withLongOpt("id")
            .hasArg(true)
            .withDescription("task identification number")
            .create("i");
    



    options.addOption(algorithm);
    options.addOption(testset);
    options.addOption(data_root);
    options.addOption(algator_root);
    options.addOption(measurement);
    options.addOption(taskID);
    
    
    options.addOption("h", "help", false,
	    "print this message");
    options.addOption("c", "compile", false,
	    "compile all classes; if this option is omitted, only outdated classes will be compiled");
    options.addOption("e", "exec", false,
	    "execute test(s) without checking; if this option is omitted, only outdated tests will be executed");
    options.addOption("l", "list_jobs", false,
	    "list the jobs (i.e. the pairs (algorithm, testset)) that are to be executed");
    
    options.addOption("v", "verbose", false, "print additional information on error");
    
    options.addOption("u", "usage", false, "print usage guide");
    
    return options;
  }

  private static void printMsg(Options options) {
    System.out.println(introMsg + "\n");
    
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("algator.Execute [options] project_name", options);

    System.exit(0);
  }

  private static void printUsage() {
    System.out.println(introMsg + "\n");

    Scanner sc = new Scanner((new Analyse()).getClass().getResourceAsStream("/data/ExecutorUsage.txt")); 
    while (sc.hasNextLine())
      System.out.println(sc.nextLine());
    
    System.exit(0);
  }

  private static Notificator getNotificator(final String alg, final String testSet, final MeasurementType mt, final int tID) {
    String idtFilename = ADEGlobal.getTaskStatusFilename(tID);
    
    Notificator notificator = 
      new Notificator() {
      
      public void notify(int i, ExecutionStatus status) {
        System.out.println(String.format("[%s, %s, %s]: test %3d / %-3d - %s", 
          alg, testSet, mt.getExtension(), i, this.getN(),status.toString()));
        
        String statusMsg = String.format("%d%c (%d/%d)", 100*i/this.getN(), '%', i, getN());
        ADETools.setTaskStatus(taskID,  TaskStatus.RUNNING, statusMsg, null);
      }
    };
    notificator.setTaskID(tID);
    return notificator;
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
      if (curArgs.length != 1) {
	printMsg(options);
      }

      String projectName = curArgs[0];

      String algorithmName = "";
      String testsetName = "";

      boolean alwaysCompile = false;
      boolean alwaysRunTests = false;

      boolean listOnly = false;
            
      String algatorRoot = ATGlobal.getALGatorRoot();
      if (line.hasOption("algator_root")) {
        algatorRoot = line.getOptionValue("algator_root");        
      }
      ATGlobal.setALGatorRoot(algatorRoot);

      String dataRoot = ATGlobal.getALGatorDataRoot();
      if (line.hasOption("data_root")) {
	dataRoot = line.getOptionValue("data_root");
      }
      ATGlobal.setALGatorDataRoot(dataRoot);
      
      if (line.hasOption("algorithm")) {
	algorithmName = line.getOptionValue("algorithm");
      }
      if (line.hasOption("testset")) {
	testsetName = line.getOptionValue("testset");
      }

      if (line.hasOption("compile")) {
	alwaysCompile = true;
      }

      if (line.hasOption("exec")) {
	alwaysRunTests = true;
      }

      if (line.hasOption("list_jobs")) {
	listOnly = true;
      }
      
      MeasurementType mType = MeasurementType.EM;
      if (line.hasOption("mtype")) {
	try {
          mType = MeasurementType.valueOf(line.getOptionValue("mtype").toUpperCase());
        } catch (Exception e) {}  
      }

      boolean verbose = line.hasOption("verbose");
      
      ATLog.setLogLevel(ATLog.LOG_LEVEL_OFF);
      if (verbose) {
        ATLog.setLogLevel(ATLog.LOG_LEVEL_STDOUT);
      }
      
      int taskID = 0; // default task id (if run manually without -i switch) 
      if (line.hasOption("id")) {
        try {
          taskID = Integer.parseInt(line.getOptionValue("id"));        
        } catch (Exception e) {}
      }

      runAlgorithms(dataRoot, projectName, algorithmName, testsetName, mType, alwaysCompile, alwaysRunTests, listOnly, verbose, taskID);

    } catch (ParseException ex) {
      printMsg(options);
    }
  }

  private static void runAlgorithms(String dataRoot, String projName, String algName,
	  String testsetName, MeasurementType mType, boolean alwaysCompile, 
          boolean alwaysRun, boolean printOnly, boolean verbose, int taskID) {
    
    // Test the project
    Project projekt = new Project(dataRoot, projName);
    if (!projekt.getErrors().get(0).equals(ErrorStatus.STATUS_OK)) {
      System.out.println(projekt.getErrors().get(0));
      System.exit(0);
    }
     
    // Test algorithms
    ArrayList<EAlgorithm> eAlgs;
    if (!algName.isEmpty()) {
      EAlgorithm alg = projekt.getAlgorithms().get(algName);
      if (alg == null) {
	System.out.println("Invalid algorithm.");
	System.exit(0);
      }
      eAlgs = new ArrayList(); 
      eAlgs.add(alg);
    } else {
       eAlgs = new ArrayList(projekt.getAlgorithms().values());
    }
    
    // Test testsets
    ArrayList<ETestSet> eTests;
    if (!testsetName.isEmpty()) {
      ETestSet test = projekt.getTestSets().get(testsetName);
      if (test == null) {
	System.out.println("Invalid testset.");
	System.exit(0);
      }
      eTests = new ArrayList<>(); 
      eTests.add(test);
    } else {
       eTests = new ArrayList(projekt.getTestSets().values());
    }
    
    
    // Test mesurement type
    EResultDescription rDesc = projekt.getResultDescriptions().get(mType);  
    if (rDesc == null) {
      System.out.printf("Result description file for '%s' does not exist.\n", mType.getExtension());
      System.exit(0);
    }
    if (mType.equals(MeasurementType.JVM)) {
      String vmep = ELocalConfig.getConfig().getField(ELocalConfig.ID_VMEP);
      File vmepFile = new File(vmep == null ? "":vmep);

      if (vmep == null || vmep.isEmpty() /*|| !vmepFile.exists()  || !vmepFile.canExecute()*/) {
        System.out.printf("Invelid vmep executable: '%s'.\n", vmep);
        System.exit(0);    
      }
    }
    
    
    if (printOnly) {    
      String algs = "none";
      System.out.println("DataRoot       : " + dataRoot);
      System.out.println("Project        : " + projName);
      System.out.print("Outdated tests : ");
      for (EAlgorithm eAlg : eAlgs) {      
        for (ETestSet eTestSet : eTests) {
	  if (!ATTools.resultsAreUpToDate(projekt, eAlg.getName(), eTestSet.getName())) {
	    System.out.printf("\n   (%s, %s)", eAlg.getName(), eTestSet.getName());
	    algs = "";
	  }
        }
      }
      System.out.println(algs);
    } else {
      for (int i = 0; i < eAlgs.size(); i++) {
	for (int j = 0; j < eTests.size(); j++) {
          Notificator notificator = getNotificator(eAlgs.get(i).getName(), eTests.get(j).getName(), mType, taskID);
	  ErrorStatus error = Executor.algorithmRun(projekt, eAlgs.get(i).getName(), 
		  eTests.get(j).getName(),  mType, notificator, alwaysCompile, alwaysRun, verbose);          
	}
      }
    }
  }
}
