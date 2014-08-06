import si.fri.algotest.entities.TestCase;

/**
 *
 * @author Anze Pratnemer
 */
public class StringSearchTestCase extends TestCase {

  public String text;
  public String pattern;

  public char[] charText;
  public char[] charPattern;
  
  public int offset;
  
  public void setCharText(String text){
      charText = text.toCharArray();
  }
  
  public void setCharPattern(String pattern){
      charPattern = pattern.toCharArray();
  }

  private String arrayToString(char [] tab) {
    String result = "";
    for (int i = 0; i < Math.min(tab.length, 15); i++) {
      if (tab[i]>=32 && tab[i]<127)
        result += tab[i];
      else
        result +="?";
    }
    return result;
  }
  
  // TODO: define a string representation of this TestCase
  @Override
public String toString() {
    String tmpString = "";
    String tmpPattern = "";
    
    if(text.length() != 0) tmpString = text;
    if(pattern.length() != 0) tmpPattern = pattern;
    
    if(charText.length != 0)  tmpString    = arrayToString(charText);
    if(charPattern.length != 0) tmpPattern = arrayToString(charPattern);
    
    return "String to search in: " + tmpString + ", Pattern to search for: " + tmpPattern + ". Offset is: " + offset + ".";
  }
}
