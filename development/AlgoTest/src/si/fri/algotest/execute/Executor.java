package si.fri.algotest.execute;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils;
import si.fri.algotest.entities.EAlgorithm;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EProject;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.StatFunction;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.timer.Timer;
import si.fri.algotest.tools.ATTools;
import static si.fri.algotest.tools.ATTools.compile;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public class Executor {

  public static ArrayList<ParameterSet> iterateTestSetAndRunAlgorithm
	  (AbsAlgorithm algorithm, AbstractTestSetIterator it, EResultDescription resultDesc, Notificator notificator) {
    
    ArrayList<ParameterSet> parameterSets = new ArrayList();
        
    Timer timer = new Timer();

    AbsAlgorithm curAlg;
    TestCase testCase = null;  
    ParameterSet result;

    int timesToExecute = 1;
    try {
      ETestSet testSet = it.testSet;
      timesToExecute =  (Integer) testSet.getField(ETestSet.ID_TestRepeat);
    } catch (Exception e) {
      System.out.println(e);
    }
    if (timesToExecute <= 0) timesToExecute = 1;
    
    int tsID = 0;
    try {    
      while (it.hasNext()) {
	it.readNext();
	
	tsID++; notificator.notify(tsID);
      
	long [][] times = new long[Timer.MAX_TIMERS][timesToExecute];
	
	// true if algorithm passes the execution, false otherwise
	boolean executionOK = true;
	curAlg = null;
	
	// run the test timesToExecute-times and save time to the times[] array
	for (int i = 0; i < timesToExecute; i++) {
	   
	  testCase = it.getCurrent();
	  
	  //!!! tale clone morda ni OK! (čeprav se mi zdi, da je)
	  // Ugotovi: Kaj vse mora narediti avtor algoritma in avtor projekta, da bo
	  // tak clone gotovo prav naredil globoko kopijo algoritma???
	  curAlg = (AbsAlgorithm) algorithm.clone();
	
	  ErrorStatus err = curAlg.init(testCase);

  	  if (err != ErrorStatus.STATUS_OK) {
	    executionOK = false;
	    break;
	  } else {
	    
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
	  
	  
	  // pregledam resultDesc parametre in za vsak parameter tipa "timer" ustvarim
	  // parameter v results s pravo vrednostj
	  if (resultDesc != null) {
	    ParameterSet pSet = resultDesc.getParameters(); 
	    for(int i=0; i<resultDesc.getParameters().size();i++) {
	      EParameter rdP = pSet.getParameter(i);
	      if (ParameterType.TIMER.equals(rdP.getType())) {
		String [] subtypeFields;
		try {
		  String subtype = rdP.getSubtype();
		  subtypeFields = subtype.split(" ");
		  if (subtypeFields.length != 2)
		    throw new Exception("subtype: " + subtype);
		  
		  int tID = Integer.parseInt(subtypeFields[0]);
		  if (tID < 0 || tID > Timer.MAX_TIMERS - 1)
		    throw new Exception("Timer ID not in [0...MAX_TIMERS] " +  subtype);
		  
		  StatFunction fs =StatFunction.getStatFunction(subtypeFields[1]);
		  if (fs.equals(StatFunction.UNKNOWN))
		    throw new Exception("Invalid function: " +  subtype);

                  
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
    String algTPL = projekt.getField(EProject.ID_AlgorithmTPL);
    String testCase = projekt.getField(EProject.ID_TestCaseClass);
    String tsIterator = projekt.getField(EProject.ID_TestSetIteratorClass);


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
  public static ErrorStatus algorithmMakeCompile(EProject eProjekt, EAlgorithm eAlgorithm, boolean alwaysCompile) {
    String projRoot = eProjekt.getProjectRootDir();

    String projBin = ATGlobal.getPROJECTbin(projRoot);

    String algName = eAlgorithm.getName();

    String algSrc = ATGlobal.getALGORITHMsrc(projRoot, algName);
    String algBin = ATGlobal.getALGORITHMbin(projRoot, algName);
    String algClass = eAlgorithm.getField(EAlgorithm.ID_Classes);

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
  public static ErrorStatus algorithmRun(String projectsRoot, String projName, String algName,
	  String testSetName, Notificator notificator, boolean alwaysCompile, boolean alwaysRun) {
    String projRoot = ATGlobal.getPROJECTroot(projectsRoot, projName);
    String projFilename = ATGlobal.getPROJECTfilename(projectsRoot, projName);
    String projBin = ATGlobal.getPROJECTbin(projRoot);

    String algFilename = ATGlobal.getALGORITHMfilename(projRoot, algName);
    String algBin = ATGlobal.getALGORITHMbin(projRoot, algName);

    String testSetFilename = ATGlobal.getTESTSETfilename(projRoot, testSetName);

    String resultDescriptionFilename = ATGlobal.getRESULTDESCfilename(projRoot,projName, MeasurementType.EM);

    EProject eProject = new EProject(new File(projFilename));
    if (!ErrorStatus.getLastErrorStatus().isOK()) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_PROJECT, "");
    }

    EResultDescription eResDesc = new EResultDescription(new File(resultDescriptionFilename));
    if (!ErrorStatus.getLastErrorStatus().isOK()) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_RESULTDESCRIPTION, "");
    }
    
    
    EAlgorithm eAlgorithm = new EAlgorithm(new File(algFilename));
    if (!ErrorStatus.getLastErrorStatus().isOK()) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_ALGORITHM, "");
    }
    
    ETestSet eTestSet = new ETestSet(new File(testSetFilename));
    if (!ErrorStatus.getLastErrorStatus().isOK()) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_TESTSET, "");
    }
    
    int numberOfInstances = eTestSet.getField(ETestSet.ID_N);
    boolean resultsAreUptodate = ATTools.resultsAreUpToDate(projRoot, algName, testSetName, numberOfInstances);
    if (!alwaysRun && resultsAreUptodate) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK,
	      String.format("Running test set '%s' - nothing to be done.", testSetName));
    }

    if (projectMakeCompile(eProject, alwaysCompile) != ErrorStatus.STATUS_OK) {
      return ErrorStatus.getLastErrorStatus();
    }

    if (algorithmMakeCompile(eProject, eAlgorithm, alwaysCompile) != ErrorStatus.STATUS_OK) {
      return ErrorStatus.getLastErrorStatus();
    }

    String algClassName = eAlgorithm.getField(EAlgorithm.ID_Classes);
    String testSetIteratorClassName = eProject.getField(EProject.ID_TestSetIteratorClass);

    try {
      URLClassLoader parentclassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
      URL[] parentURLs = parentclassLoader.getURLs();

      URL[] urls = new URL[parentURLs.length + 2];
      for (int i = 0; i < urls.length - 2; i++) {
	urls[i] = parentURLs[i];
      }
      urls[urls.length - 2] = new File(projBin).toURI().toURL();
      urls[urls.length - 1] = new File(algBin).toURI().toURL();


      URLClassLoader classLoader = URLClassLoader.newInstance(urls);

      Class algClass = Class.forName(algClassName, true, classLoader);
      AbsAlgorithm algInstance = (AbsAlgorithm) algClass.newInstance();

      Class tsClass = Class.forName(testSetIteratorClassName, true, classLoader);
      AbstractTestSetIterator tsInstance = (AbstractTestSetIterator) tsClass.newInstance();
      tsInstance.setTestSet(eTestSet);

      notificator.setNumberOfInstances(numberOfInstances);

      // the order of parameters to be printed
      String [] order = eResDesc.getParamsOrder();
      String delim = eResDesc.getField(EResultDescription.ID_Delim);
         
      ArrayList<ParameterSet> resultParameterSets = iterateTestSetAndRunAlgorithm(algInstance, tsInstance, eResDesc, notificator);
      
      if (ErrorStatus.getLastErrorStatus().isOK()) {
	String resFilename = ATGlobal.getRESULTfilename(projRoot, algName, testSetName, MeasurementType.EM);
	PrintWriter pw = new PrintWriter(resFilename);
	for (ParameterSet parameterSet : resultParameterSets) {
	  parameterSet.addParameter(EResultDescription.getAlgorithmNameParameter(algName), true);
	  parameterSet.addParameter(EResultDescription.getTestsetNameParameter(testSetName), true); 
	  pw.println(parameterSet.toString(order, false, delim));
	}
	pw.close();
	return ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK,
		String.format("Running test set '%s' with algorithm '%s' - done.", testSetName, algName));
      } else // execution failed 
      {
	throw new Exception(ErrorStatus.getLastErrorMessage());
      }
    } catch (Exception e) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_RUN, e.toString());
    }
  }
}
