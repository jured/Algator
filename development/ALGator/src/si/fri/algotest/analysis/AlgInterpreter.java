package si.fri.algotest.analysis;

import bsh.EvalError;
import bsh.Interpreter;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import si.fri.algotest.global.ATLog;

/**
 *
 * @author ernest, judita
 */
public class AlgInterpreter {

    private static Interpreter interpreter;
    private static final String[] stringsNotAllowed = new String[]{"{", "}"};
    public static final String[] mathMembers = new String[]{"abs", "ceil", "floor", "max", "min", "pow", "random", "round", "signum", "sqr"};

    private static Interpreter GetInterpreter() {
        if (interpreter == null) {
            interpreter = new Interpreter();
        }
        return interpreter;
    }

    public static String prepareExpression(String expression) {
        for (String str : stringsNotAllowed) {
            if (expression.contains(str)) {
                ATLog.log("Illegal character in expression " + expression, 3);
                return "";
            }
        }
        expression = expression.replace(";", ",");
        for (String mb : mathMembers) {
            expression = expression.replace(mb + "(", "Math." + mb + "(");
        }
        return expression;
    }

    public static Object evalExpression(String expression) {
/*        Interpreter ip = GetInterpreter();
        try {
            ip.eval("result=" + expression);
            return ip.get("result");
        } catch (EvalError ex) {
            return "?";
        }
*/
       Expression e = new ExpressionBuilder(expression).build();
       double result = e.evaluate();
       return result;
    }

}
