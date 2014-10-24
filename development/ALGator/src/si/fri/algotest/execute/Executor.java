package si.fri.algotest.execute;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import si.fri.algotest.entities.EAlgorithm;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EProject;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.Project;
import si.fri.algotest.entities.StatFunction;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.timer.Timer;
import si.fri.algotest.tools.ATTools;
import static si.fri.algotest.tools.ATTools.compile;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public class Executor {

  public static ArrayList<ParameterSet> iterateTestSetAndRunAlgorithm(Project project, String algName, AbstractTestSetIterator it, EResultDescription resultDesc,
          Notificator notificator, MeasurementType mType) {

    ArrayList<ParameterSet> parameterSets = new ArrayList();

    Timer timer = new Timer();

    AbsAlgorithm curAlg;
    TestCase testCase = null;
    ParameterSet result;

    int timesToExecute = 1;
    // for EM type of execution timesToExecute may be more than 1
    if (mType.equals(MeasurementType.EM)) {
      try {
        ETestSet testSet = it.testSet;
        timesToExecute = Integer.parseInt((String) testSet.getField(ETestSet.ID_TestRepeat));
      } catch (Exception e) {
        System.out.println(e);
      }
    }
    if (timesToExecute <= 0) {
      timesToExecute = 1;
    }

    int tsID = 0;
    try {
      while (it.hasNext()) {
        it.readNext();

        tsID++;
        notificator.notify(tsID);

        long[][] times = new long[Timer.MAX_TIMERS][timesToExecute];

        // true if algorithm passes the execution, false otherwise
        boolean executionOK = true;
        curAlg = null;

        // run the test timesToExecute-times and save time to the times[] array
        for (int i = 0; i < timesToExecute; i++) {

          testCase = it.getCurrent();

          curAlg = New.algorithmInstance(project, algName, mType);

          ErrorStatus err = curAlg.init(testCase);

          if (err != ErrorStatus.STATUS_OK) {
            executionOK = false;
            break;
          } else {

            Counters.resetCounters();
            
            //!!! TODO
            // Test should be run in a separate thread with limited time available. 
            // If time is exceeded set PASS=KILLED and return form the method! 
            timer.start();
              curAlg.run();
            timer.stop();

            for (int tID = 0; tID < Timer.MAX_TIMERS; tID++) {
              times[tID][i] = timer.time(tID);
            }
          }
        }
        if (executionOK && curAlg != null) {
          result = curAlg.done();

          result.addParameter(EResultDescription.getPassParameter(true), true);

          switch (mType) {
            case EM:
              // pregledam resultDesc parametre in za vsak parameter tipa "timer" ustvarim
              // parameter v results s pravo vrednostj
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
                              (String) rdP.getField(EParameter.ID_Name), null, null, time);
                      result.addParameter(timeP, true);
                    } catch (Exception e) {
                      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "Subtype parameter invalid (" + e.toString() + ")");
                    }
                  }
                }
              }
              break;
            case CNT:
              if (resultDesc != null) {
                ParameterSet pSet = resultDesc.getParameters();
                for (int i = 0; i < pSet.size(); i++) {                  
                  if (ParameterType.COUNTER.equals(pSet.getParameter(i).getType())) {
                    String counterName = (String) pSet.getParameter(i).getField(EParameter.ID_Name);
                    int value = Counters.getCounterValue(counterName);
                    result.addParameter(new EParameter(counterName , null, null, value), true);
                  }
                   
                }
              }
              break;
          }
        } else {
          result = new ParameterSet();
          EParameter p1 = new EParameter("[Error]: invalid test", "Dataset can not be executed.", ParameterType.STRING, testCase.toString());
          result.addParameter(p1, true);
        }
        parameterSets.add(result);
      }
      it.close();
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_RUN, e.toString());
    }

    ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK, "");
    return parameterSets;
  }

  /**
   * Compares the creation date of the files at bin directory and compiles the
   * sources if required (bin files are older than src files or bin files are
   * missing). If alwaysCompile==true, the verification of the date is omitted
   * (the compilations takes place although the classes already exist and are up
   * to date).
   */
  public static ErrorStatus projectMakeCompile(EProject projekt, boolean alwaysCompile) {
    String projRoot = projekt.getProjectRootDir();

    // the classes to be compiled
    String algTPL     = ATTools.stripFilenameExtension((String)projekt.getField(EProject.ID_AlgorithmClass));
    String testCase   = ATTools.stripFilenameExtension((String)projekt.getField(EProject.ID_TestCaseClass));
    String tsIterator = ATTools.stripFilenameExtension((String)projekt.getField(EProject.ID_TestSetIteratorClass));

    // java src and bin dir
    String projSrc = ATGlobal.getPROJECTsrc(projRoot);
    String projBin = ATGlobal.getPROJECTbin(projRoot);

    String missingSource = ATTools.sourcesExists(projSrc, new String[]{algTPL, testCase, tsIterator});
    if (missingSource != null) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_SOURCES_DONT_EXIST, missingSource);
    }

    // compare the creation date of the files
    if (!alwaysCompile && !ATTools.isSourceNewer(projSrc, projBin, new String[]{algTPL, testCase, tsIterator})) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK,
              String.format("Compile project '%s' - nothing to be done.", projekt.getName()));
    }

    ErrorStatus err = compile(projSrc, new String[]{algTPL + ".java", testCase + ".java", tsIterator + ".java"},
            projBin, new String[]{}, String.format("project '%s'", projekt.getName()));

    return ErrorStatus.setLastErrorMessage(err, "");
  }

  /**
   * Compares the creation date of the files at src and bin folder and compiles
   * the sources if required (bin files are older than src files or bin files
   * are missing). If alwaysCompile==true, the verification of the date is
   * omitted (the compilations takes place although the classes already exist
   * and are up to date).
   */
  public static ErrorStatus algorithmMakeCompile(EProject eProjekt, EAlgorithm eAlgorithm, MeasurementType mType, boolean alwaysCompile) {
    String projRoot = eProjekt.getProjectRootDir();

    String projBin = ATGlobal.getPROJECTbin(projRoot);

    String algName = eAlgorithm.getName();

    String algSrc = ATGlobal.getALGORITHMsrc(projRoot, algName);
    String algBin = ATGlobal.getALGORITHMbin(projRoot, algName);
    String algClass = eAlgorithm.getField(EAlgorithm.ID_MainClassName);

    if (mType.equals(MeasurementType.CNT)) {
      testAndCreateCOUNTClass(algSrc, algClass);
      algClass += ATGlobal.COUNTER_CLASS_EXTENSION;
    }

    // test for sources
    String missingSource = ATTools.sourcesExists(algSrc, new String[]{algClass});
    if (missingSource != null) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_SOURCES_DONT_EXIST, missingSource);
    }

    // compare the creation date of the files
    if (!alwaysCompile && !ATTools.isSourceNewer(algSrc, algBin, new String[]{algClass})) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK,
              String.format("Compiling algorithm  '%s' - nothing to be done.", algName));
    }

    ErrorStatus err = compile(algSrc, new String[]{algClass + ".java"},
            algBin, new String[]{projBin}, String.format("algorithm '%s'", algName));

    return err;
  }

  /**
   * Create a new java class in which all //@COUNT{cnt_name, value} are replaced
   * with Counters.addToCounter("CMP", 1) commands. The new class resides in the
   * same folder as the oroginal class (root).
   */
  static void testAndCreateCOUNTClass(String classRoot, String className) {
    try {
      String newClassName = className + ATGlobal.COUNTER_CLASS_EXTENSION;
      File classFile = new File(classRoot + File.separator + className + ".java");
      File newClassFile = new File(classRoot + File.separator + newClassName + ".java");

      if (newClassFile.exists() && FileUtils.isFileNewer(newClassFile, classFile)) {
        return;
      }

      String classContent = "";
      Scanner sc = new Scanner(classFile);
      while (sc.hasNextLine()) {
        classContent += sc.nextLine() + "\n";
      }
      sc.close();

      classContent = "import si.fri.algotest.execute.Counters;\n" + classContent;
      classContent = classContent.replaceAll(className, newClassName);
      classContent = classContent.replaceAll("//\\@COUNT\\{(.*),(.*)\\}", "Counters.addToCounter(\"$1\", $2);");

      PrintWriter pw = new PrintWriter(newClassFile);
      pw.println(classContent);
      pw.close();
    } catch (Exception e) {
    }
  }

  /**
   * Runs the algorithm over all the tests from the given test set. The method
   * first checks if the if the project and the algorithm has to be compiled. If
   * source files are newer than the classes it compiles the project and/or the
   * algorithm.
   *
   * @param projectsRoot path to the projects folder
   * @param projName the name of the project
   * @param algName the name of the algorithm
   * @param testSetName the name of the test set
   * @param notificator a notificator
   * @param alwaysCompile if true the sources are compiled although the classes
   * are up to date.
   * @param alwaysRun run the algorithm although the results already exist; if
   * algorithm runs sucessfully, the existing results are overwriten.
   * @return
   */
  public static ErrorStatus algorithmRun(Project project, String algName, String testSetName,
          MeasurementType mType, Notificator notificator, boolean alwaysCompile, boolean alwaysRun) {

    if (project == null) {
      return ErrorStatus.ERROR;
    }

    String runningMsg = String.format("Running [%s, %s, %s]", mType.getExtension(), testSetName, algName);
    ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK,runningMsg);
     
    String projRoot = project.getProject().getProjectRootDir();

    EResultDescription eResDesc = project.getResultDescriptions().get(mType);
    if (eResDesc == null) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_RESULTDESCRIPTION, "");
    }

    EAlgorithm eAlgorithm = project.getAlgorithms().get(algName);
    if (eAlgorithm == null) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_ALGORITHM, "");
    }

    ETestSet eTestSet = project.getTestSets().get(testSetName);
    if (eTestSet == null) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_TESTSET, "");
    }

    int numberOfInstances = eTestSet.getFieldAsInt(ETestSet.ID_N);
    boolean resultsAreUptodate = ATTools.resultsAreUpToDate(projRoot, algName, testSetName, numberOfInstances);
    if (!(alwaysRun || alwaysCompile) && resultsAreUptodate) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK, runningMsg +  " - nothing to be done.");
    }

    if (projectMakeCompile(project.getProject(), alwaysCompile) != ErrorStatus.STATUS_OK) {
      return ErrorStatus.getLastErrorStatus();
    }

    if (algorithmMakeCompile(project.getProject(), eAlgorithm, mType, alwaysCompile) != ErrorStatus.STATUS_OK) {
      return ErrorStatus.getLastErrorStatus();
    }

    AbstractTestSetIterator tsInstance = New.testsetIteratorInstance(project, algName);
    tsInstance.setTestSet(eTestSet);

    try {
      notificator.setNumberOfInstances(numberOfInstances);

      // the order of parameters to be printed
      String[] order = eResDesc.getParamsOrder();
      String delim = eResDesc.getField(EResultDescription.ID_Delim);

      ArrayList<ParameterSet> resultParameterSets
              = iterateTestSetAndRunAlgorithm(project, algName, tsInstance, eResDesc, notificator, mType);

      if (ErrorStatus.getLastErrorStatus().isOK()) {
        String resFilename = ATGlobal.getRESULTfilename(projRoot, algName, testSetName, mType);
        PrintWriter pw = new PrintWriter(resFilename);
        for (ParameterSet parameterSet : resultParameterSets) {
          parameterSet.addParameter(EResultDescription.getAlgorithmNameParameter(algName), true);
          parameterSet.addParameter(EResultDescription.getTestsetNameParameter(testSetName), true);
          pw.println(parameterSet.toString(order, false, delim));
        }
        pw.close();
        return ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK, runningMsg + " - done.");
      } else // execution failed 
      {
        throw new Exception(ErrorStatus.getLastErrorMessage());
      }
    } catch (Exception e) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_RUN, e.toString());
    }
  }

  public static void main(String[] args) {
    testAndCreateCOUNTClass("/Users/Tomaz/Dropbox/FRI/ALGator/test_data_root/projects/PROJ-Sorting/algs/ALG-BubbleSort/src", "BubblesortSortAlgorithm");
  }
}
