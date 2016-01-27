package si.fri.algotest.execute;

import algator.ExternalExecute;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.Entity;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.Project;
import si.fri.algotest.entities.StatFunction;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.global.ExecutionStatus;
import si.fri.algotest.timer.Timer;

/**
 *
 * @author tomaz
 */
public class ExternalExecutor {

  /**
   * The original algorithm that is passed to the executor is written to the
   * TEST file, the algorithm with the results (times and parameters) is written
   * to the RESUL file.
   */
  public static enum SER_ALG_TYPE {

    TEST {
              @Override
              public String toString() {
                return ".test";
              }
            },
    RESULT {
              @Override
              public String toString() {
                return ".result";
              }
            }
  }

  private final static String SERIALIZED_ALG_FILENAME = "alg.ser";

  /**
   * This file is used as a communication chanell between main ALGator executor
   * and ExternalJVMExecutor. When an algorithm test is started, this file is
   * initialized with empty contents. For each execution of the algorithm,
   * ExternalJVMExecutor writes one byte to this file. ALGator's executor
   * regulary checks the content of this file and stops the execution is there
   * is no progress.
   */
  private final static String COMMUNICATION_FILENAME = "alg.com";

  /**
   * Iterates through testset and for each test runs an algorithm.
   *
   * @param project
   * @param algName
   * @param it
   * @param resultDesc
   * @param notificator
   * @param mType
   * @return
   */
  public static void iterateTestSetAndRunAlgorithm(Project project, String algName,
          AbstractTestSetIterator it, EResultDescription resultDesc,
          Notificator notificator, MeasurementType mType, File resultFile) {

    ParameterSet algResultParams; 

    ETestSet testSet = it.testSet;

    // get the number of times to execute one test (this is applicable only
    /// for the EM type of tests; all other tests are performed only once)
    int timesToExecute = 1;
    if (mType.equals(MeasurementType.EM)) {
      try {
        timesToExecute = Integer.parseInt((String) testSet.getField(ETestSet.ID_TestRepeat));
      } catch (Exception e) {
        // if ETestSet.ID_TestRepeat parameter is missing, timesToExecute is set to 1 and exception is ignored
      }
    }
    timesToExecute = Math.max(timesToExecute, 1); // to prevent negative number

    // Maximum time allowed (in seconds) for one execution of one test; if the algorithm 
    // does not  finish in this time, the execution is killed
    int timeLimit = 10;
    try {
      timeLimit = testSet.getField(ETestSet.ID_TimeLimit);
    } catch (Exception e) {
      // if ETestSet.ID_TimeLimit parameter is missing, timelimit is set to 10 (sec) and exception is ignored
    }

    // parameters to be added to result file (in case exception occures while executing algorithm)
    String delim = resultDesc.getField(EResultDescription.ID_Delim);
    EParameter algP      = EResultDescription.getAlgorithmNameParameter(algName);
    EParameter tsP       = EResultDescription.getTestsetNameParameter(it.testSet.getName());
    EParameter failedErr = EResultDescription.getExecutionStatusParameter(ExecutionStatus.FAILED);

    String order[] = resultDesc.getParamsOrder();
    
    int testID = 0; // 
    try {
      while (it.hasNext()) {
        it.readNext();
        testID++;

        TestCase testCase = it.getCurrent();
        
        // v testcase dodam vse parametre; to sem dodal, da se ohrani informacija o parametrih, kot so bili definirani
        // v atrd datoteki; recimo, če je tam definiram parameter tipa double, se prej podatki o tem parametru (recimo 
        // subtype - število decimalk) ni prenesel in se je zato vedno uporabila default vrednost. Po tej spremembi se 
        // podatki prevailno prenesejo, upam pa, da se kaj drugega ne podre! Če se, briši spodnji dve vrstici in 
        // poišči drugo rešitev za prenos podatkov o parametrih iz atrd datoteke!
        for (int i=0; i<resultDesc.getParameters().size(); i++) 
          testCase.addParameter(resultDesc.getParameters().getParameter(i), false);
        
        
        AbsAlgorithm curAlg = New.algorithmInstance(project, algName, mType);
        curAlg.setTimesToExecute(timesToExecute);

        // algorithm instance obtained from file as a result of execution
        AbsAlgorithm resultAlg = null;

        // were testCase and algorithm created?
        boolean executionOK = testCase != null && curAlg != null;

        // was algorithm properly initialized?
        executionOK = executionOK && curAlg.init(testCase) == ErrorStatus.STATUS_OK;

        String tmpFolderName = ATGlobal.getTMPDir(project.getName());

        ErrorStatus executionStatus = ErrorStatus.ERROR_CANT_PERFORM_TEST;

        if (executionOK) {
          saveAlgToFile(New.getClassPathsForAlgorithm(project, algName), curAlg, tmpFolderName, SER_ALG_TYPE.TEST);

          executionStatus = runWithLimitedTime(tmpFolderName, timesToExecute, timeLimit, mType, false);
        }

        EParameter executionStatusParameter;
        switch (executionStatus) {
          case STATUS_OK:
            notificator.notify(testID, ExecutionStatus.DONE);
            executionStatusParameter = EResultDescription.getExecutionStatusParameter(ExecutionStatus.DONE);
            break;
          case PROCESS_KILLED:
            notificator.notify(testID, ExecutionStatus.KILLED);
            executionStatusParameter = EResultDescription.getExecutionStatusParameter(ExecutionStatus.KILLED);
            break;
          default:
            notificator.notify(testID, ExecutionStatus.FAILED);
            executionStatusParameter = failedErr;
        }
        
        algResultParams = new ParameterSet();
        
        if (executionStatus == ErrorStatus.STATUS_OK) { // the execution passed normaly (not killed)
          resultAlg = getAlgorithmFromFile(tmpFolderName, SER_ALG_TYPE.RESULT);

          if (resultAlg != null) {
            algResultParams = resultAlg.done();

            switch (mType) {
              case EM:
                algResultParams.addParameters(getTimeParameters(resultDesc, resultAlg), true);
                break;
              case CNT:
                algResultParams.addParameters(getCounterParameters(resultDesc, resultAlg), true);
                break;
            }
          } else {
            algResultParams.addParameter(EResultDescription.getErrorParameter("Invalid test: "+testCase.toString()), true);
            executionStatusParameter = failedErr;
          }
        } else { // the execution did not perform succesfully          
          if (executionStatus == ErrorStatus.PROCESS_KILLED) {
            algResultParams.addParameter(EResultDescription.getErrorParameter(
              String.format("Process killed after %d seconds.", timeLimit)), true);
          } else {
            algResultParams.addParameter(EResultDescription.getErrorParameter(ErrorStatus.getLastErrorMessage()), true);
          }
        }
        
        algResultParams.addParameter(algP, true);
        algResultParams.addParameter(tsP, true);
        algResultParams.addParameter(EResultDescription.getTestIDParameter("Test-" + testID), true);                        
        algResultParams.addParameter(executionStatusParameter, true);
        
        PrintWriter pw = new PrintWriter(new FileWriter(resultFile, true));
          pw.println(algResultParams.toString(order, false, delim));
        pw.close();        

        ATGlobal.deleteTMPDir(tmpFolderName, project.getName());        
      }
      it.close();
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_RUN, e.toString());
    }

    ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK, "");
  }

  // has the process finished?
  private static boolean processIsTerminated(Process process) {
    try {
      process.exitValue();
    } catch (IllegalThreadStateException itse) {
      return false;
    }
    return true;
  }

  /**
   * Method runs a given algorithm (algorithm's serialized file is written in
   * foldername) for n times (where n is one of the paramters in the algorithm's
   * testcase) and returns null if each execution finished in
   * timeForOneExecutionTime and errorMessage otherwise
   */
  static ErrorStatus runWithLimitedTime(String folderName, int timesToExecute,
          long timeForOneExecution, MeasurementType mType, boolean verbose) {

    Object result = ExternalExecute.runWithExternalJVM(folderName, mType, verbose);

    // during the process creation, an error occured
    if (result instanceof String) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.PROCESS_CANT_BE_CREATED, (String) result);
    }

    if (!(result instanceof Process)) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR.ERROR, "?unknown?");
    }

    Process externProcess = (Process) result;

    long milis = System.currentTimeMillis();
    whileloop:
    while (true) {
      // loop for one second
      for (int i = 0; i < 10; i++) {
        if (processIsTerminated(externProcess)) {
          break whileloop;
        }
        // wait for 0.1s
        try {
          Thread.sleep(100);
        } catch (Exception e) {
        }
      }

      long resultsCount = getCommunicationCount(folderName);
      long secondsPassed = (System.currentTimeMillis() - milis) / 1000;

      int expectedResults = (int) (secondsPassed / timeForOneExecution);
      if (resultsCount < expectedResults) {
        externProcess.destroy();
        return ErrorStatus.setLastErrorMessage(ErrorStatus.PROCESS_KILLED,
                String.format("(after %d sec.)", (int) secondsPassed));
      }
    }

    try {
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(externProcess.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(externProcess.getErrorStream()));

      String s;
      StringBuffer sb = new StringBuffer();
      while ((s = stdInput.readLine()) != null) {
        sb.append(s);
      }
      while ((s = stdError.readLine()) != null) {
        sb.append(s);
      }

      if (sb.length() != 0) {
        return ErrorStatus.setLastErrorMessage(ErrorStatus.PROCESS_CANT_BE_CREATED, sb.toString());
      } else {
        return ErrorStatus.STATUS_OK;
      }

    } catch (Exception e) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.PROCESS_CANT_BE_CREATED, e.toString());
    }
  }

  // pregledam resultDesc parametre in za vsak parameter tipa "timer" ustvarim
  // parameter v results s pravo vrednostj
  static ParameterSet getTimeParameters(EResultDescription resultDesc, AbsAlgorithm algorithm) {
    ParameterSet timeParameters = new ParameterSet();
    long[][] times = algorithm.getExecutionTimes();

    if (resultDesc != null) {
      ParameterSet pSet = resultDesc.getParameters();
      for (int i = 0; i < resultDesc.getParameters().size(); i++) {
        EParameter rdP = pSet.getParameter(i);
        if (ParameterType.TIMER.equals(rdP.getType())) {
          String[] subtypeFields;
          try {
            String subtype = rdP.getSubtype();
            subtypeFields = subtype.split(" ");
            if (subtypeFields.length != 2) {
              throw new Exception("subtype: " + subtype);
            }

            int tID = Integer.parseInt(subtypeFields[0]);
            if (tID < 0 || tID > Timer.MAX_TIMERS - 1) {
              throw new Exception("Timer ID not in [0...MAX_TIMERS] " + subtype);
            }

            StatFunction fs = StatFunction.getStatFunction(subtypeFields[1]);
            if (fs.equals(StatFunction.UNKNOWN)) {
              throw new Exception("Invalid function: " + subtype);
            }

            // times[tID] -> ArrayList<Long> (list)
            Long[] longObjects = ArrayUtils.toObject(times[tID]);
            ArrayList<Long> list = new ArrayList<>(java.util.Arrays.asList(longObjects));

            Long time = (Long) StatFunction.getFunctionValue(fs, list);
            EParameter timeP = new EParameter(
                    (String) rdP.getField(Entity.ID_NAME), null, ParameterType.TIMER, time);
            timeParameters.addParameter(timeP, true);
          } catch (Exception e) {
            ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "Subtype parameter invalid (" + e.toString() + ")");
          }
        }
      }
    }
    return timeParameters;
  }

  static ParameterSet getCounterParameters(EResultDescription resultDesc, AbsAlgorithm algorithm) {
    ParameterSet counterParameters = new ParameterSet();
    HashMap<String, Integer> counters = algorithm.getCounters();
    if (resultDesc != null && counters != null) {
      ParameterSet pSet = resultDesc.getParameters();
      for (int i = 0; i < pSet.size(); i++) {
        if (ParameterType.COUNTER.equals(pSet.getParameter(i).getType())) {
          String counterName = (String) pSet.getParameter(i).getField(Entity.ID_NAME);
          int value = 0;
          if (counters.containsKey(counterName)) {
            value = counters.get(counterName);
          }
          counterParameters.addParameter(new EParameter(counterName, null, null, value), true);
        }
      }
    }
    return counterParameters;
  }

  /**
   * Saves the measurement type, classpath and algotihm instance to a file.
   */
  public static boolean saveAlgToFile(URL[] urls, AbsAlgorithm curAlg,
          String folderName, SER_ALG_TYPE algType) {
    try (FileOutputStream fis = new FileOutputStream(new File(folderName + File.separator + SERIALIZED_ALG_FILENAME + algType));
            ObjectOutputStream dos = new ObjectOutputStream(fis);) {
      dos.writeObject(urls);
      dos.writeObject(curAlg);

      return true;
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.CANT_WRITE_ALGORITHM_TO_FILE, e.toString());
      return false;
    }
  }

  //need to do add path to Classpath with reflection since the URLClassLoader.addURL(URL url) method is protected:
  static void addPath(URL s) throws Exception {
    URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<URLClassLoader> urlClass = URLClassLoader.class;
    Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
    method.setAccessible(true);
    method.invoke(urlClassLoader, new Object[]{s});
  }

  public static AbsAlgorithm getAlgorithmFromFile(String folderName, SER_ALG_TYPE algType) {
    try (FileInputStream fis = new FileInputStream(new File(folderName + File.separator + SERIALIZED_ALG_FILENAME + algType));
            ObjectInputStream ois = new ObjectInputStream(fis);) {
      // get the URLs that were used to load algorithm ...
      Object o = ois.readObject();
      URL[] urls = (URL[]) o;
      // ... and add these URLS to URLClassLoader
      if (urls != null) {
        for (URL url : urls) {
          addPath(url);
        }
      }

      // Try to instantiate the algorithm
      o = ois.readObject();
      return (AbsAlgorithm) o;
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.CANT_READ_ALGORITHM_FROM_FILE, e.toString());
      return null;
    }
  }

  public static URL[] getURLsFromFile(String folderName, SER_ALG_TYPE algType) {
    try (FileInputStream fis = new FileInputStream(new File(folderName + File.separator + SERIALIZED_ALG_FILENAME + algType));
            ObjectInputStream ois = new ObjectInputStream(fis);) {
      // get the URLs that were used to load algorithm ...
      Object o = ois.readObject();
      return (URL[]) o;
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.CANT_READ_ALGORITHM_FROM_FILE, e.toString());
      return null;
    }
  }

  /**
   * This method clears the contents of the communication file
   *
   * @param folderName
   */
  public static boolean initCommunicationFile(String folderName) {
    File f = new File(folderName + File.separator + COMMUNICATION_FILENAME);
    try (FileWriter fw = new FileWriter(f)) {
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static void addToCommunicationFile(String folderName) {
    File f = new File(folderName + File.separator + COMMUNICATION_FILENAME);

    try (FileWriter fw = new FileWriter(f, true)) {
      fw.write((byte) 0);
    } catch (Exception e) {
    }
  }

  public static long getCommunicationCount(String folderName) {
    File f = new File(folderName + File.separator + COMMUNICATION_FILENAME);
    try {
      return f.length();
    } catch (Exception e) {
      return 0;
    }
  }

}
