package algator;

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
import si.fri.adeserver.ADETask;
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
import static si.fri.algotest.tools.ATTools.getTaskResultFileName;

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
    

    Option data_root = OptionBuilder.withArgName("folder")
	    .withLongOpt("data_root")
	    .hasArg(true)
	    .withDescription("use this folder as data_root; default value in $ALGATOR_DATA_ROOT (if defined) or $ALGATOR_ROOT/data_root")
	    .create("d");
    
    Option algator_root = OptionBuilder.withArgName("folder")
            .withLongOpt("algator_root")
            .hasArg(true)
            .withDescription("use this folder as algator_root; default value in $ALGATOR_ROOT")
            .create("r");
    
    Option verbose = OptionBuilder.withArgName("verbose_level")
            .withLongOpt("verbose")
            .hasArg(true)
            .withDescription("print additional information (0 = OFF, 1 = some (default), 2 = all")
            .create("v");

    Option logTarget = OptionBuilder.withArgName("log_target")
            .hasArg(true)
            .withDescription("where to print information (1 = stdout (default), 2 = file, 3 = both")
            .create("log");
    
    options.addOption(algorithm);
    options.addOption(testset);
    options.addOption(data_root);
    options.addOption(algator_root);
    options.addOption(measurement);
        
    options.addOption(verbose);
    options.addOption(logTarget);
    
    options.addOption("h", "help", false,
	    "print this message");
    options.addOption("c", "compile", false,
	    "compile all classes; if this option is omitted, only outdated classes will be compiled");
    options.addOption("e", "exec", false,
	    "execute test(s) without checking; if this option is omitted, only outdated tests will be executed");
    options.addOption("l", "list_jobs", false,
	    "list the jobs (i.e. the pairs (algorithm, testset)) that are to be executed");
        
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

  private static Notificator getNotificator(final String proj, final String alg, final String testSet, final MeasurementType mt) {
    Notificator notificator = 
      new Notificator() {
      
      public void notify(int i, ExecutionStatus status) {
        if ((ATLog.getLogTarget() & ATLog.LOG_TARGET_STDOUT) != 0)
          System.out.println(String.format("[%s, %s, %s]: test %3d / %-3d - %s", 
            alg, testSet, mt.getExtension(), i, this.getN(),status.toString()));
        
        String statusMsg = String.format("%d/%d # %d%c", i, getN(), 100*i/this.getN(), '%');
        ADETask tmpTask = new ADETask(proj, alg, testSet, mt.getExtension(), true);
        ADETools.writeTaskStatus(tmpTask,  TaskStatus.RUNNING, statusMsg, ATGlobal.getThisComputerID());
      }
    };
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

      ATGlobal.verboseLevel = 1;
      if (line.hasOption("verbose")) {
        if (line.getOptionValue("verbose").equals("0"))
          ATGlobal.verboseLevel = 0;
        if (line.getOptionValue("verbose").equals("2"))
          ATGlobal.verboseLevel = 2;
      }
      
      ATGlobal.logTarget = ATLog.LOG_TARGET_STDOUT;
      if (line.hasOption("log")) {
        if (line.getOptionValue("log").equals("0"))
          ATGlobal.logTarget = ATLog.LOG_TARGET_OFF;
        if (line.getOptionValue("log").equals("2"))
          ATGlobal.logTarget = ATLog.LOG_TARGET_FILE;
        if (line.getOptionValue("log").equals("3"))
          ATGlobal.logTarget = ATLog.LOG_TARGET_FILE + ATLog.LOG_TARGET_STDOUT;
      }     
      ATLog.setLogTarget(ATGlobal.logTarget);
            
      runAlgorithms(dataRoot, projectName, algorithmName, testsetName, mType, alwaysCompile, alwaysRunTests, listOnly);

    } catch (ParseException ex) {
      printMsg(options);
    }
  }

  private static void runAlgorithms(String dataRoot, String projName, String algName,
	  String testsetName, MeasurementType mType, boolean alwaysCompile, 
          boolean alwaysRun, boolean printOnly) {
    
    // Test the project
    Project projekt = new Project(dataRoot, projName);
    if (!projekt.getErrors().get(0).equals(ErrorStatus.STATUS_OK)) {
      ATLog.log("Invalid project: " + projekt.getErrors().get(0).toString(), 1);

      System.exit(0);
    }
     
    // Test algorithms
    ArrayList<EAlgorithm> eAlgs;
    if (!algName.isEmpty()) {
      EAlgorithm alg = projekt.getAlgorithms().get(algName);
      if (alg == null) {
	ATLog.log("Invalid algorithm - " + algName, 1);
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
	ATLog.log("Invalid testset - " + testsetName, 1);
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
      System.out.println("DataRoot       : " + dataRoot);
      System.out.println("Project        : " + projName);
      System.out.println("Tasks          :  Algorithm             TestSet        MType  UpToDate Complete");
      for (EAlgorithm eAlg : eAlgs) {      
        for (ETestSet eTestSet : eTests) {
          for (String mtype : new String[] {"EM", "CNT", "JVM"}) {
            String resultFileName = getTaskResultFileName(projekt, eAlg.getName(), eTestSet.getName(), mtype);
            int expectedNumberOfInstances = eTestSet.getFieldAsInt(ETestSet.ID_N);            
            
            boolean uptodate = ATTools.resultsAreUpToDate(projekt, eAlg.getName(), eTestSet.getName(), mtype, resultFileName);
            boolean complete = ATTools.resultsAreComplete(resultFileName, expectedNumberOfInstances);
	    
            System.out.printf("File: '%s'\n", resultFileName);
	    System.out.printf("                 %-23s%-15s%-7s%-9s%-9s\n", eAlg.getName(), eTestSet.getName(), mtype, new Boolean(uptodate), new Boolean(complete));
	  }
        }
      }
    } else {
      ErrorStatus error = ErrorStatus.STATUS_OK;
      for (int i = 0; i < eAlgs.size(); i++) {
	for (int j = 0; j < eTests.size(); j++) {
          ATLog.setPateFilename(ATGlobal.getTaskHistoryFilename(projName, eAlgs.get(i).getName(), eTests.get(j).getName(), mType.getExtension()));
          Notificator notificator = getNotificator(projName, eAlgs.get(i).getName(), eTests.get(j).getName(), mType);
	  error = Executor.algorithmRun(projekt, eAlgs.get(i).getName(), 
		  eTests.get(j).getName(),  mType, notificator, alwaysCompile, alwaysRun); 
          
          // when execution failes, all batch is canceled
          // Is this a good idea?  Probably not!
          // if (!error.equals(ErrorStatus.STATUS_OK)) {
          //  System.exit(error.ordinal());
          //}
	}        
      }
      System.exit(ErrorStatus.STATUS_OK.ordinal()); // 0
    }
  }
}
