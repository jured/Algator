package si.fri.algotest.entities;

import java.io.File;
import org.json.JSONArray;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ErrorStatus;



/**
 *
 * @author tomaz
 */
public class EQuery extends Entity {
  // Entity identifier
  public static final String ID_Query   ="Query";  
  
  //Fields
  public static final String ID_Description    = "Description";	     // String
  public static final String ID_Algorithms     = "Algorithms";       // NameAndAbrev []
  public static final String ID_TestSets       = "TestSets";         // NameAndAbrev []
  public static final String ID_inParameters   = "TestParameters";   // String []
  public static final String ID_outParameters  = "ResultParameters"; // String []
  public static final String ID_GroupBy        = "GroupBy";          // String []
  public static final String ID_Filter         = "Filter";           // String []
  public static final String ID_SortBy         = "SortBy";           // String []
  
   
  
  
  
  public EQuery() {
   super(ID_Query, 
	 new String [] {ID_Description, ID_Algorithms, ID_TestSets, ID_inParameters, ID_outParameters, 
                        ID_GroupBy, ID_Filter, ID_SortBy});
  }
  
  public EQuery(String [] algs, String [] tsts, String [] inParams, String [] outParams,
                String [] groupby, String [] filter, String [] sortby) {
    this();
    
    set(ID_Algorithms,    new JSONArray(algs));
    set(ID_TestSets,      new JSONArray(tsts));
    set(ID_inParameters,  new JSONArray(inParams));
    set(ID_outParameters, new JSONArray(outParams));
    set(ID_GroupBy,       new JSONArray(groupby));
    set(ID_Filter,        new JSONArray(filter));
    set(ID_SortBy,        new JSONArray(sortby));
  }
  
  
  public EQuery(File fileName) {
    this();
    initFromFile(fileName);
    setRepresentatives(ID_Algorithms, ID_TestSets);
  }
  
  /**
   * Return an aray of NameAndAbrev for parameter of type String [] with vaules 
   * of form "name as abrev".
   * Note: algorithms and testsets are given in json file as array of string 
   * values of form "name as abrev". 
   * @param id ID of parameter (i.e. ID_Algorithms)
   * @return 
   */
  public NameAndAbrev [] getNATabFromJSONArray(String id) {
    String [] entities = getStringArray(id);
    
    NameAndAbrev [] result = new NameAndAbrev[entities.length];
    for (int i = 0; i < entities.length; i++) {
      result[i] = new NameAndAbrev(entities[i]);
    }
    
    return result;
  }
  
  /**
   * Method produces an json array of string of form "name as abrev" from a given 
   * array of NameAndAbrev entities.
   */
  public void setJSONArrayFromNATab(NameAndAbrev [] entities, String id) {
    String [] strEntities = new String[entities.length];
    for (int i = 0; i < entities.length; i++) {
      strEntities[i] = entities[i].toString();
    }
    JSONArray jTab = new JSONArray(strEntities);
    set(id, jTab);
  }
  
  public static void main(String args[]) {
    String   root = ATGlobal.ALGatorDataRoot;
    
    if (root==null || root.isEmpty())
      root = "/Users/Tomaz/Dropbox/FRI/ALGator/data_root/"; 
    
    String projName = "Sorting";
    String projRoot     = ATGlobal.getPROJECTroot(root, projName);
    
    EQuery query = new EQuery(new File(ATGlobal.getQUERYfilename(projRoot, "q1")));
    Object o = query.get(ID_Algorithms);
    
    System.out.println(query);
    
    NameAndAbrev [] naa = query.getNATabFromJSONArray(ID_Algorithms);
    for (int i = 0; i < naa.length; i++) {
      naa[i].setAbrev(naa[i].getAbrev() + "1");
    }
    query.setJSONArrayFromNATab(naa, ID_Algorithms);
    
    System.out.println(query.toJSONString());
  }
}