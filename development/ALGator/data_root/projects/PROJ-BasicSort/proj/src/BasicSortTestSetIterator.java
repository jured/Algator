import java.io.File;
import java.util.Random;
import java.util.Scanner;
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
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "Not a valid input!");
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
	for (i = 0; i < probSize; i++) 
	  array[i] = rnd.nextInt(1000);
	break;
      case "SORTED":
	for (i = 0; i < probSize; i++) 
	  array[i] = i;
	break;
      case "INVERSED":
	for (i = 0; i < probSize; i++) 
	  array[i] = probSize-i;	
	break;
      case "FILE":
	try {
	  if (fields.length != 4)
	    throw new Exception("No input file");
	  
	  i=0;
	  String testFile = filePath + File.separator + fields[3];
	  Scanner sc = new Scanner(new File(testFile));
	  while(i < probSize && sc.hasNextInt()) {
	    array[i++] = sc.nextInt();
	  }
	  sc.close();
	} catch (Exception e) {
	  reportInvalidDataFormat(e.toString());
	}
    }
    tCase.arrayToSort = array;    

    return tCase;
  }
}
 