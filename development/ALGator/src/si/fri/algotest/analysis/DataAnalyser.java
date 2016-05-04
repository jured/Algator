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
import si.fri.algotest.entities.EVariable;
import si.fri.algotest.entities.EProject;
import si.fri.algotest.entities.EQuery;
import si.fri.algotest.entities.EResult;
import si.fri.algotest.entities.Entity;
import si.fri.algotest.entities.MeasurementType;
import si.fri.algotest.entities.NameAndAbrev;
import si.fri.algotest.entities.VariableSet;
import si.fri.algotest.entities.Project;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.tools.ATTools;

/**
 *
 * @author tomaz
 */
public class DataAnalyser {

  
  /**
   * for all Parameters in filter, method checks the corresponding parameter in
   * the result; if the values doesn't match, return false, else return true
   *
   * @return
   */
  public static boolean parametersMatchFilter(VariableSet params, VariableSet filter) {
    if (filter == null) {
      return true;
    }

    try {
      for (int i = 0; i < filter.size(); i++) {
	EVariable refPar = filter.getVariable(i);
	Object refVal = refPar.get(EVariable.ID_Value);

	EVariable param = params.getVariable((String) refPar.getName());
	Object value = param.get(EVariable.ID_Value);

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
  public static ArrayList<VariableSet> selectData(HashMap<String, ArrayList<VariableSet>> algResults, VariableSet join,
	  VariableSet filter, VariableSet testFields, VariableSet resultFields) {

    ArrayList<VariableSet> result = new ArrayList();
    Set<String> algKeys = algResults.keySet();

    for (String alg : algKeys) {
      for (VariableSet ps : algResults.get(alg)) {
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
  public static void readResults(ResultPack resPack, Project project,
	  String algorithm, String testset, MeasurementType measurement, String computerID) {

    if (project == null || resPack == null) {
      return;
    }

    String resDescFilename = ATGlobal.getRESULTDESCfilename(project.getProjectRoot(), project.getName(), measurement);
    EResult eResultDesc = new EResult(new File(resDescFilename));

    String delim =  ATGlobal.DEFAULT_CSV_DELIMITER;

    VariableSet resultPS = eResultDesc.getVariables();
    // test parameters are only defined in EM file
    String[] testOrder = eResultDesc.getStringArray(EResult.ID_ParOrder);
    if (testOrder == null) testOrder = new String[0];
    
    String[] resultOrder = eResultDesc.getStringArray(EResult.ID_IndOrder);


    String resFileName;
    // če imam eksplicitno podano ime racunalnika, vem, kjer moram iskati rezultate ...
    if (computerID!=null && !computerID.isEmpty())
      resFileName = ATGlobal.getRESULTfilename(project.getProjectRoot(), algorithm, testset, measurement, computerID);
    // ... sicer pa poiščem najbolj primerno datoteko
    else
      resFileName = ATTools.getTaskResultFileName(project, algorithm, testset, measurement.getExtension());
    
    File resFile = new File(resFileName);

    if (resFile.exists()) {

      // add the current resultdescription parameters to the resPack resultDescription parameterset
      resPack.resultDescription.getVariables().addVariables(eResultDesc.getVariables(), false);

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
	  VariableSet algPS = new VariableSet(resultPS);

	  // sets the value of default parameters
	  algPS.getVariable(EResult.algParName).set(EVariable.ID_Value, algorithm);
	  algPS.getVariable(EResult.tstParName).set(EVariable.ID_Value, testset);
	  algPS.getVariable(EResult.testIDParName).set(EVariable.ID_Value, testName);
          algPS.getVariable(EResult.passParName).set(EVariable.ID_Value, pass);

	  int lineFiledsPos = EResult.FIXNUM;

	  // sets the value of test parameters
	  for (int i = 0; i < testOrder.length; i++) {
	    EVariable tP = algPS.getVariable(testOrder[i]);
            
	    if (tP != null)
              if (lineFiledsPos < lineFields.length) 
	        tP.set(EVariable.ID_Value, lineFields[lineFiledsPos]);
              else
                tP.set(EVariable.ID_Value, tP.getType().getDefaultValue()); 
            
	    lineFiledsPos++;
	  }

	  // sets the value of result parameters
	  for (int i = 0; i < resultOrder.length; i++) {
	    EVariable tP = algPS.getVariable(resultOrder[i]);
            
	    if (tP != null)
              if (lineFiledsPos < lineFields.length) 
	        tP.set(EVariable.ID_Value, lineFields[lineFiledsPos]);
              else
                tP.set(EVariable.ID_Value, tP.getType().getDefaultValue());            
            
            lineFiledsPos++;
	  }

	  // add this ParameterSet to the ResultPack
	  String key = testset + "-" + testName;
	  VariableSet ps = resPack.getResult(key);
	  // If ParameterSet for this testset-test doesn't exist ...
	  if (ps == null) {
	    // ... append a new testset to the map
	    resPack.putResult(key, algPS);
	  } else {
	    // ... otherwise, add new parameters to existing parameterset
	    ps.addVariables(algPS, false);
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

  
  
    /**
   * Gets the query result as table of values. The input query can be a) query written in json format
   * and b) filename of a query written in a file. If query is json, EQuery is generated and result 
   * is returned: otherwise, if query is filename, method checks the file with name queryName+parameters. 
   * If file exists and is fresh (never than project configuration files), method returns its content, 
   * else it runs a query, writes the result to file and returns the result.
   */
  public static String getQueryResultTableAsString(String projectname, String query, String [] params, String computerID) {
    if (projectname==null || projectname.isEmpty() || query==null || query.isEmpty()) return "";
    
    if (query.startsWith("{")) {
      EProject project = new EProject(new File(ATGlobal.getPROJECTfilename(ATGlobal.getALGatorDataRoot(), projectname)));  
      EQuery eQuery = new EQuery(query, params);
      return runQuery(project, eQuery, computerID).toString();
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
        TableData td = runQuery(projectname, queryname, params, computerID);
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
  
  /**
   * Returns an array of entities (algorithms or testsets) used in thus query. If a star ("*") is in the list
   * of query's entity, method returns all project's entities, otherwise it returns 
   * only entities listed in query.
   */
  private static String[] getQueryEntities(EProject project, EQuery query, String queryEntityID, String projectEntityID) {
    return getQueryEntities(query, queryEntityID, project.getStringArray(projectEntityID));
  }
  private static String[] getQueryEntities(EQuery query, String queryEntityID, String [] allEntities) {
    return getQueryEntities(query, queryEntityID, allEntities, "*");
  }

  private static String[] getQueryEntities(EQuery query, String queryEntityID, String [] allEntities, String asterisk) {
      String [] etts = query.getStringArray(queryEntityID);
      
      boolean containsAll = false; // is "*" in the list of entities?
      for (int i = 0; i < etts.length; i++) {
        if (etts[i].startsWith(asterisk)) {
          containsAll = true; break;
        }
      }
      if (containsAll) {
        etts = allEntities;        
      }
      
      return etts;
  }
  
  
  public static TableData runQuery(String projectname, String queryname, String computerID) {
    return runQuery(projectname, queryname, null, computerID);
  }   
  public static TableData runQuery(String projectname, String queryname, String [] params, String computerID) { 
    EProject project = new EProject(new File(ATGlobal.getPROJECTfilename(ATGlobal.getALGatorDataRoot(), projectname)));  
    EQuery   query   = new EQuery(new File(ATGlobal.getQUERYfilename(project.getProjectRootDir(), queryname)), params);
    
    return runQuery(project, query, computerID);
  }
  
  /**
   * Methos runs a given query. For NO_COUNT queries it calls  runQuery_NO_COUNT once, while for COUNT queries
   * runQuery_NO_COUNT is called n times (n=number of algorithm selected in query) and the results 
   * are joint into a single tableData 
   */
  public static TableData runQuery(EProject project, EQuery query, String computerID) {
    if (!query.isCount()) {
      
      return runQuery_NO_COUNT(project,query, computerID);
    } else {
      TableData result = new TableData();            
      
      String algorithms [] = getQueryEntities(project, query, EQuery.ID_Algorithms, EProject.ID_Algorithms);
              
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
                
          TableData dataForAlg = runQuery_NO_COUNT(project, query, computerID);      
          int algCount = (dataForAlg != null && dataForAlg.data != null) ? dataForAlg.data.size() : 0;
          line.add(algCount);
        }
        result.data.add(line);
      }
      
      return result;
    }
  }
  
    
    
  
  public static TableData runQuery_NO_COUNT(EProject eProject, EQuery query, String computerID) {
    TableData td = new TableData();
    
    String queryComputerID = query.getField(EQuery.ID_ComputerID);
    // če ima query definiran computerID, potem se uporabi tega
    if (queryComputerID != null && !queryComputerID.isEmpty()) {
      computerID = queryComputerID;
    } else { // če comupterID v poizvedbi ni naveden, pa uporabim podani computerID; če je ta prazen pa thisComputerID
      if (computerID == null || computerID=="")
        computerID=ATGlobal.getThisComputerID();
    }
    
    String algorithms []    = getQueryEntities(eProject, query, EQuery.ID_Algorithms, EProject.ID_Algorithms);
    NameAndAbrev[] algs     = query.getNATabFromJSONArray(algorithms);

    String tsts []          = getQueryEntities(eProject, query, EQuery.ID_TestSets, EProject.ID_TestSets);    
    NameAndAbrev[] testsets = query.getNATabFromJSONArray(tsts);
    
    if (algs == null || algs.length < 1 || testsets == null || testsets.length < 1)
      return td;

    HashMap<String, ResultPack> results = new HashMap<>();

    String projectRootDir = eProject.getProjectRootDir();
    String data_root      = ATGlobal.getDataRootFromProjectRoot(projectRootDir);
    Project project = new Project(data_root, eProject.getName());
    
    for (NameAndAbrev alg : algs) {
      ResultPack rPack = new ResultPack();
      for (NameAndAbrev ts : testsets) {
        ATLog.disableLog(); // to prevent error messages on missing description files
	  readResults(rPack, project, alg.getName(), ts.getName(), MeasurementType.EM,  computerID);
          readResults(rPack, project, alg.getName(), ts.getName(), MeasurementType.CNT, computerID);
          readResults(rPack, project, alg.getName(), ts.getName(), MeasurementType.JVM, computerID);
        ATLog.enableLog();
      }
      results.put(alg.getName(), rPack);
    }

    ArrayList errors = new ArrayList(); HashMap resultDescriptions = new HashMap();
    Project.readResultDescriptions(eProject.getProjectRootDir(), eProject.getName(), resultDescriptions, errors);
    
    String [] allINParamaters = Project.getTestParameters(resultDescriptions);
    String inParameters []    = getQueryEntities(query, EQuery.ID_Parameters, allINParamaters);        
    NameAndAbrev[] inPars     = query.getNATabFromJSONArray(inParameters);


    // calculate EM parameters ...
    String [] allEMParamaters = Project.getResultParameters(resultDescriptions, MeasurementType.EM);
    String emParameters []    = getQueryEntities(query, EQuery.ID_Indicators, allEMParamaters, "*EM");        
    NameAndAbrev[] emPars     = query.getNATabFromJSONArray(emParameters);
    // ... CNT parameters ...
    String [] allCNTParamaters = Project.getResultParameters(resultDescriptions, MeasurementType.CNT);
    String cntParameters []    = getQueryEntities(query, EQuery.ID_Indicators, allCNTParamaters, "*CNT");        
    NameAndAbrev[] cntPars     = query.getNATabFromJSONArray(cntParameters);
    // ... JVM parameters ...
    String [] allJVMParamaters = Project.getResultParameters(resultDescriptions, MeasurementType.JVM);
    String jvmParameters []    = getQueryEntities(query, EQuery.ID_Indicators, allJVMParamaters, "*JVM");        
    NameAndAbrev[] jvmPars     = query.getNATabFromJSONArray(jvmParameters);
    // ... and join all together
    int n = 0;
    ArrayList<NameAndAbrev> outParsAL = new ArrayList<>();
    for (NameAndAbrev emParam:  emPars ) 
      if (!outParsAL.contains(emParam) && !emParam.getName().startsWith("*"))  outParsAL.add(emParam);
    for (NameAndAbrev cntParam: cntPars) 
      if (!outParsAL.contains(cntParam) && !cntParam.getName().startsWith("*")) outParsAL.add(cntParam);
    for (NameAndAbrev jvmParam: jvmPars) 
      if (!outParsAL.contains(jvmParam)  && !jvmParam.getName().startsWith("*")) outParsAL.add(jvmParam);
    
    NameAndAbrev[] outPars = new NameAndAbrev[outParsAL.size()]; 
    outParsAL.toArray(outPars);
            //new NameAndAbrev[emPars.length + cntPars.length + jvmPars.length];
    
    //NameAndAbrev[] outPars = query.getNATabFromJSONArray(EQuery.ID_outParameters);
    
    // ver 1.0: the order of testset-test key is obtained from the first algorithm (this order
    // should be the same for all algorithms, therefore the selection of the algorithms  is arbitrary). 
    // ver 2.0 (jan2016): The above statement is not true! If the results of the first algorithm are corrupted, 
    // keyOrder can be null. Therefore, in this version we take the keyOrder with maximum number of keys.
    ArrayList<String> keyOrder=null;
    NameAndAbrev alg0 = null;
    for (NameAndAbrev alg : algs) {
      if (keyOrder==null || keyOrder.size() < results.get(alg.getName()).keyOrder.size()) {
        alg0 = alg;
        keyOrder = results.get(alg0.getName()).keyOrder;
      }
    }


    // add headers for default test parameters
    td.header.add(EResult.testNoParName);  // ID of a table row
    td.header.add(EResult.tstParName);
    td.header.add(EResult.testIDParName);
    td.header.add(EResult.passParName);

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

      String alg0Name = alg0.getName();
      VariableSet ps = results.get(alg0Name).getResult(key);
        
      // add values for 3 default test parameters
      line.add(testNUM);
      line.add(ps.getVariable(EResult.tstParName).get(EVariable.ID_Value));
      line.add(ps.getVariable(EResult.testIDParName).get(EVariable.ID_Value));
      line.add(ps.getVariable(EResult.passParName).get(EVariable.ID_Value));
      
      
      //line.add(testNUM);
      
      for (NameAndAbrev inPar : inPars) {
	Object value;
	try {
	  EVariable parameter = ps.getVariable(inPar.getName());
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
	    VariableSet ps2 = results.get(alg.getName()).getResult(key);
	    EVariable parameter = ps2.getVariable(outPar.getName());
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
}
