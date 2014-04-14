package algator;

import java.util.ArrayList;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import si.fri.algotest.entities.EAlgorithm;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.Project;
import si.fri.algotest.execute.Executor;
import si.fri.algotest.execute.Notificator;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.tools.ATTools;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public class Execute {

  private static String introMsg = "ALGator Executor, " + Version.getVersion();
  
  static Notificator notificator = new Notificator() {
    public void notify(int i) {
      System.out.println(String.format("Notificator: test %d out of %d done.", i, this.getN()));
    }
  };

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


    Option data_root = OptionBuilder.withArgName("data_root_folder")
	    .withLongOpt("data_root")
	    .hasArg(true)
	    .withDescription("use this folder as data_root; default value in $ALGATOR_DATA_ROOT")
	    .create("d");


    options.addOption(algorithm);
    options.addOption(testset);
    options.addOption(data_root);

    options.addOption("h", "help", false,
	    "print this message");
    options.addOption("c", "compile", false,
	    "compile all classes; if this option is omitted, only outdated classes will be compiled");
    options.addOption("e", "exec", false,
	    "execute test(s) without checking; if this option is omitted, only outdated tests will be executed");
    options.addOption("l", "list_jobs", false,
	    "list the jobs (i.e. the pairs (algorithm, testset)) that are to be executed");
    
    options.addOption("v", "verbose", false,
	    "print additional information on error");
    

    return options;
  }

  private static void printMsg(Options options) {

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("algator.Executor [options] project_name", options);

    System.exit(0);
  }

  /**
   * Used to run the system. Parameters are given trought the arguments
   *
   * @param args
   */
  public static void main(String args[]) {
    System.out.println(introMsg + "\n");

    Options options = getOptions();

    CommandLineParser parser = new BasicParser();
    try {
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("h")) {
	printMsg(options);
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

      String dataRoot = System.getenv("ALGATOR_DATA_ROOT");
      if (line.hasOption("data_root")) {
	dataRoot = line.getOptionValue("data_root");
      }

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

      ATLog.setLogLevel(ATLog.LOG_LEVEL_OFF);
      if (line.hasOption("verbose")) {
        ATLog.setLogLevel(ATLog.LOG_LEVEL_STDOUT);
      }

      runAlgorithms(dataRoot, projectName, algorithmName, testsetName, alwaysCompile, alwaysRunTests, listOnly);


    } catch (ParseException ex) {
      printMsg(options);
    }



  }

  private static void runAlgorithms(String dataRoot, String projName, String algName,
	  String testsetName, boolean alwaysCompile, boolean alwaysRun, boolean printOnly) {
    
    Project projekt = new Project(dataRoot, projName);
    if (!projekt.getErrors().get(0).equals(ErrorStatus.STATUS_OK)) {
      System.out.println(projekt.getErrors().get(0));
      System.exit(0);
    }
     
    ArrayList<EAlgorithm> eAlgs;
    if (!algName.isEmpty()) {
      EAlgorithm alg = projekt.getAlgorithms().get(algName);
      if (alg == null) {
	System.out.println("Invalid algorithm.");
	System.exit(0);
      }
      eAlgs = new ArrayList<>(); 
      eAlgs.add(alg);
    } else {
       eAlgs = new ArrayList(projekt.getAlgorithms().values());
    }
    
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
	  System.out.printf("Running test set '%s' with algorithm '%s' \n", eTests.get(j).getName(), eAlgs.get(i).getName());
	  ErrorStatus error = Executor.algorithmRun(dataRoot, projName, eAlgs.get(i).getName(), 
		  eTests.get(j).getName(),  notificator, alwaysCompile, alwaysRun);
	  //if (!error.isOK())
	    System.out.println(ErrorStatus.getLastErrorMessage() + "\n");
	}
      }
      
    }

  }
}
