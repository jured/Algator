package si.fri.algotest.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import si.fri.algotest.entities.EResult;
import si.fri.algotest.entities.VariableSet;

/**
 *
 * @author ernest, judita
 */
public class ReadResultsInitData {

    public ArrayList<String> resultOrder = new ArrayList<>();

    public ArrayList<String> testOrder = new ArrayList<>();

    public VariableSet resultPS;

    public EResult eResultDesc;

    public ReadResultsInitData(String[] rOrder, String[] tOrder, VariableSet resultVS, EResult resultDesc) {
        resultOrder.addAll(Arrays.asList(rOrder));
        testOrder.addAll(Arrays.asList(tOrder));
        resultPS = resultVS;
        eResultDesc = resultDesc;
    }

}
