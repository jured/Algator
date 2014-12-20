package si.fri.algotest.entities;

import java.io.Serializable;


/**
 * General test case with its parameters. Only basic funcionality of
 * test is supported in this class. Usually a subclass of this class
 * is needed to cover the needs of a given problem. 
 * @author tomaz
 */
public class TestCase implements Serializable {

  protected ParameterSet parameters;

  public TestCase() {
    parameters = new ParameterSet();
  }

  
  public String toString() {
    return parameters.toString();
  }
  
  
  public void addParameter(EParameter parameter) {
    parameters.addParameter(parameter, true);
  }
  
  public ParameterSet getParameters() {
    return parameters;
  }
  
}
