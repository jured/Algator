import si.fri.algotest.entities.EParameter;
import si.fri.algotest.entities.ParameterSet;
import si.fri.algotest.entities.ParameterType;
import si.fri.algotest.entities.TestCase;
import si.fri.algotest.execute.AbsAlgorithm;
import si.fri.algotest.global.ErrorStatus;

/**
 *
 * @author Anze Pratnemer
 */
public abstract class StringSearchAbsAlgorithm extends AbsAlgorithm {

  StringSearchTestCase stringSearchTestCase;
  int resultOffset;

  @Override
  public ErrorStatus init(TestCase test) {
    if (test instanceof StringSearchTestCase) {
      stringSearchTestCase = (StringSearchTestCase) test;
      return ErrorStatus.STATUS_OK;
    } else
      return ErrorStatus.setLastErrorMessage(ErrorStatus.ERROR_CANT_PERFORM_TEST, "Invalid test:" + test);
  }

  @Override
  public void run() {
    execute(stringSearchTestCase.text, stringSearchTestCase.pattern, stringSearchTestCase.offset);
  }

/**
 *
 * @return
 */
@Override
  public ParameterSet done() {
    ParameterSet result = new ParameterSet(stringSearchTestCase.getParameters());
    //System.out.println("Offset v testu: " + stringSearchTestCase.offset);
    String ok = stringSearchTestCase.offset > -1 && stringSearchTestCase.offset < stringSearchTestCase.text.length()-stringSearchTestCase.pattern.length() ? "OK" : "NOK";

    EParameter passParA = new EParameter("Offset",  "", ParameterType.INT, stringSearchTestCase.offset);
    EParameter passParB = new EParameter("TextLen", "", ParameterType.INT, stringSearchTestCase.text.length());
    EParameter passParC = new EParameter("PattLen", "", ParameterType.INT, stringSearchTestCase.pattern.length());
    result.addParameter(passParA, true);
    result.addParameter(passParB, true);
    result.addParameter(passParC, true);

    
    EParameter passPar = new EParameter("Check", "", ParameterType.STRING, ok);
    result.addParameter(passPar, true);
    

    return result;
  }

  // TODO: define parameters for the execute method
  protected abstract void execute(String text, String pattern, int offset);

}