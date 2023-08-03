package ShopKeep;


import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

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

    /*
    vars: Int numMundane, Int numMagical, Array numFilters
    functions:
    getSRDLists(Boolean mun, Boolean mag)
    filterSRDList(Array filter, JSONArray list)
    getMundanes(JSONArray filtered)
    getMagicals(JSONArray filtered)
    concatJSONArrays(Array lists)
    return
    * */
}
