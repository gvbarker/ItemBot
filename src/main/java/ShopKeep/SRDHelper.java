package ShopKeep;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

public class SRDHelper {
    private int numMundane;
    private int numMagical;
    private String[] filters;
    private JSONArray requestedItems;

    public SRDHelper(int numMundane, int numMagical, String[] filters) {
        this.numMundane = numMundane;
        this.numMagical = numMagical;
        this.filters = filters;
    }
    public SRDHelper(int numMundane, int numMagical) {
        this.numMundane = numMundane;
        this.numMagical = numMagical;
    }

    public JSONArray getRequestedItems() {
        return this.requestedItems;
    }
    private JSONArray generateRequestedMundaneItems(int numIterations) {

        return;
    }
    public void generateRequestedItems(int numIterations) {
        JSONArray[] retArr = new JSONArray[numIterations];
        for (int i = 0; i < numIterations; i++) {
            JSONArray mundanes = generateRequestedMundaneItems(this.numMundane);

        }
    }

    /*
    vars: Int numMundane, Int numMagical, Array numFilters
    functions:
    getSRDLists(Boolean mun, Boolean mag)
    filterSRDList(Array filter, JSONArray list)
    getMundanes(JSONArray filtered)
    getMagicals(JSONArray filtered)
    concatJSONArrays(Array lists)
    generateRequestedItems(int numIterations)
    getRequestedItems()
    * */
}
