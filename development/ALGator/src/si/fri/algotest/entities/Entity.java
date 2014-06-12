package si.fri.algotest.entities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONObject;
import si.fri.algotest.tools.ATTools;
import si.fri.algotest.global.ErrorStatus;

/**
 * Entity represents a low-level class needed to implement an entity like 
 * ATProject, ATAlgorithm, ATParameter, ...
 * Entity loads an entity from a file or from a JSON string. The fields of an 
 * entity are set through the constructor's fieldName parameter. The method 
 * initFromJSON reads all these fields from JSON description of an entity and
 * fills the map with correspnding values.
 * To use the vaules of the fileds in a subclass of Entity the user can either 
 * use the get(set)String, get(set)Integer, get(set)Double, get(set)StringArray 
 * methods or write his own getters and setters.
 * @author tomaz
 */
public class Entity implements Cloneable {

  private final String unknown_value = "?";
  
  /**
   * The ID of an entity in an JSON file
   */
  protected String entity_id;
  
    
  /**
   * The folder of the entity file (if entity was read from a file), null otherwise 
   */
  public String entity_rootdir;
  
  /**
   * The prefix of the name of the file from which the entity was read (if entity was read from a file), null otherwise
   */
  private String entity_name;

  
  protected TreeSet<String> fieldNames;
  protected HashMap<String, Object> fields;
  
  
  // a list of representative fields (fields that represent this entity)
  // this list is used to construct toString message
  protected ArrayList<String> representatives;

  public Entity() {
    fieldNames  = new TreeSet();
    fields       = new HashMap();
  }
  
  public Entity(String entityID, String [] fieldNames) {
    this();
    
    entity_id   = entityID;
    entity_name = "?";
    
    this.fieldNames.addAll(Arrays.asList(fieldNames));
    
    representatives=new ArrayList<>();
  }
  
  public String getName() {
    return entity_name;
  }
  
  /**
   * Reads a JSON file. If entity_id=="", then whole file represents an JSON
   * object to be read (i.e. the file contains only this object); else, the 
   * file contains a JSON object in which the field with key=entity_id is read. 
   * @param entityFile
   * @return 
   */
  public ErrorStatus initFromFile(File entityFile) {
    entity_rootdir = ATTools.extractFilePath(entityFile);
    entity_name    = ATTools.extractFileNamePrefix(entityFile);
    
    try (Scanner sc = new Scanner(entityFile, "UTF-8")) {
      String vsebina = "";
      while (sc.hasNextLine()) 
	vsebina += sc.nextLine();
      
      if (entity_id == null || entity_id.isEmpty())
	return initFromJSON(vsebina);
      
      JSONObject object = new JSONObject(vsebina);
      String entity = object.optString(entity_id);
      if (entity.isEmpty())
	throw new Exception("Token '"+entity_id+"' does not exist.");
      
      return initFromJSON(entity);
    } catch (Exception e) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_READFILE,  
	      String.format("File: %s, Msg: %s", entityFile.getAbsolutePath(), e.toString()));
    }
  }
  
  /**
   * Method reads a JSON object and fills the map with correspnding values.
   * @param json
   * @return 
   */
  public ErrorStatus initFromJSON(String json) {
    try {
      JSONObject jsonObj = new JSONObject(json);
      for(String sp : fieldNames) {
        fields.put(sp, jsonObj.opt(sp));
      }
      return ErrorStatus.setLastErrorMessage(ErrorStatus.STATUS_OK, "");
    } catch (Exception e) {
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_INIT_FROM_JSON, "");      
    }
    
  }

  public String toJSONString() {
    return toJSONString(false);
  }
  
  
  public String toJSONString(boolean wrapWithEntity) {
    JSONObject result = new JSONObject();
    for(String sp : fieldNames) {
      Object o = fields.get(sp);
      
      if (o instanceof Object[]) {
        o = new JSONArray(o);
      }
      
      result.put(sp, o);
    }

    if (wrapWithEntity) {
      JSONObject wrapped = new JSONObject();
      wrapped.put(entity_id, result);
      return wrapped.toString(2);
    } else
      return result.toString(2);
  }
  
  
  public Object get(String fieldKey) {
    if (fieldNames.contains(fieldKey))
      return fields.get(fieldKey);
    else
      return unknown_value;
  }   
  
  public void set(String fieldKey, Object object) {
    fields.put(fieldKey, object);
  }
  
  /**
   * Method is used to get a field in its type. For example, a field
   * of type Integer is read by Integer i = getField("key"), a field
   * of type Double is read by Double i = getField("key").
   */
  public <E> E getField(String fieldKey) {
    E result = null;
    if (fieldNames.contains(fieldKey)) {
      result = (E) fields.get(fieldKey);
      
    }
    return result; 
  }   
    
  /**
   * Method return an String array obtained from corresponding field. If fieldKey 
   * does not exist or if jason field is not an string array, an array with length==0
   * is returned.
   */
  public String[] getStringArray(String fieldKey) {
    try {
      JSONArray ja = getField(fieldKey);
      String [] result = new String [ja.length()];
      for (int i = 0; i < ja.length(); i++) {
	result[i] = ja.getString(i);
      }
      return result;
    } catch (Exception e) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_NOT_A_STRING_ARRAY, fieldKey);
      
      return new String[0];
    }
  }
  
  public void setRepresentatives(String ... fields) {
    representatives = new ArrayList<>();
    for (String string : fields) {
      representatives.add(string);
    }
  }

  @Override
  public String toString() {
    String desc = getName();
    for (String rep : representatives) 
      desc += (desc.isEmpty() ? "" : ", ") + rep + "=" + fields.get(rep);
    
    return entity_id + "[" + desc + "]";
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    Entity myClone = (Entity) super.clone(); 
    myClone.fields          = (HashMap)   this.fields.clone();
    myClone.fieldNames      = (TreeSet)   this.fieldNames.clone();
    myClone.representatives = (ArrayList) this.representatives.clone();
    return myClone;
  }
  
  
  
  
  public static void main(String args[]) {
    Entity e = new Entity("MojaE", new String[] {"A", "B"});
    e.setRepresentatives("A", "B");
    e.set("A", "eA");
    e.set("B", "eB");

    Entity b = null;
    try {
      b = (Entity) e.clone();
    } catch (CloneNotSupportedException ex) {
      System.out.println(ex);
    }
    
    System.out.println(e);
    System.out.println(b);
    System.out.println("");
    
    b.set("A", "xyA");
    b.set("B", "xyB");
    b.setRepresentatives("A");
    b.entity_id = "Bluks";
  
    System.out.println(e);
    System.out.println(b);
    
    System.out.println(b.toJSONString());
    
  }
  
}