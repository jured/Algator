package algator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import si.fri.algotest.entities.EProject;
import si.fri.algotest.global.ATGlobal;

/**
 *
 * @author tomaz
 */
public class Admin {
  private static String introMsg = "ALGator Admin, " + Version.getVersion();
  

  private static Options getOptions() {
    Options options = new Options();

    Option data_root = OptionBuilder.withArgName("folder")
	    .withLongOpt("data_root")
	    .hasArg(true)
	    .withDescription("use this folder as data_root; default value in $ALGATOR_DATA_ROOT (if defined) or $ALGATOR_ROOT/data_root")
	    .create("dr");

    options.addOption(data_root);
    
    options.addOption("h", "help", false,
	    "print this message");
    options.addOption("v", "version", false,
	    "print ALGator version");    
    
    options.addOption("cp", "create_project", false,
	    "create a new project");
    options.addOption("ca", "create_algorithm", false,
	    "create a new algorithm for a given project");
    options.addOption("ct", "create_testset", false,
	    "create a new testset for a given project");    
    
    options.addOption("u", "usage", false, "print usage guide");
    
    return options;
  }

  private static void printMsg(Options options) {    
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("algator.Admin [options] <project_name> <algorithm_name>", options);

    System.exit(0);
  }

  private static void printUsage() {
    Scanner sc = new Scanner((new Analyse()).getClass().getResourceAsStream("/data/AdminUsage.txt")); 
    while (sc.hasNextLine())
      System.out.println(sc.nextLine());
    
    System.exit(0);
  }
  
