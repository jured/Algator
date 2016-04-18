package si.fri.algotest.entities;

import java.io.Serializable;


/**
 * General test case with its parameters. Only basic funcionality of
 * test is supported in this class. Usually a subclass of this class
 * is needed to cover the needs of a given problem. 
 * @author tomaz
 */
public class TestCase implements Serializable {

  protected VariableSet parameters;

  public TestCase() {
    parameters = new VariableSet();
  }

  
  public String toString() {
    return parameters.toString();
  }
  
  
  public void addParameter(EVariable parameter) {
    addParameter(parameter, true);
  }
  
  public void addParameter(EVariable parameter, boolean replaceExisting) {
    parameters.addVariable(parameter, replaceExisting);
  }
  
  public VariableSet getParameters() {
    return parameters;
  }
  
}
