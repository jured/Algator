package si.fri.algotest.execute;

import algator.ExternalExecute;
import algator.VMEPExecute;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.Project;
import si.fri.algotest.entities.StatFunction;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.global.VMEPErrorStatus;
import si.fri.algotest.timer.Timer;

/**
 *
 * @author tomaz
 */
public class VMEPExecutor {
  
  /** How many times the process executed by jamvm is expected to be slower than process executed by normal vm */
  private static final int SLOW_FACTOR = 2;
  /** Maximum number of seconds for jamvm startup */ 
  private static final int JAMVM_DELAY = 5;

  /**
   * Iterates trought testset and executes each testcase. To execute a testcase, a JAMVM virtual machine 
   * is used (method runWithLimitedTime). If execution is successful, a result  (one line) is copied from 
   * getJVMRESULTfilename to regular result file for this algorithm-testset. If execution failes, a line with 
   * error message is appended to result file.
   * @param project
   * @param algName
   * @param testSetName
   * @param it
   * @param resultDesc
   * @param notificator
   * @param verbose 
   */
  public static void iterateTestSetAndRunAlgorithm(Project project, String algName, String testSetName,
          AbstractTestSetIterator it, EResultDescription resultDesc, Notificator notificator, boolean verbose) {

    ArrayList<ParameterSet> allAlgsRestuls = new ArrayList();
    ParameterSet oneAlgResults;
    VMEPErrorStatus executionStatus;
    
    String delim = resultDesc.getField(EResultDescription.ID_Delim);
    String algP  = EResultDescription.getAlgorithmNameParameter(algName)  .getField(EParameter.ID_Value);
    String tsP   = EResultDescription.getTestsetNameParameter(testSetName).getField(EParameter.ID_Value);
      
    /* The name of the output file */
    String resFilename = ATGlobal.getRESULTfilename(project.dataRoot, algName, testSetName, MeasurementType.JVM);
    
    // Maximum time allowed (in seconds) for one execution of one test; if the algorithm 
    // does not  finish in this time, the execution is killed
    int timeLimit = SLOW_FACTOR * 10;
    try {
       timeLimit = SLOW_FACTOR * Integer.parseInt((String) it.testSet.getField(ETestSet.ID_TimeLimit));
    } catch (NumberFormatException e) {
        // if ETestSet.ID_TimeLimit parameter is missing, timelimit is set to 30 (sec) and exception is ignored
    }
    timeLimit += JAMVM_DELAY;
    
    int testID = 0; // 
    try (PrintWriter pw = new PrintWriter(resFilename)) {
      while (it.hasNext()) {
        it.readNext();
        
        String tP = it.getCurrent().getParameters().getParamater(EResultDescription.testIDParName).getField(EParameter.ID_Value);
        String resultLine = algP + delim + tsP + delim + tP + delim;
        
        notificator.notify(++testID);

        String tmpFolderName = ATGlobal.getTMPDir(project.dataRoot, project.projectName);          
        executionStatus = runWithLimitedTime(project.getName(), algName, testSetName, testID, tmpFolderName, project.dataRoot, timeLimit);
        
        if (!executionStatus.equals(VMEPErrorStatus.OK)) {
          if (executionStatus.equals(VMEPErrorStatus.KILLED)) {
            pw.printf("%s%s", resultLine, EResultDescription.Status_KILLED);            
          } else {
            String errorMsg = ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_PERFORM_TEST, executionStatus.toString()).toString();
            pw.printf("%s%s%s%s", resultLine, EResultDescription.Status_FAILED, delim, errorMsg);            
          }
        } else {
          String oneResultFilename = ATGlobal.getJVMRESULTfilename(tmpFolderName, algName, testSetName, testID);
          try (Scanner sc = new Scanner(new File(oneResultFilename))) {
            String oneResult = sc.nextLine();
            if (oneResult.startsWith(algP + delim + tsP))
              pw.println(oneResult);
            else
              pw.printf("%s%s%s%s", resultLine, EResultDescription.Status_FAILED, delim, VMEPErrorStatus.UNKNOWN);            
          } catch (Exception e) {
            pw.printf("%s%s%s%s: %s", resultLine, EResultDescription.Status_FAILED, delim, VMEPErrorStatus.UNKNOWN, e.toString().replaceAll("\n", ""));            
          }
        }      
        try {
          FileUtils.deleteDirectory(new File(tmpFolderName));
        } catch (Exception e) {
          ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "Folder can not be removed " + tmpFolderName); 
        }
      }
      it.close();
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_RUN, e.toString());
    }
  }

  /**
   * Tries to obtain process exit code. If the process has not finished yet, 
   * method returns -1, else it returns process exit code.
   */
  private static int processExitCode (Process process) {
    try {
        int exit_code = process.exitValue();
        return exit_code;
    } catch (IllegalThreadStateException itse) {
        return -1;
    }
  }
  
  /**
   * 
   * Runs a given algorithm on a given test and waits for timeLimit seconds. Result of this method is 
   * v VMEPErrorStatus, further information about execution status are stored in ErrorStatus.lastErrorMessage.
   * @return 
   *   <code>VMEPErrorStatus.KILLED</code> if algorithm runs out of time                            <br>
   *   <code>VMEPErrorStatus.JAMVM_ERROR</code> if problems occure during the initialization or execution phase<br>
   *   <code>VMEPErrorStatus.*</code> if algorithm exited with exit code different than 0 <br>
   *   <code>VMEPErrorStatus.OK</code> if algorithm exited normally <br>  

   * If algorithm finishes in time, </code>runWithLimitedTime</code> returns <code>VMEPErrorStatus.OK</code>
   */
  static VMEPErrorStatus runWithLimitedTime(String projectName, String algname, String testSetName, 
          int testID, String comFolder, String dataRoot, int timeLimit) {
    
    ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK, "");
    
    Object result =  VMEPExecute.runWithJamVM(projectName,algname, testSetName, testID, comFolder, dataRoot);
      
    // during the process creation, an error occured
    if (result == null || !(result instanceof Process)) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_PROCESS_CANT_BE_CREATED, result == null ? "???" : result.toString());
      return VMEPErrorStatus.JAMVM_ERROR;
    }
    
    Process externProcess = (Process) result;
     
    // loop and wait for process to finish
    int loop_per_sec  = 10;
    int secondsPassed = 0;
    whileloop: while (timeLimit > 0) {
      // loop for one second
      for(int i=0; i<loop_per_sec; i++) {
        if (processExitCode(externProcess) >= 0)
          break whileloop;
        
        try {Thread.sleep(1000/loop_per_sec);} catch (InterruptedException e) {}
      }
      timeLimit--; secondsPassed++;
    }
    int exitCode = processExitCode(externProcess);
    if (exitCode < 0) { // process hasn't finised yet, it has to be killed
      try {
        ErrorStatus.setLastErrorMessage(ErrorStatus.MESSAGE_PROCESS_KILLED, String.format("(after %d sec.)", (int)secondsPassed)); 
        externProcess.destroy();
      } finally {
        return VMEPErrorStatus.KILLED;
      }
    }
    
    try {
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(externProcess.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(externProcess.getErrorStream()));
 
      String s;StringBuffer sb = new StringBuffer();
      while ((s = stdInput.readLine()) != null) sb.append(s);            
      while ((s = stdError.readLine()) != null) sb.append(s);
      
      if (sb.length() != 0) {
        ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_PROCESS_CANT_BE_CREATED, sb.toString());
        return VMEPErrorStatus.JAMVM_ERROR;
      } else {
        return  VMEPErrorStatus.getErrorStatusByID(exitCode);
      }
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_PROCESS_CANT_BE_CREATED, e.toString().replaceAll("\n", ""));
      return VMEPErrorStatus.JAMVM_ERROR;
    }  
  }
  
   
  
  
  
}
