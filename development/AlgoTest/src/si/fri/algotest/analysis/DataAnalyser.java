package si.fri.algotest.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EProject;
import si.fri.algotest.entities.EQuery;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.NameAndAbrev;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;

/**
 *
 * @author tomaz
 */
public class DataAnalyser {

  /**
   * Method reads the results from files from cartesian product (algorithms x
   * testsets) and creates for each algorithm an ArayList of results that
   * includes all testsets. The result is a HashMap in which the algorithm name
   * is a key and a corresponding arraylist is a value.
   */
  public static HashMap<String, ArrayList<ParameterSet>> readResultsFromFiles(EProject project, String[] algorithms, String[] testsets) {
    if (project == null) {
      return null;
    }

    String resDescFilename = ATGlobal.getRESULTDESCfilename(
	    project.getProjectRootDir(), project.getName(), MeasurementType.EM);
    EResultDescription eResultDesc = new EResultDescription(new File(resDescFilename));

    HashMap<String, ArrayList<ParameterSet>> algResults = new HashMap();

    String delim = eResultDesc.getField(EResultDescription.ID_Delim);
    if (delim == null || delim.isEmpty()) {
      delim = ";";
    }

    ParameterSet resultPS = eResultDesc.getParameters();
    String[] testOrder = eResultDesc.getStringArray(EResultDescription.ID_TestParOrder);
    String[] resultOrder = eResultDesc.getStringArray(EResultDescription.ID_ResultParOrder);

    for (String alg : algorithms) {
      ArrayList<ParameterSet> oneAlgResults = new ArrayList();
      for (String tst : testsets) {
	String resFileName = ATGlobal.getRESULTfilename(project.getProjectRootDir(), alg, tst, MeasurementType.EM);
	File resFile = new File(resFileName);
	if (resFile.exists()) {
	  try (Scanner sc = new Scanner(resFile)) {
	    while (sc.hasNextLine()) {
	      String line = sc.nextLine();
	      if (line == null) {
		line = "";
	      }

	      String[] lineFields = line.split(delim);

	      // initializes algoritthms parameterset with "empty" parameters
	      ParameterSet algPS = new ParameterSet(resultPS);

	      // sets the value of default parameters
	      algPS.getParamater(EResultDescription.algParName).set(EParameter.ID_Value, alg);
	      algPS.getParamater(EResultDescription.tstParName).set(EParameter.ID_Value, tst);


	      int lineFiledsPos = EResultDescription.FIXNUM;

	      // sets the value of test parameters
	      for (int i = 0; i < testOrder.length; i++) {
		EParameter tP = algPS.getParamater(testOrder[i]);
		if (tP != null && lineFiledsPos < lineFields.length) {
		  tP.set(EParameter.ID_Value, lineFields[lineFiledsPos++]);
		}
	      }

	      // sets the value of result parameters
	      for (int i = 0; i < resultOrder.length; i++) {
		EParameter tP = algPS.getParamater(resultOrder[i]);
		if (tP != null && lineFiledsPos < lineFields.length) {
		  tP.set(EParameter.ID_Value, lineFields[lineFiledsPos++]);
		}
	      }

	      oneAlgResults.add(algPS);
	    }

	    algResults.put(alg, oneAlgResults);
	  } catch (Exception e) {
	    ATLog.log("Can't read results: " + e);
	    return null;
	  }
	}
      }
    }
    return algResults;
  }

