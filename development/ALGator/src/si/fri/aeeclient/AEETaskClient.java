package si.fri.aeeclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import si.fri.adeserver.ADEGlobal;
import si.fri.algotest.entities.ELocalConfig;
import si.fri.algotest.global.ATGlobal;

/**
 *
 * @author tomaz
 */
public class AEETaskClient {
  
  private static void runTask(String task) {
    String [] parts = task.split(ADEGlobal.STRING_DELIMITER);
    if (parts.length != 5) return; // error in task format
    
    CommandLine cmdLine = new CommandLine("java");    
//  to potrebujem samo v primeru, da TaskClient poganjam iz NetBeansa    
  cmdLine.addArgument("-cp");
  cmdLine.addArgument("/Users/Tomaz/Dropbox/FRI/ALGOSystem/ALGator/development/ALGator/dist/ALGator.jar");
    cmdLine.addArgument("algator.Execute"); 
    
    // Project
    cmdLine.addArgument(parts[1]);
    // Algorithm
    cmdLine.addArgument("-a");
    cmdLine.addArgument(parts[2]);
    // Testset
    cmdLine.addArgument("-t");
    cmdLine.addArgument(parts[3]);
    // Measurement type
    cmdLine.addArgument("-m");
    cmdLine.addArgument(parts[4]);
    // task id
    cmdLine.addArgument("-i");
    cmdLine.addArgument(parts[0]);
    // data_root
    cmdLine.addArgument("-d");
    cmdLine.addArgument(ATGlobal.getALGatorDataRoot());
    // Always execute
    cmdLine.addArgument("-e");

    DefaultExecutor executor = new DefaultExecutor();
    executor.setExitValue(0);
    // max time to wait for task to finish : 10 minutes
    ExecuteWatchdog watchdog = new ExecuteWatchdog(10*60*1000);
    executor.setWatchdog(watchdog);
    
    AEELog.log(": Starting    - " + task);
    try {
      int exitValue = executor.execute(cmdLine);
      
      if (watchdog.killedProcess()) {
        AEELog.log(": Killed      - " + task);
      } else {
        AEELog.log(": Completed   - " + task);
      }
    } catch (Exception e) {
      AEELog.log(ADEGlobal.ERROR_PREFIX + e.toString());
    }
  }

  public static void runClient(String hostName)  {
    if (hostName == null)
      hostName   = ELocalConfig.getConfig().getTaskServerName();
    
    int    portNumber = ADEGlobal.ADEPort;
    
    String compID = ELocalConfig.getConfig().getField(ELocalConfig.ID_COMPID);
    
    boolean logTaskServerProblem = true;
    
    while(true) {  
      try (
              Socket kkSocket = new Socket(hostName, portNumber);
              PrintWriter    toServer    = new PrintWriter(kkSocket.getOutputStream(), true);
              BufferedReader fromServer  = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));) 
      {
        logTaskServerProblem = true;
        AEELog.log("Task client connected to server - " + hostName);
        System.out.println("Task client connected to server - " + hostName);
        while (true) {
          String taskRequset = ADEGlobal.REQ_GET_NEXT_TASK + ADEGlobal.STRING_DELIMITER + compID;
          toServer.println(taskRequset);
          String task = fromServer.readLine();
          if ((task != null) && !task.isEmpty() && !task.equals(ADEGlobal.NO_TASKS)) {
            if (!ADEGlobal.isError(task)) {
              runTask(task);
              
              String taskNo = ""; try {taskNo = task.split(" ")[0];} catch (Exception e) {}
              toServer.println(ADEGlobal.REQ_COMPLETE_TASK + ADEGlobal.STRING_DELIMITER + taskNo);
            }
          }
          
          // sleep for a second
          try {Thread.sleep(1000);} catch (InterruptedException ex) {}
        }
        
      } catch (UnknownHostException e) {
        System.out.println("Unknown host " + hostName);
        AEELog.log("Not connected - unknown host name.");
      } catch (java.net.ConnectException e) {
        if (logTaskServerProblem) {
          System.out.println("TaskServer is not running at "+ hostName);
          AEELog.log("Not connected - TaskServer is not running at " + hostName);
          logTaskServerProblem = false;
        }
      } catch (IOException e) {
        System.out.println("I/O error " + e);
        AEELog.log("Not connected - " + e.toString());
      }
      
      // sleep for 5 seconds before next try
      try {Thread.sleep(5000);} catch (InterruptedException ex) {}
    }
  }
}
