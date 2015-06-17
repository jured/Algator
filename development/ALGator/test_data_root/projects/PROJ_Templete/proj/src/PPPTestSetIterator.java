import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.DefaultTestSetIterator;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.tools.ATTools;
import si.fri.algotest.global.ErrorStatus;


/**
 *
 * @author ...
 */
public class PPPTestSetIterator extends DefaultTestSetIterator {
   
  
  @Override
  public TestCase getCurrent() {
    if (currentInputLine == null) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "No valid input!");
      return null;
    }
    
    PPPTestCase tCase = new PPPTestCase();
    EParameter testIDPar = EResultDescription.getTestIDParameter("Test-" + Integer.toString(lineNumber));
    tCase.addParameter(testIDPar);   
    
    // TODO: init input parameters
    // EParameter parameter1 = new EParameter("...",  "",  ParameterType.STRING, ...);
    
    // TODO: add input parameters to the testset 
    //tCase.addParameter(parameter1);
   
    // TODO: set testcase data fields
    
    return tCase;
  }

  
  
  // TEST
    
    public static void main(String args[]) {
    String dataroot     = "path to data root folder"; // a folder with the "projects" folder
    String projName     = "PPP";
    
    ETestSet testSet = ATTools.getFirstTestSetFromProject(dataroot, projName);
    PPPTestSetIterator stsi = new PPPTestSetIterator();
    stsi.setTestSet(testSet);
    
    ATTools.iterateAndPrintTests(stsi);
  }
  
}
 