import java.io.File;
import java.util.Random;
import java.util.Scanner;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.DefaultTestSetIterator;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.tools.ATTools;


/**
 *
 * @author tomaz
 */
public class SortTestSetIterator extends DefaultTestSetIterator {
   
  String filePath;
  String testFileName;
  
  private void reportInvalidDataFormat(String note) {
    String msg = String.format("Invalid input data in file %s in line %d.", testFileName, lineNumber);
    if (!note.isEmpty())
      msg += " ("+note+")";
    
    ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR,msg);
  }

  @Override
  public void initIterator() {
    super.initIterator();
    
    String fileName = testSet.getTestSetDescriptionFile();
    filePath = testSet.entity_rootdir;
    testFileName = filePath + File.separator + fileName;
  }

  
  
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
    
    // unique identificator of a test
    EParameter testIDPar = EResultDescription.getTestIDParameter("Test-" + Integer.toString(lineNumber));
    
    EParameter parameter1 = new EParameter("Test",  "Test name",                    ParameterType.STRING, testName);
    EParameter parameter2 = new EParameter("N",     "Number of elements",           ParameterType.INT,    probSize);
    EParameter parameter3 = new EParameter("Group", "A name of a group of tests",   ParameterType.STRING, group);
    
    SortTestCase tCase = new SortTestCase();
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


  
  
  // TEST
  public static void main(String args[]) {
    String root         = "../../data_root";
    String projName     = "Sorting";
    
    ETestSet testSet = ATTools.getFirstTestSetFromProject(root, projName);
    SortTestSetIterator stsi = new SortTestSetIterator();
    stsi.setTestSet(testSet);
    
    ATTools.iterateAndPrintTests(stsi);
  }
}
 