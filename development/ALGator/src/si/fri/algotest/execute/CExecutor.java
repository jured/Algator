package si.fri.algotest.execute;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public class CExecutor {

  public static Object runWithAlgatorC(String project_name, String alg_name, String testset_name, String mType) {    
    ATLog.log(String.format("Running algatorc -e %s %s %s %s", project_name, alg_name, testset_name, mType), 3);
    try {      
      String algatorCCommand = "algatorc";            
      String[] command = {algatorCCommand, "-e", project_name, alg_name, testset_name};      
      
      ProcessBuilder probuilder = new ProcessBuilder( command );
    
      return probuilder.start();      
    } catch (Exception e) {
      return e.toString();
    }
  }

  
  // !!! Metoda je zelo podobna metodi runWithLimitedTime v WMEPExecutor. Ali res morata biti dve metodi???
  // Poglej, če bi lahko kaj ponovno uporabil???
  /**
   * Using external program (algatorc), method executes given task. If task does not finish in 
   * timeAllowed seconds, algatorc is killed and ErrorStatus.PROCESS_KILLED is returned. Otherwise,
   * if algatorC exit status != 0, ErrorStatus.ERROR_EXECUTING_C is returned.
   * If task finishes correctly, ErrorStatus.STATUS_OK is returned.
   */  
  static void runWithLimitedTime(String project_name, String alg_name, String testset_name, String mType, int timeLimit) {    
    ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK, "");
    
    Object result =  runWithAlgatorC(project_name, alg_name, testset_name, mType);
      
    // during the process creation, an error occured
    if (result == null || !(result instanceof Process)) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.PROCESS_CANT_BE_CREATED, result == null ? "???" : result.toString());
      return;
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
        return;
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
      } else {
        if (ATGlobal.verboseLevel > 0)
          System.out.println(sb);
      }
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.PROCESS_CANT_BE_CREATED, e.toString().replaceAll("\n", ""));      
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


  
}
