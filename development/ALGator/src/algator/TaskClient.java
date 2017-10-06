package algator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import si.fri.adeserver.ADEGlobal;
import si.fri.algotest.entities.ELocalConfig;
import si.fri.algotest.global.ATGlobal;

/**
 *
 * @author tomaz
 */
public class TaskClient {
  
  private static String introMsg = "ALGator TaskClient, " + Version.getVersion();

  private static Options getOptions() {
    Options options = new Options();

    Option data_root = OptionBuilder.withArgName("folder")
            .withLongOpt("data_root")
            .hasArg(true)
            .withDescription("use this folder as data_root; default value in $ALGATOR_DATA_ROOT (if defined) or $ALGATOR_ROOT/data_root")
            .create("dr");

    Option data_local = OptionBuilder.withArgName("folder")
            .withLongOpt("data_local")
            .hasArg(true)
            .withDescription("use this folder as data_LOCAL; default value in $ALGATOR_DATA_LOCAL (if defined) or $ALGATOR_ROOT/data_local")
            .create("dl");    
    
    Option algator_root = OptionBuilder.withArgName("folder")
            .withLongOpt("algator_root")
            .hasArg(true)
            .withDescription("use this folder as algator_root; default value in $ALGATOR_ROOT")
            .create("r");

    Option server = OptionBuilder.withArgName("server_name")
            .withLongOpt("server")
            .hasArg(true)
            .withDescription("the name of the server with TaskServer")
            .create("s");

    Option ask = OptionBuilder.withArgName("request")
            .withLongOpt("ask")
            .hasArg(true)
            .withDescription("send a <request> to TaskServer and print servers's response")
            .create("a");
    
    options.addOption(data_root);
    options.addOption(data_local);
    options.addOption(algator_root);
    options.addOption(server);
    options.addOption(ask);

    options.addOption("h", "help", false,
            "print this message");

    return options;
  }

  private static void printMsg(Options options) {
    System.out.println(introMsg + "\n");

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("algator.TaskClient [options]", options);

    System.exit(0);
  }
  
  
  private static String askTaskServer(String hostName, String request) {
    if (hostName == null)
      hostName   = ELocalConfig.getConfig().getTaskServerName();
    
    int    portNumber = ADEGlobal.ADEPort;
    
    String compID = ELocalConfig.getConfig().getComputerID();

      try (
        Socket kkSocket = new Socket(hostName, portNumber);
        PrintWriter    toServer    = new PrintWriter(kkSocket.getOutputStream(), true);
        BufferedReader fromServer  = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));) 
      {
          String taskRequset = ADEGlobal.REQ_STATUS;
          toServer.println(request);
          return fromServer.readLine().replaceAll("<br>", "\n");
      } catch (Exception e) {
        return String.format("TaskServer on '%s' is not running.", hostName);
      }          
  }

  public static void main(String args[]) {
    Options options = getOptions();

    CommandLineParser parser = new BasicParser();
    try {
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("h")) {
	printMsg(options);
      }
            
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
      
      String dataLocal = ATGlobal.getALGatorDataLocal();
      if (line.hasOption("data_local")) {
	dataLocal = line.getOptionValue("data_local");
      }
      ATGlobal.setALGatorDataLocal(dataLocal);            
      
      String serverName = null;
      if (line.hasOption("server")) {
	serverName = line.getOptionValue("server");
      }
      
      if (line.hasOption("ask")) {
        String request = line.getOptionValue("ask");        

        System.out.println(askTaskServer(serverName, request));
        System.exit(0);
      }
            
      si.fri.aeeclient.AEETaskClient.runClient(serverName);
      
    } catch (ParseException ex) {
      printMsg(options);
    }
  }
  
}