  /**
   * for all Parameters in filter, method checks the corresponding parameter in
   * the result; if the values doesn't match, return false, else return true
   *
   * @return
   */
  public static boolean parametersMatchFilter(ParameterSet params, ParameterSet filter) {
    if (filter == null) {
      return true;
    }

    try {
      for (int i = 0; i < filter.size(); i++) {
	EParameter refPar = filter.getParameter(i);
	Object refVal = refPar.get(EParameter.ID_Value);

	EParameter param = params.getParamater((String) refPar.getField(EParameter.ID_Name));
	Object value = param.get(EParameter.ID_Value);

	if (!value.equals(refVal)) {
	  return false;
	}
      }
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  /**
   * Method selects among all the data given in algResults only those lines that
   * meet given condidiotn.
   *
   * @param filter in the result returned by this method only the lines with the
   * value of fileds equal to the values in this parameterset will be perserved;
   * i.e. for all Parameters in filter method checks the corresponding parameter
   * in the result; if the values doesn't match, result will be skipped
   */
  public static ArrayList<ParameterSet> selectData(HashMap<String, ArrayList<ParameterSet>> algResults, ParameterSet join,
	  ParameterSet filter, ParameterSet testFields, ParameterSet resultFields) {

    ArrayList<ParameterSet> result = new ArrayList();
    Set<String> algKeys = algResults.keySet();

    for (String alg : algKeys) {
      for (ParameterSet ps : algResults.get(alg)) {
	if (parametersMatchFilter(ps, filter)) {
	  result.add(ps);
	}
      }
    }
    return result;
  }

  /**
   * Method reads a file results[.ext] (where [.ext] is measurement dependant)
   * and appends results to the resPack. The key of a result in resPack.results
   * is tesset-test. If a result already exists in resPack.results, parameters
   * are appended to this resultset, otherwise a new resultSet is generated and
   * inserted into the map.
   */
  public static void readResults(ResultPack resPack, EProject project,
	  String algorithm, String testset, MeasurementType measurement) {

    if (project == null || resPack == null) {
      return;
    }

    String resDescFilename = ATGlobal.getRESULTDESCfilename(project.getProjectRootDir(), project.getName(), measurement);
    EResultDescription eResultDesc = new EResultDescription(new File(resDescFilename));

    String delim = eResultDesc.getField(EResultDescription.ID_Delim);
    if (delim == null || delim.isEmpty()) {
      delim = ";";
    }

    ParameterSet resultPS = eResultDesc.getParameters();
    String[] testOrder = eResultDesc.getStringArray(EResultDescription.ID_TestParOrder);
    String[] resultOrder = eResultDesc.getStringArray(EResultDescription.ID_ResultParOrder);

    String resFileName = ATGlobal.getRESULTfilename(project.getProjectRootDir(), algorithm, testset, measurement);
    File resFile = new File(resFileName);

    if (resFile.exists()) {

      // add the current resultdescription parameters to the resPack resultDescription parameterset

      resPack.resultDescription.getParameters().addParameters(eResultDesc.getParameters(), false);

      try (Scanner sc = new Scanner(resFile)) {
	while (sc.hasNextLine()) {
	  String line = sc.nextLine();
	  if (line == null) {
	    line = "";
	  }

	  String[] lineFields = line.split(delim);

	  String testName = lineFields[2];

	  // initializes algorithms parameterset with "empty" parameters
	  ParameterSet algPS = new ParameterSet(resultPS);

	  // sets the value of default parameters
	  algPS.getParamater(EResultDescription.algParName).set(EParameter.ID_Value, algorithm);
	  algPS.getParamater(EResultDescription.tstParName).set(EParameter.ID_Value, testset);
	  algPS.getParamater(EResultDescription.testIDParName).set(EParameter.ID_Value, testName);

	  int lineFiledsPos = EResultDescription.FIXNUM;

	  // sets the value of test parameters
	  for (int i = 0; i < testOrder.length; i++) {
	    EParameter tP = algPS.getParamater(testOrder[i]);
	    if (tP != null && lineFiledsPos < lineFields.length) {
	      tP.set(EParameter.ID_Value, lineFields[lineFiledsPos++]);
	    }
	  }

	  // sets the value of result parameters
	  for (int i = 0; i < resultOrder.length; i++) {
	    EParameter tP = algPS.getParamater(resultOrder[i]);
	    if (tP != null && lineFiledsPos < lineFields.length) {
	      tP.set(EParameter.ID_Value, lineFields[lineFiledsPos++]);
	    }
	  }

	  // add this ParameterSet to the ResultPack
	  String key = testset + "-" + testName;
	  ParameterSet ps = resPack.getResult(key);
	  // If ParameterSet for this testset-test doesn't exist ...
	  if (ps == null) {
	    // ... append a new testset to the map
	    resPack.putResult(key, algPS);
	  } else {
	    // ... otherwise, add new parameters to existing parameterset
	    ps.addParameters(algPS, false);
	  }
	}
      } catch (Exception e) {
	ATLog.log("Can't read results: " + e);
      }
    }
  }

  public static TableData runQuery(EProject project, EQuery query) {
    TableData td = new TableData();
    
    NameAndAbrev[] algs = query.getNATabFromJSONArray(EQuery.ID_Algorithms);
    NameAndAbrev[] testsets = query.getNATabFromJSONArray(EQuery.ID_TestSets);
    
    if (algs == null || algs.length < 1 || testsets == null || testsets.length < 1)
      return td;

    //TODO: dodati je treba measurement v query

    HashMap<String, ResultPack> results = new HashMap<>();

    for (NameAndAbrev alg : algs) {
      ResultPack rPack = new ResultPack();
      for (NameAndAbrev ts : testsets) {
	readResults(rPack, project, alg.getName(), ts.getName(), MeasurementType.EM);
      }
      results.put(alg.getName(), rPack);
    }

    NameAndAbrev[] inPars = query.getNATabFromJSONArray(EQuery.ID_inParameters);
    NameAndAbrev[] outPars = query.getNATabFromJSONArray(EQuery.ID_outParameters);

    // the order of testset-test key is obtained trom the first algorithm (this order
    // should be the same for all algorithms, therefore the selection of the algorithms 
    // is arbitrary). 
    ArrayList<String> keyOrder = results.get(algs[0].getName()).keyOrder;


    for (NameAndAbrev inPar : inPars) {
      td.header.add(inPar.getAbrev());
    }
    for (NameAndAbrev outPar : outPars) {
      for (NameAndAbrev alg : algs) {
	td.header.add(alg.getAbrev() + "." + outPar.getAbrev());
      }
    }

    for (String key : keyOrder) {
      ArrayList<Object> line = new ArrayList<>();

      String alg1Name = algs[0].getName();
      for (NameAndAbrev inPar : inPars) {
	Object value;
	try {
	  ParameterSet ps = results.get(alg1Name).getResult(key);
	  EParameter parameter = ps.getParamater(inPar.getName());
	  value =  parameter.get(EParameter.ID_Value);
	} catch (Exception e) {
	  value = "?";
	}
	line.add(value);
      }

      // scans outParams and find its value for every algorithm-testset-test
      for (NameAndAbrev outPar : outPars) {
	for (NameAndAbrev alg : algs) {
	  Object value;
	  try {
	    ParameterSet ps = results.get(alg.getName()).getResult(key);
	    EParameter parameter = ps.getParamater(outPar.getName());
	    value = parameter.get(EParameter.ID_Value);
	  } catch (Exception e) {
	    value = "?";
	  }
	  line.add(value);
	}
      }
      td.data.add(line);
    }
    
    String [] groupby = query.getStringArray(EQuery.ID_GroupBy);
    for (int i = 0; i < groupby.length; i++) {
      td.groupBy(groupby[i]); 
    }
    
    
    String [] sortby = query.getStringArray(EQuery.ID_SortBy);
    for (int i = 0; i < sortby.length; i++) {
      td.sort(sortby[i]); 
    }
    
    return td;
  }

  /* ****************************************************** */
  /*             TESTS                                      */
  /*                                                        */
  public static void scanQuery(EProject project, EQuery query) {
    NameAndAbrev[] algs = query.getNATabFromJSONArray(EQuery.ID_Algorithms);
    NameAndAbrev[] testsets = query.getNATabFromJSONArray(EQuery.ID_TestSets);



    String projRoot = project.getProjectRootDir();

    for (NameAndAbrev alg : algs) {
      for (NameAndAbrev ts : testsets) {
	String resultFile = ATGlobal.getRESULTfilename(projRoot, alg.getName(), ts.getName(), MeasurementType.EM);
	System.out.printf("%s-%s\n", alg.getName(), ts.getName());
	try (Scanner sc = new Scanner(new File(resultFile))) {
	  while (sc.hasNextLine()) {
	    System.out.println(sc.nextLine());
	  }
	} catch (Exception e) {
	}
      }
    }
  }

  public static void main(String args[]) {
    String root = ATGlobal.ALGatorDataRoot;
    String projName = "Sorting";

    String projRoot = ATGlobal.getPROJECTroot(root, projName);
    String projFilename = ATGlobal.getPROJECTfilename(root, projName);

    EProject eProject = new EProject(new File(projFilename));
    EQuery query = new EQuery(new File(ATGlobal.getQUERYfilename(projRoot, "q1")));

    // scanQuery(eProject, query);

    ResultPack rPack = new ResultPack();
    readResults(rPack, eProject, "QuickSort", "TestSet1", MeasurementType.EM);
    readResults(rPack, eProject, "QuickSort", "TestSet2", MeasurementType.EM);
    System.out.println(rPack);

    runQuery(eProject, query);
  }
}
