package si.fri.algotest.entities;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author tomaz
 */
public class EGraph extends Entity  implements Serializable {
  // Entity identifier
  public static final String ID_ResultParameter   = "Graph";
  
  // Fields
  public static final String ID_Desc         = "Description";   // String
  public static final String ID_Xaxis        = "Xaxis";         // String
  public static final String ID_Yaxes        = "Yaxes";         // String[]
  public static final String ID_GraphTypes   = "GraphTypes";    // GraphType[]
  public static final String ID_XaxisLabel   = "XaxisLabel";    // String
  public static final String ID_YaxisLabel   = "YaxisLabel";    // String
    
  private GraphType graphType[];
  
  public EGraph() {
   super(ID_ResultParameter, 
	 new String [] {ID_Desc, ID_Xaxis, ID_Yaxes, ID_GraphTypes, ID_XaxisLabel, ID_YaxisLabel});
  }
  
  public EGraph(File fileName) {
    this();
    initFromFile(fileName);
    setRepresentatives(ID_Desc);    
    
    String [] types = getStringArray(ID_GraphTypes);
    graphType = new GraphType[types.length];
    for (int i=0; i<types.length; i++) {
      graphType[i] = GraphType.getType(types[i]);
    }
  }  
}
