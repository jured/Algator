import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Random;
import si.fri.algotest.entities.EVariable;
import si.fri.algotest.entities.EResult;
import si.fri.algotest.entities.VariableType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.DefaultTestSetIterator;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author tomaz
 */
public class BasicSortTestSetIterator extends DefaultTestSetIterator {     
  @Override
  public TestCase getCurrent() {
    if (currentInputLine == null) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "No valid input!");
      return null;
    }
    
    // sort-project specific: line contains at least 3 fileds: testName, n and group
    String [] fields = currentInputLine.split(":"); 
    if (fields.length < 3) {
      reportInvalidDataFormat("to few fields");
      return null;
    }
    
    String testName = fields[0];
    int probSize;
    try {
      probSize = Integer.parseInt(fields[1]);
    } catch (Exception e) {
      reportInvalidDataFormat("'n' is not a number");
      return null;
    }
    String group = fields[2];
    
    int rndSize = -1; // the size of the rnadom numbers used in the array (used with RND group). 

    
    // unique identificator of a test
    EVariable testIDPar = EResult.getTestIDParameter("Test-" + Integer.toString(lineNumber));
    
    EVariable parameter1 = new EVariable("Test",  "Test name",                    VariableType.STRING, testName);
    EVariable parameter2 = new EVariable("N",     "Number of elements",           VariableType.INT,    probSize);
    EVariable parameter3 = new EVariable("Group", "A name of a group of tests",   VariableType.STRING, group);
    
    BasicSortTestCase tCase = new BasicSortTestCase();
    // ID
    tCase.addParameter(testIDPar);
    // input parameters
    tCase.addParameter(parameter1);
    tCase.addParameter(parameter2);
    tCase.addParameter(parameter3);
    
    // read the input data (content of an aray to sort); to source of the
    // data is defined by the group parameter
    int [] array = new int[probSize];
    int i=0;
    switch (group) {
      case "INLINE":
	if (fields.length < 4) {
          reportInvalidDataFormat("to few fields");
          return null;
        }
	String data[] = fields[3].split(" ");
	if (data.length != probSize) {
	  reportInvalidDataFormat("invalid number of inline data");
	  return null;
	}
	
	try {
	  for (i = 0; i < probSize; i++) 
	    array[i] = Integer.parseInt(data[i]);
	} catch (Exception e) {
	  reportInvalidDataFormat("invalid type of inline data - data " + (i+1));
	  return null;
	}
	break;
      case "RND":
	Random rnd = new Random(System.currentTimeMillis());
        try {rndSize = Integer.parseInt(fields[3]);} catch (Exception e) {rndSize=Integer.MAX_VALUE;}

	for (i = 0; i < probSize; i++) 
	  array[i] = Math.abs(rnd.nextInt(rndSize));
	break;
      case "SORTED":
	for (i = 0; i < probSize; i++) 
	  array[i] = i;
	break;
      case "INVERSED":
	for (i = 0; i < probSize; i++) 
	  array[i] = probSize-i;	
	break;
      case "FILE":  // first parameter is filemane, second the offset (from where numbers are read)
	try {
	  if (fields.length != 5)
	    throw new Exception("No input file or missing offset");
	  
	  i=0;
	  String testFile = filePath + File.separator + fields[3];
          
          Integer offset = 0;
          try {
            offset = Integer.parseInt(fields[4]);
          } catch (Exception e){}
          
	  DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(testFile))));
          
          // skip the first "offset" numbers
          dis.skipBytes(offset*4);
          
	  while(i < probSize && dis.available() > 0) {
	    array[i++] = dis.readInt();
	  }
          if (i < probSize)
            throw new Exception("Not enough data in file");
	  dis.close();
	} catch (Exception e) {
	  reportInvalidDataFormat(e.toString());
	}
    }
    
    EVariable parameter4 = new EVariable("RndSize", "The size of RND nummers",   VariableType.INT, rndSize);
    tCase.addParameter(parameter4);

    tCase.arrayToSort = array;
    return tCase; 
  } 
}
 