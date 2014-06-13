package algator;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import si.fri.algotest.analysis.DataAnalyser;
import si.fri.algotest.analysis.view.Analyser;
import si.fri.algotest.analysis.TableData;
import si.fri.algotest.entities.EQuery;
import si.fri.algotest.entities.Project;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public class Analyse {

  private static String introMsg = "ALGator Analyzer, " + Version.getVersion();

  private static Options getOptions() {
    Options options = new Options();

    Option data_root = OptionBuilder.withArgName("data_root_folder")
            .withLongOpt("data_root")
            .hasArg(true)
            .withDescription("use this folder as data_root; default value in $ALGATOR_DATA_ROOT")
            .create("d");

    Option query = OptionBuilder.withArgName("query_name")
            .withLongOpt("query")
            .hasArg(true)
            .withDescription("the name of the query to run")
            .create("q");
    
    Option queryOrigin = OptionBuilder.withArgName("[R|F|S]")
            .withLongOpt("query_origin")
            .hasArg(true)
            .withDescription("the origin of the query (R=data root folder, F=custom folder, S=standard input); default: R")
            .create("o");
    

    options.addOption(data_root);
    options.addOption(query);
    options.addOption(queryOrigin);

    options.addOption("h", "help", false,
            "print this message");


    options.addOption("v", "verbose", false,
            "print additional information on error");

    options.addOption("u", "usage", false,
            "print usage guide");
    
    return options;
  }

  private static void printMsg(Options options) {

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("algator.Analyse [options] project_name", options);

    System.exit(0);
  }

  private static void printUsage() {
    Scanner sc = new Scanner((new Analyse()).getClass().getResourceAsStream("/data/AnalyserUsage.txt")); 
    while (sc.hasNextLine())
      System.out.println(sc.nextLine());
    
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

      if (line.hasOption("u")) {
        printUsage();
      }
      
      
      String[] curArgs = line.getArgs();
      if (curArgs.length != 1) {
        printMsg(options);
      }

      boolean printTable = false;

      String dataRoot = System.getenv("ALGATOR_DATA_ROOT");
      if (line.hasOption("data_root")) {
        dataRoot = line.getOptionValue("data_root");        
      }
      System.out.println("Data root = " + dataRoot);
      
      ATLog.setLogLevel(ATLog.LOG_LEVEL_OFF);
      if (line.hasOption("verbose")) {
        ATLog.setLogLevel(ATLog.LOG_LEVEL_STDOUT);
      }

      String projectName = curArgs[0];
      Project projekt = new Project(dataRoot, projectName);
      if (!projekt.getErrors().get(0).equals(ErrorStatus.STATUS_OK)) {
        System.out.println(projekt.getErrors().get(0));
        System.exit(0);
      }

      ATGlobal.ALGatorDataRoot = dataRoot;
   
      
      String origin = line.getOptionValue("query_origin");
      if (origin == null) origin = "R";
      
      if (line.hasOption("query") || "S".equals(origin)) {  
        // if a query is given, run a query and print result ...
        String result = runQuery(projekt, line.getOptionValue("query"), origin);
        System.out.println(result);
      } else {
        // ...else run a GUI analizer
        new Analyser(projekt);
      }
      
    } catch (ParseException ex) {
      printMsg(options);
    }
  }
  
  public static String runQuery(Project project, String queryName, String origin) {
    EQuery query = new EQuery();
    switch (origin) {
      case "S":
        Scanner sc = new Scanner(System.in);
        String vsebina = "";
        while (sc.hasNextLine()) 
          vsebina += sc.nextLine();
        query.initFromJSON(vsebina);
        break;
      case "F":
      case "R":
        String fileName;
        if (origin.equals("F")) {
          fileName = queryName;
        } else
          fileName = ATGlobal.getQUERYfilename(project.getProject().getProjectRootDir(), queryName);
        query.initFromFile(new File(fileName));
        break;
    }
    
    String result = "Invalid query.";
    if (query != null & !query.toJSONString().equals("{}")) {
      // run query ...
      TableData td = DataAnalyser.runQuery(project.getProject(), query);
      // ... and print table to screen
      result = td.toString();
    }

    return result;
  }
  
}