  /**
   * Used to run the system. Parameters are given through the arguments
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
      
      if (line.hasOption("v")) {
        Version.printVersion();
        System.out.println();
      }
      
      String[] curArgs = line.getArgs();

      String dataRoot = ATGlobal.getALGatorDataRoot();
      if (line.hasOption("data_root")) {
        dataRoot = line.getOptionValue("data_root");        
      }
      ATGlobal.setALGatorDataRoot(dataRoot);

      if (line.hasOption("create_project")) {
	if (curArgs.length != 1) {
          System.out.println("Invalid project name");
          printMsg(options); 
        } else {
          createProject(curArgs[0]);
          System.exit(0);
        }
      }

      if (line.hasOption("create_algorithm")) {
	if (curArgs.length != 2) {
          System.out.println("Invalid project or algorithm name");
          printMsg(options); 
        } else {
          createAlgorithm(curArgs[0], curArgs[1]);
          System.exit(0);
        }
      }

      if (line.hasOption("create_testset")) {
	if (curArgs.length != 2) {
          System.out.println("Invalid project or test set name");
          printMsg(options); 
        } else {
          createTestset(curArgs[0], curArgs[1]);
          System.exit(0);
        }
      }
      
      
      printMsg(options);
    } catch (ParseException ex) {
      printMsg(options);
    }
  }
  
  private static boolean createProject(String proj_name) {
    String dataroot = ATGlobal.getALGatorDataRoot();         
    String projSrcFolder = ATGlobal.getPROJECTsrc(ATGlobal.getPROJECTroot(dataroot, proj_name));
    String projRoot = ATGlobal.getPROJECTroot(dataroot, proj_name);
    String projConfFolder = ATGlobal.getPROJECTconfig(dataroot, proj_name);
    String testsFolder = ATGlobal.getTESTSroot(dataroot, proj_name);
    String docFolder = ATGlobal.getPROJECTdocFolder(projRoot);
    
    HashMap<String,String> substitutions = getSubstitutions(proj_name);
        
    System.out.println("Creating project " + proj_name + " ...");
        
    try {                        
      File projFolderFile = new File(projRoot);
      if (projFolderFile.exists()) {
        System.out.printf("\n Project %s already exists!\n", proj_name);
        System.exit(0);
      } 
      
      projFolderFile.mkdirs();
      
      copyFile("templates/PPP.atp",            projConfFolder,  proj_name+".atp",                 substitutions);
      
      copyFile("templates/PPP-em.atrd",        projConfFolder,  proj_name+"-em.atrd",             substitutions);
      
      copyFile("templates/PPPAbsAlgorithm",    projSrcFolder,   proj_name+"AbsAlgorithm.java",    substitutions);
      copyFile("templates/PPPTestCase",        projSrcFolder,   proj_name+"TestCase.java",        substitutions);
      copyFile("templates/PPPTestSetIterator", projSrcFolder,   proj_name+"TestSetIterator.java", substitutions);
      
      copyFile("templates/TestSet1.atts",      testsFolder,     "TestSet1.atts",                  substitutions);
      copyFile("templates/TestSet1.txt",       testsFolder,     "TestSet1.txt",                   substitutions);
      
      copyFile("templates/TestSet1.html",      docFolder,       "TestSet1.html",                  substitutions);
      copyFile("templates/PPP.html",           docFolder,       proj_name + ".html",              substitutions);
      copyFile("templates/TestSets.html",      docFolder,       "TestSets.html",                  substitutions);      

    } catch (Exception e) {
      System.out.println("Can not create project: " + e.toString());
      return false;
    }
    return true;
  }

  private static boolean createAlgorithm(String proj_name, String alg_name) {        
    String dataroot = ATGlobal.getALGatorDataRoot();         
    String projRoot = ATGlobal.getPROJECTroot(dataroot, proj_name);
    String algRoot = ATGlobal.getALGORITHMroot(projRoot, alg_name);
    String algSrc  = ATGlobal.getALGORITHMsrc(projRoot, alg_name);
    String docFolder = ATGlobal.getPROJECTdocFolder(projRoot);
    
    HashMap<String,String> substitutions = getSubstitutions(proj_name);
    substitutions.put("<AAA>", alg_name);
    
    // first create project if it does not exist
    File projFolderFile = new File(projRoot);
    if (!projFolderFile.exists()) {
      if (!createProject(proj_name))
        System.exit(0);
    }    
    
    System.out.println("Creating algorithm " + alg_name +  " for the project " + proj_name);
    try {                              
      
      File algFolderFile = new File(algRoot);
      if (algFolderFile.exists()) {
        System.out.printf("\n Algorithm %s already exists!\n", proj_name);
        System.exit(0);
      }       
      algFolderFile.mkdirs();
      
      copyFile("templates/AAA.atal",           algRoot,         alg_name+".atal",                 substitutions);
      copyFile("templates/AAAAlgorithm",       algSrc,          alg_name+"Algorithm.java",        substitutions);
      copyFile("templates/AAA.html",           docFolder,       alg_name + ".html",               substitutions);
      
      
      EProject eProject = new EProject(new File(ATGlobal.getPROJECTfilename(dataroot, proj_name)));
      ArrayList a = new ArrayList<String>(Arrays.asList(eProject.getStringArray(EProject.ID_Algorithms)));
        a.add(alg_name);
      eProject.set(EProject.ID_Algorithms, a.toArray());
      eProject.saveEntity();

    } catch (Exception e) {
      System.out.println("Can not create project: " + e.toString());
      return false;
    }    
    return true;
  }


  private static boolean createTestset(String proj_name, String testset_name) {        
    String dataroot = ATGlobal.getALGatorDataRoot();         
    String projRoot = ATGlobal.getPROJECTroot(dataroot, proj_name);
    String testsRoot = ATGlobal.getTESTSroot(dataroot, proj_name);
    String docFolder = ATGlobal.getPROJECTdocFolder(projRoot);

    
    HashMap<String,String> substitutions = getSubstitutions(proj_name);
    substitutions.put("<TS>", testset_name);
    
    // first create project if it does not exist
    File projFolderFile = new File(projRoot);
    if (!projFolderFile.exists()) {
      if (!createProject(proj_name))
        System.exit(0);
    }    
    
    System.out.println("Creating test set " + testset_name +  " for the project " + proj_name);
    try {                              
      
      File testsetFolderFile = new File(testsRoot);
      if (!testsetFolderFile.exists()) {
        testsetFolderFile.mkdirs();
      }
      
      File testSetFile = new File(testsRoot + File.separator + testset_name+".atts");
      if (testSetFile.exists()) {
        System.out.printf("\n Testset %s already exists!\n", testset_name);
        System.exit(0);
      }
             
      
      
      copyFile("templates/TS.atts",            testsRoot,       testset_name+".atts",             substitutions);
      copyFile("templates/TS.txt",             testsRoot,       testset_name+".txt",              substitutions);
      copyFile("templates/TS.html",            docFolder,       testset_name + ".html",           substitutions);
      
      
      EProject eProject = new EProject(new File(ATGlobal.getPROJECTfilename(dataroot, proj_name)));
      ArrayList ts = new ArrayList<String>(Arrays.asList(eProject.getStringArray(EProject.ID_TestSets)));
        ts.add(testset_name);
      eProject.set(EProject.ID_TestSets, ts.toArray());
      eProject.saveEntity();

    } catch (Exception e) {
      System.out.println("Can not create test set: " + e.toString());
      return false;
    }    
    return true;
  }
  
  
  //*******************************
  
  private static HashMap<String, String> getSubstitutions(String proj_name) {
    StringBuffer lc = new StringBuffer(proj_name);
    lc.setCharAt(0, Character.toLowerCase(proj_name.charAt(0)));
    String projNameCamelCase = lc.toString();

    SimpleDateFormat sdf = new SimpleDateFormat("MM, YYYY");
        
    HashMap<String, String> substitutions = new HashMap();
    substitutions.put("<PPP>", proj_name);
    substitutions.put("<pPP>", projNameCamelCase);
    substitutions.put("<today>", sdf.format(new Date()));   
    
    substitutions.put("\r", "\n");   
    
    return substitutions;
  }
  
  private static String readFile(String fileName) {
    try {
      ClassLoader classLoader = Admin.class.getClassLoader();
      InputStream fis = classLoader.getResourceAsStream(fileName);
      return new Scanner(fis).useDelimiter("\\Z").next();      
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    return "";
  }
  
  private static void writeFile(String fileName, String content) {
    try {
      // first creates a folder ...
      String filePath = FilenameUtils.getFullPath(fileName);
      File filePathFile = new File(filePath);
      filePathFile.mkdirs();
      
      // ... then writes a content
      PrintWriter pw = new PrintWriter(fileName);
      pw.print(content);
      pw.close();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }
    
  private static String replace(String source, String what, String with) {
    return source.replaceAll(what, with);
  }
  
  /**
   * Copies a template to destination folder  + makes substitutions. 
   */
  private static void copyFile(String tplName, String outputFolder, String outputFileName, HashMap<String, String> substitutions) { 
    String absAlg = readFile(tplName);
    for(String key: substitutions.keySet()) {
      absAlg = replace(absAlg, key, substitutions.get(key));
    }
    writeFile(new File(outputFolder, outputFileName).getAbsolutePath(), absAlg);
  }

}
