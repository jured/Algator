import java.io.*;
import java.io.IOException;
import java.nio.charset.Charset;
import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.EResultDescription;
import si.fri.algotest.entities.ETestSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.DefaultTestSetIterator;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.tools.ATTools;


/**
 *
 * @author Anze Pratnemer
 */
public class StringSearchTestSetIterator extends DefaultTestSetIterator {
    String filePath;
    String testFileName;
    
    int randomPatternMaxLength = 100;
    String pattern;
    String text;
  
    private void reportInvalidDataFormat(String note) {
        String msg = String.format("Invalid input data in file %s in line %d.", testFileName, lineNumber);
        if (!note.isEmpty())
            msg += " (" + note + ")";
    
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
    public TestCase getCurrent(){
        if (currentInputLine == null) {
            ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR, "No valid input!");
        return null;
        }
    
        String [] fields = currentInputLine.split(":"); 
        if (fields.length < 4) {
            reportInvalidDataFormat("To few fields!");
            return null;
        }
        String testName = fields[0];

        //preberemo fajl v string    
        String testFile = filePath + File.separator + fields[3];

        try{    
        text = readFile(testFile, "UTF-8");
        //System.out.println(text);
        }
        catch(IOException e){
            System.out.println(e.toString());
            return null; 
        }
        //konec branja iz file

        int probSize = text.length();
        
        //nastavimo iskalni niz
        String group = fields[1]; 
        switch (group) {
            case "RND":   //uporabimo nakljucni iskani niz dolocene dolzine
                Integer patternLength = Integer.getInteger(fields[2]);
                if (patternLength == null ) {   //dolzina ni podana, naredimo nakljucno dolzino
                patternLength = (int)(Math.random() * randomPatternMaxLength);
                System.out.println("pattern length: " + patternLength);
                }

                try {
                    //dolocimo nakljucni kazalec v tekstu kjer bomo zaceli brati iskani niz nakljucne dolzine            
                    long kazalec = (long) (Math.random() * probSize);

                    //ce nam velikost iskani niz preliva dolzino teksta, nastavimo iskani niz na zadnjih patternLength znakov teksta
                    if(kazalec + patternLength > probSize) kazalec = probSize - patternLength; 
                    pattern = text.substring((int)kazalec, (int)kazalec+patternLength);             
                }catch (Exception e) {
                    reportInvalidDataFormat(e.toString());
                }       
                break;

            case "SET":   //uporabimo dolocen iskalni niz
                pattern = fields[2];
                break;
        }

        StringSearchTestCase tCase = new StringSearchTestCase();
        EParameter testIDPar = EResultDescription.getTestIDParameter("Test-" + Integer.toString(lineNumber));
        tCase.addParameter(testIDPar);

        EParameter parameter1 = new EParameter("Test", "Test name", ParameterType.STRING, testName);
        EParameter parameter2 = new EParameter("N", "Number of elements", ParameterType.INT,    probSize);
        EParameter parameter3 = new EParameter("Pattern", "A name of a pattern", ParameterType.STRING, pattern);
        EParameter parameter4 = new EParameter("testFile", "A name of a test file", ParameterType.STRING, testFile);

        // ID
        tCase.addParameter(testIDPar);
        // input parameters
        tCase.addParameter(parameter1);
        tCase.addParameter(parameter2);
        tCase.addParameter(parameter3);
        tCase.addParameter(parameter4);

        // TODO: set testcase data fields
        tCase.setCharText(text);
        tCase.setCharPattern(pattern);
        tCase.text = text;
        tCase.pattern = pattern;
        //tCase.offset = -2;

        return tCase;
        }

public static String readFile(String file, String csName) throws IOException {
    Charset cs = Charset.forName(csName);
    return readFile(file, cs);
}

public static String readFile(String file, Charset cs) throws IOException {
    try (FileInputStream stream = new FileInputStream(file)) {
        Reader reader = new BufferedReader(new InputStreamReader(stream, cs));
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[8192];
        int read;
        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
            builder.append(buffer, 0, read);
        }
        return builder.toString();
    }        
}
  
// TEST    
public static void main(String args[]) {
    //String root         = "/Users/Tomaz/Dropbox/FRI/ALGator/development/ALGator/test_data_root"; // a folder with the "projects" folder
    String root = "I:\\sola\\Diploma\\ALGator\\data_root";
    String projName = "StringSearch";

    ETestSet testSet = ATTools.getFirstTestSetFromProject(root, projName);
    StringSearchTestSetIterator stsi = new StringSearchTestSetIterator();
    stsi.setTestSet(testSet);

    ATTools.iterateAndPrintTests(stsi);
}
  
}
 