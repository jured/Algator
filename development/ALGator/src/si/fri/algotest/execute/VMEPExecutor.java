package si.fri.algotest.execute;

import algator.VMEPExecute;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.Project;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.global.ExecutionStatus;
import si.fri.algotest.global.VMEPErrorStatus;

/**
 *
 * @author tomaz
 */
public class VMEPExecutor {
  
  /** How many times the process executed by vmep virtual machine is expected to be slower than process executed by normal vm */
  private static final int SLOW_FACTOR = 2;
  /** Maximum number of seconds for vmep startup */ 
  private static final int VMEPVM_DELAY = 5;
  
  
  /**
   * Iterates trought testset and executes each testcase. To execute a testcase, a VMEP virtual machine 
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
  public static void iterateTestSetAndRunAlgorithm(Project project, String algName, 
          String testSetName, EResultDescription resultDesc, AbstractTestSetIterator it, 
          Notificator notificator, File resultFile) {

    ArrayList<ParameterSet> allAlgsRestuls = new ArrayList();
    VMEPErrorStatus executionStatus;
    
    String delim      = resultDesc.getField(EResultDescription.ID_Delim);
    EParameter algPar = EResultDescription.getAlgorithmNameParameter(algName);
    String algP       = algPar.getField(EParameter.ID_Value);
    EParameter tsPar  = EResultDescription.getTestsetNameParameter(testSetName);
    String tsP        = tsPar.getField(EParameter.ID_Value);
    
    EParameter killedEx = EResultDescription.getExecutionStatusParameter(ExecutionStatus.KILLED);
    EParameter failedEx = EResultDescription.getExecutionStatusParameter(ExecutionStatus.FAILED);
      
    /* The name of the output file */
    String projectRoot = ATGlobal.getPROJECTroot(project.dataRoot, project.getName());
    String resFilename = ATGlobal.getRESULTfilename(projectRoot, algName, testSetName, MeasurementType.JVM);
    
    // Maximum time allowed (in seconds) for one execution of one test; if the algorithm 
    // does not  finish in this time, the execution is killed
    int timeLimit = SLOW_FACTOR * 10;
    try {
       timeLimit = SLOW_FACTOR * Integer.parseInt((String) it.testSet.getField(ETestSet.ID_TimeLimit));
    } catch (NumberFormatException e) {
        // if ETestSet.ID_TimeLimit parameter is missing, timelimit is set to 30 (sec) and exception is ignored
    }
    timeLimit += VMEPVM_DELAY;
    
    int testID = 0; // 
    try {
      while (it.hasNext()) {
        it.readNext();++testID;

        ParameterSet result = new ParameterSet();
        result.addParameter(algPar, true);
        result.addParameter(tsPar,  true);

        String tmpFolderName = ATGlobal.getTMPDir(project.dataRoot, project.projectName);          
        
        TestCase testCase = it.getCurrent();
        if (testCase != null) {
          EParameter testP = testCase.getParameters().getParamater(EResultDescription.testIDParName);
          result.addParameter(testP,  true);
          
          executionStatus = runWithLimitedTime(project.getName(), algName, testSetName, testID, tmpFolderName, project.dataRoot, timeLimit);
        } else {
          executionStatus = VMEPErrorStatus.INVALID_TEST;
          ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_TEST, " ");
        }

        
        String testResultLine;
        if (!executionStatus.equals(VMEPErrorStatus.OK)) {
          if (executionStatus.equals(VMEPErrorStatus.KILLED)) {
            notificator.notify(testID,ExecutionStatus.KILLED);
            result.addParameter(killedEx, true);
            result.addParameter(EResultDescription.getErrorParameter(
              String.format("Killed after %d second(s)", timeLimit)), true);
          } else {
            notificator.notify(testID,ExecutionStatus.FAILED);
            result.addParameter(failedEx, true);
            result.addParameter(EResultDescription.getErrorParameter(
              ErrorStatus.getLastErrorMessage() + executionStatus.toString()), true);
          }
          testResultLine=result.toString(resultDesc.getParamsOrder(), false, delim);
        } else {
          String oneResultFilename = ATGlobal.getJVMRESULTfilename(tmpFolderName, algName, testSetName, testID);
          try (Scanner sc = new Scanner(new File(oneResultFilename))) {
            testResultLine = sc.nextLine();
            if (testResultLine.startsWith(algP + delim + tsP)) {
              notificator.notify(testID,ExecutionStatus.DONE);
            } else {
              notificator.notify(testID,ExecutionStatus.FAILED);
              result.addParameter(failedEx, true);
              result.addParameter(EResultDescription.getErrorParameter(
                VMEPErrorStatus.UNKNOWN.toString()), true);
              testResultLine=result.toString(resultDesc.getParamsOrder(), false, delim);
            }
          } catch (Exception e) {
            notificator.notify(testID,ExecutionStatus.FAILED);
            result.addParameter(failedEx, true);
            result.addParameter(EResultDescription.getErrorParameter(e.toString()), true);
            testResultLine=result.toString(resultDesc.getParamsOrder(), false, delim);
          }
        }  
        // append a line representing test results to the corresponding result file        
        PrintWriter pw = new PrintWriter(new FileWriter(resultFile, true));
          pw.println(testResultLine);
        pw.close();
        
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
   *   <code>VMEPErrorStatus.VMEPVM_ERROR</code> if problems occure during the initialization or execution phase<br>
   *   <code>VMEPErrorStatus.*</code> if algorithm exited with exit code different than 0 <br>
   *   <code>VMEPErrorStatus.OK</code> if algorithm exited normally <br>  

   * If algorithm finishes in time, </code>runWithLimitedTime</code> returns <code>VMEPErrorStatus.OK</code>
   */
  static VMEPErrorStatus runWithLimitedTime(String projectName, String algname, String testSetName, 
          int testID, String comFolder, String dataRoot, int timeLimit) {
    
    ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK, "");
    
    Object result =  VMEPExecute.runWithVMEP(projectName,algname, testSetName, testID, comFolder, dataRoot);
      
    // during the process creation, an error occured
    if (result == null || !(result instanceof Process)) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.PROCESS_CANT_BE_CREATED, result == null ? "???" : result.toString());
      return VMEPErrorStatus.VMEPVM_ERROR;
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
        ErrorStatus.setLastErrorMessage(ErrorStatus.PROCESS_KILLED, String.format("(after %d sec.)", (int)secondsPassed)); 
        externProcess.destroy();
        return VMEPErrorStatus.KILLED;
      } catch (Exception e) {}
    }
    
    try {
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(externProcess.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(externProcess.getErrorStream()));
 
      String s;StringBuffer sb = new StringBuffer();
      while ((s = stdInput.readLine()) != null) sb.append(s);            
      while ((s = stdError.readLine()) != null) sb.append(s);
      
      if (exitCode != 0) {
        ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_EXECUTING_VMPEJVM, sb.toString());
        return VMEPErrorStatus.getErrorStatusByID(exitCode);
      } else {
        if (ATGlobal.verboseLevel > 0)
          System.out.println(sb);
        return  VMEPErrorStatus.OK;
      }
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.PROCESS_CANT_BE_CREATED, e.toString().replaceAll("\n", ""));
      return VMEPErrorStatus.VMEPVM_ERROR;
    }  
  }
  
   
  
  
  
}
