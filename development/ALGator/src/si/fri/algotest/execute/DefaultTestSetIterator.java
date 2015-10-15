package si.fri.algotest.execute;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.global.ATLog;
import si.fri.algotest.global.ErrorStatus;

/**
 * SimpleTestSetIterator provides the methods to iterate throught  testsets in which 
 * each line of description file describes exactly one testcase. 
 * The project-dependant testsetiterator that extends this 
 * class has only to implement the getCurrent() method, which parses the current 
 * input line (currentInputLine) and produces corresponding testcase.    
 * @author tomaz
 */
public abstract class DefaultTestSetIterator  extends AbstractTestSetIterator {

  private   Scanner input;             // scanner used to iterate throught the Description file
  private   String  testFileName;      // the name of the file this iterator reads from
  
  protected int     lineNumber;        // the number of the current line
  protected String  currentInputLine;  // the current input line 

  
  
  
  @Override
  /**
   * The constructor of this class was changed and initIterator() has to be called manually!
   */
  public void initIterator() {
    if (testSet != null) {
      String fileName = testSet.getTestSetDescriptionFile();

      try {
        if (fileName == null) 
	  throw new Exception("Testset descritpion file does not exist.");
      
        String filePath = testSet.entity_rootdir;
      
        String testFileName = filePath + File.separator + fileName;
      
        input = new Scanner(new File(testFileName));
	lineNumber=0;
      } catch (Exception e) {
	input = null;
        ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_INVALID_TESTSET, e.toString());
      } 
    }
  }

  @Override
  public boolean hasNext() {
    return (input != null && input.hasNextLine());
  }
  
  @Override
  public void readNext() {
    if (input == null || !input.hasNextLine()) {
      ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "No more input to read!");
    }
    
    currentInputLine = input.nextLine(); lineNumber++;
  }

  @Override
  public boolean readTest(int testNumber) {
    if (lineNumber > testNumber) {
      try {
        input.close();
        input = new Scanner(new File(testFileName));
	lineNumber=0;
      } catch (Exception e) {
	input = null;	
	ATLog.log("Error reading test with number " + testNumber+ "! Error: " + e.toString(), 2);
        return false;
      } 
    }
    
    while (testNumber > lineNumber && hasNext())
      readNext();
    
    return testNumber == lineNumber;
  }
  
  
    
 @Override
  public void close() throws IOException {
    input.close();
  }
  

}
