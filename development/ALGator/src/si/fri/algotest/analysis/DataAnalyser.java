package si.fri.algotest.analysis;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import si.fri.algotest.entities.DeparamFilter;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EProject;
import si.fri.algotest.entities.EQuery;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.Entity;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.NameAndAbrev;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.tools.ATTools;

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
	    ATLog.log("Can't read results: " + e, 1);
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

	EParameter param = params.getParamater((String) refPar.getField(Entity.ID_NAME));
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
    // test parameters are only defined in EM file
    String[] testOrder = eResultDesc.getStringArray(EResultDescription.ID_TestParOrder);
    if (testOrder == null) testOrder = new String[0];
    
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
          String pass     = lineFields[3];

	  // initializes algorithms parameterset with "empty" parameters
	  ParameterSet algPS = new ParameterSet(resultPS);

	  // sets the value of default parameters
	  algPS.getParamater(EResultDescription.algParName).set(EParameter.ID_Value, algorithm);
	  algPS.getParamater(EResultDescription.tstParName).set(EParameter.ID_Value, testset);
	  algPS.getParamater(EResultDescription.testIDParName).set(EParameter.ID_Value, testName);
          algPS.getParamater(EResultDescription.passParName).set(EParameter.ID_Value, pass);

	  int lineFiledsPos = EResultDescription.FIXNUM;

	  // sets the value of test parameters
	  for (int i = 0; i < testOrder.length; i++) {
	    EParameter tP = algPS.getParamater(testOrder[i]);
            
	    if (tP != null)
              if (lineFiledsPos < lineFields.length) 
	        tP.set(EParameter.ID_Value, lineFields[lineFiledsPos]);
              else
                tP.set(EParameter.ID_Value, tP.getType().getDefaultValue()); 
            
	    lineFiledsPos++;
	  }

	  // sets the value of result parameters
	  for (int i = 0; i < resultOrder.length; i++) {
	    EParameter tP = algPS.getParamater(resultOrder[i]);
            
	    if (tP != null)
              if (lineFiledsPos < lineFields.length) 
	        tP.set(EParameter.ID_Value, lineFields[lineFiledsPos]);
              else
                tP.set(EParameter.ID_Value, tP.getType().getDefaultValue());            
            
            lineFiledsPos++;
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
	ATLog.log("Can't read results: " + e, 1);
      }
    }
  }

  public static DeparamFilter [] getDeparamFilters(String [] filter) {
    if (filter == null) return new DeparamFilter[0];
    
    // filters without @()
    String [] clearFilter = new String[filter.length];
    
   
    Pattern pattern = Pattern.compile("(.*)@\\((.*)\\)");
    String range="";
    
    for(int i=0; i<filter.length;i++) {
      String curFilter = filter[i];
      if (filter[i].contains("@(")) {
        Matcher matcher = pattern.matcher(filter[i]);
        if (matcher.find()) {
          curFilter = matcher.group(1);
          range     = matcher.group(2);
        }
      }
      clearFilter[i] = curFilter;  
    }
    
    
    ArrayList<DeparamFilter> deFilterList = new ArrayList<DeparamFilter>();
    if (range.isEmpty()) {
      DeparamFilter deFilter = new DeparamFilter(1, clearFilter);
      deFilterList.add(deFilter);
    } else {
      int iFrom, iTo, iStep;
      String rangeParts [] = range.split(",");
      try {
        iFrom = Integer.valueOf(rangeParts[0]);
        iTo   = Integer.valueOf(rangeParts[1]);
        iStep = Integer.valueOf(rangeParts[2]);
      } catch (Exception e) {
        iFrom = iTo = iStep = 1;
      }
      for(int i=iFrom; i<=iTo; i=i+iStep) {
        String [] thisFilter  = clearFilter.clone();
        for (int iTab = 0; iTab < thisFilter.length; iTab++) {
          thisFilter[iTab] = thisFilter[iTab].replaceAll("\\$1", Integer.toString(i));
        }
        deFilterList.add(new DeparamFilter(i, thisFilter));
      }
    }
    return (DeparamFilter []) deFilterList.toArray(new DeparamFilter[1]);
  }
  
  public static String getFilterHeaderName(String [] filter) {
    return "#";
  }

  
  
  public static String getQueryResultTableAsString(String projectname, String query) {
    return getQueryResultTableAsString(projectname, query, null);
  }
  
  /**
   * Gets the query result as table of values. The input query can be a) query written in json format
   * and b) filename of a query written in a file. If query is json, EQuery is generated and result 
   * is returned: otherwise, if query is filename, method checks the file with name queryName+parameters. 
   * If file exists and is fresh (never than project configuration files), method returns its content, 
   * else it runs a query, writes the result to file and returns the result.
   */
  public static String getQueryResultTableAsString(String projectname, String query, String [] params) {
    if (projectname==null || projectname.isEmpty() || query==null || query.isEmpty()) return "";
    
    if (query.startsWith("{")) {
      EProject project = new EProject(new File(ATGlobal.getPROJECTfilename(ATGlobal.getALGatorDataRoot(), projectname)));  
      EQuery eQuery = new EQuery(query, params);
      return runQuery(project, eQuery).toString();
    } else try {
      String queryname = query;
      String projectRoot = ATGlobal.getPROJECTroot(ATGlobal.getALGatorDataRoot(), projectname);
      String qResultFileName = ATGlobal.getQUERYOutputFilename(projectRoot, queryname, params);            
      HashSet<String> queryDepFiles = ATTools.getFilesForQuery(projectname, queryname, params);
            
      if (ATTools.resultsAreUpToDate(queryDepFiles, qResultFileName)) {
        File qResultFile = new File(qResultFileName);
        String content = "";
        Scanner sc = new Scanner(qResultFile);
        while (sc.hasNextLine()) {
          String vrstica = sc.nextLine();
          content += (content.isEmpty() ? "" : "\n") + vrstica;
        }
        sc.close();
        return content;
      } else {
        TableData td = runQuery(projectname, queryname, params);
        String result = td.toString();
        PrintWriter pw = new PrintWriter(qResultFileName);
        pw.print(result);
        pw.close();
        
        return result;
      }
    } catch (Exception e) {
      return "";
    }
  }
  
  
  public static TableData runQuery(String projectname, String queryname) {
    return runQuery(projectname, queryname, null);
  }   
  public static TableData runQuery(String projectname, String queryname, String [] params) { 
    EProject project = new EProject(new File(ATGlobal.getPROJECTfilename(ATGlobal.getALGatorDataRoot(), projectname)));  
    EQuery   query   = new EQuery(new File(ATGlobal.getQUERYfilename(project.getProjectRootDir(), queryname)), params);
    
    return runQuery(project, query);
  }
  
  /**
   * Methos runs a given query. For NO_COUNT queries it calls  runQuery_NO_COUNT once, while for COUNT queries
   * runQuery_NO_COUNT is called n times (n=number of algorithm selected in query) and the results 
   * are joint into a single tableData 
   */
  public static TableData runQuery(EProject project, EQuery query) {
    if (!query.isCount()) {
      
      return runQuery_NO_COUNT(project,query);
    }
//    else {
//      TableData result = new TableData();
//      result.header.add(EResultDescription.algParName);
//      result.header.add("COUNT");
//      
//      String algorithms [] = query.getStringArray(EQuery.ID_Algorithms);
//      for (String algorithm : algorithms) {
//        String [] enAlgoritemArray = {algorithm};
//        JSONArray enALgoritemJArray = new JSONArray(enAlgoritemArray);
//        query.set(EQuery.ID_Algorithms, enALgoritemJArray);
//        
//        TableData dataForAlg = runQuery_NO_COUNT(project, query);
//        ArrayList line = new ArrayList();
//        line.add(new NameAndAbrev(algorithm).getName());
//        
//        int algCount = (dataForAlg != null && dataForAlg.data != null) ? dataForAlg.data.size() : 0;
//        line.add(algCount);
//        
//        result.data.add(line);
//      }
    else {
      TableData result = new TableData();
      String algorithms [] = query.getStringArray(EQuery.ID_Algorithms);
      
      String [] origQueryFilter = query.getStringArray(EQuery.ID_Filter);
      
      // header
      result.header.add(getFilterHeaderName(origQueryFilter));
      for (String algorithm : algorithms) {         
         result.header.add(new NameAndAbrev(algorithm).getAbrev()+".COUNT");
      }
            
      //data

      DeparamFilter [] filters = getDeparamFilters(origQueryFilter);
      for(DeparamFilter curFilter : filters) {
        ArrayList line = new ArrayList();

        // first column of result = the value of the parameter
        line.add(curFilter.getParamValue());
        
     
        for (String algorithm : algorithms) {
          String [] enAlgoritemArray = {algorithm};
          JSONArray enALgoritemJArray = new JSONArray(enAlgoritemArray);
          query.set(EQuery.ID_Algorithms, enALgoritemJArray);
          query.set(EQuery.ID_Filter, new JSONArray(curFilter.getFilter()));
                
          TableData dataForAlg = runQuery_NO_COUNT(project, query);      
          int algCount = (dataForAlg != null && dataForAlg.data != null) ? dataForAlg.data.size() : 0;
          line.add(algCount);
        }
        result.data.add(line);
      }
      
      return result;
    }
  }
  
    
    
  
  public static TableData runQuery_NO_COUNT(EProject project, EQuery query) {
    TableData td = new TableData();
    
    NameAndAbrev[] algs = query.getNATabFromJSONArray(EQuery.ID_Algorithms);
    NameAndAbrev[] testsets = query.getNATabFromJSONArray(EQuery.ID_TestSets);
    
    if (algs == null || algs.length < 1 || testsets == null || testsets.length < 1)
      return td;

    HashMap<String, ResultPack> results = new HashMap<>();

    for (NameAndAbrev alg : algs) {
      ResultPack rPack = new ResultPack();
      for (NameAndAbrev ts : testsets) {
        ATLog.disableLog(); // to prevent error messages on missing description files
	  readResults(rPack, project, alg.getName(), ts.getName(), MeasurementType.EM);
          readResults(rPack, project, alg.getName(), ts.getName(), MeasurementType.CNT);
          readResults(rPack, project, alg.getName(), ts.getName(), MeasurementType.JVM);
        ATLog.enableLog();
      }
      results.put(alg.getName(), rPack);
    }

    NameAndAbrev[] inPars = query.getNATabFromJSONArray(EQuery.ID_inParameters);
    NameAndAbrev[] outPars = query.getNATabFromJSONArray(EQuery.ID_outParameters);
    
    // the order of testset-test key is obtained from the first algorithm (this order
    // should be the same for all algorithms, therefore the selection of the algorithms 
    // is arbitrary). 
    ArrayList<String> keyOrder = results.get(algs[0].getName()).keyOrder;

    // add headers for default test parameters
    td.header.add(EResultDescription.testNoParName);  // ID of a table row
    td.header.add(EResultDescription.tstParName);
    td.header.add(EResultDescription.testIDParName);
    td.header.add(EResultDescription.passParName);

    // Input (test) parameters + 4 default parameters (TestNo, TestSet, TestID, pass) 
    td.numberOfInputParameters = inPars.length + 4; 

    
    for (NameAndAbrev inPar : inPars) {
      td.header.add(inPar.getAbrev());
    }
    for (NameAndAbrev outPar : outPars) {
      for (NameAndAbrev alg : algs) {
	td.header.add(alg.getAbrev() + "." + outPar.getAbrev());
      }
    }

    int testNUM = 0;
    for (String key : keyOrder) {
      testNUM++;
      ArrayList<Object> line = new ArrayList<>();

      String alg1Name = algs[0].getName();
      ParameterSet ps = results.get(alg1Name).getResult(key);
        
      // add values for 3 default test parameters
      line.add(testNUM);
      line.add(ps.getParamater(EResultDescription.tstParName).get(EParameter.ID_Value));
      line.add(ps.getParamater(EResultDescription.testIDParName).get(EParameter.ID_Value));
      line.add(ps.getParamater(EResultDescription.passParName).get(EParameter.ID_Value));
      
      
      //line.add(testNUM);
      
      for (NameAndAbrev inPar : inPars) {
	Object value;
	try {
	  EParameter parameter = ps.getParamater(inPar.getName());
	  value =  parameter.getValue();
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
	    ParameterSet ps2 = results.get(alg.getName()).getResult(key);
	    EParameter parameter = ps2.getParamater(outPar.getName());
	    value = parameter.getValue();
	  } catch (Exception e) {
	    value = "?";
	  }
	  line.add(value);
	}
      }
      td.data.add(line);
    }
    
    
    String [] filter = query.getStringArray(EQuery.ID_Filter);
    for (int i = 0; i < filter.length; i++) {
      try {
        td.filter(filter[i]); 
      } catch (Exception e) {
        e.printStackTrace();
      }
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
    ATGlobal.setALGatorRoot("/Users/Tomaz/Dropbox/FRI/ALGOSystem/ALGATOR_ROOT");
    ATGlobal.setALGatorDataRoot("/Users/Tomaz/Dropbox/FRI/ALGOSystem/ALGATOR_ROOT/data_root");

    String projectname="Sorting";
    String queryName="query1";
    String [] params = new String[] {"BubbleSort"};
    
    String projectRoot = ATGlobal.getPROJECTroot(ATGlobal.getALGatorDataRoot(), projectname);
    String qResultFileName = ATGlobal.getQUERYOutputFilename(projectRoot, queryName, params);
    
    HashSet<String> dep = ATTools.getFilesForQuery(projectname, queryName, params);
    for (String dep1 : dep) {
      System.out.println(dep1);
    }
        
    System.out.println(ATTools.resultsAreUpToDate(dep, qResultFileName));
    
    String r = getQueryResultTableAsString(projectname, queryName, params);
    
    System.out.println(ATTools.resultsAreUpToDate(dep, qResultFileName));
    
    System.out.println(r);
  }
}
