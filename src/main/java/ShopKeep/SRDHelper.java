package ShopKeep;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

import java.util.*;

public class SRDHelper {
    private int numMundane;
    private int numMagical;
    private String[] filters;
    private JSONArray requestedItems;

    private final Map<String, String> filterMap = new HashMap<>(Map.of(
            "-no-vehicles", "mounts-and-vehicles",
            "-no-weapons", "weapon",
            "-no-armors", "armor"
    ));


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
    private void printJSONArray(JSONArray jarr) {
        jarr.forEach(item -> {
            JSONObject it = (JSONObject) item;
            System.out.println( (String) it.get("name"));
        });

    }
    private boolean checkRemoveItem(JSONObject item) {
        for (String rule : this.filters) {
            item = (JSONObject) item.get("equipment_category");
            String index = (String) item.get("index");
            boolean filterCheck = (filterMap.get(rule).equals(index));
            if (filterCheck) {
                return true;
            }
        }
        return false;
    }

    private JSONArray filterSRDList(JSONArray arr) {
        JSONArray filteredList = new JSONArray();
        arr.forEach(entry -> {
            JSONObject item = (JSONObject) entry;
            if (!checkRemoveItem(item)) {
                filteredList.add(item);
            }
        });
        return filteredList;
    }

    private JSONArray generateItems(int count, String type) {
        JSONParser parser = new JSONParser();
        JSONArray items = new JSONArray();
        String srdPath = (type.equals("mundane")) ? "src/main/resources/5e-SRD-Equipment.json" : "src/main/resources/5e-SRD-Magic-Items.json";
        Object srdContent;
        Random rand = new Random();
        try (FileReader reader = new FileReader(srdPath)) {
            srdContent = parser.parse(reader);
        }
        catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        JSONArray itemList = (JSONArray) srdContent;
        itemList = filterSRDList(itemList);
        for (int i = 0; i < count; i++) {
            //noinspection unchecked
            items.add(itemList.get(rand.nextInt(itemList.size())));
        }
        return items;
    }

    public JSONArray[] generateRequestedItems(int numIterations) {
        JSONArray[] retArr = new JSONArray[numIterations];
        for (int i = 0; i < numIterations; i++) {
            JSONArray mundane = generateItems(this.numMundane, "mundane");
            retArr[i] = mundane;
            JSONArray magical = generateItems(this.numMagical, "magical");
            retArr[i].add(magical);
        }
        return retArr;
    }
    /*
    vars: Int numMundane, Int numMagical, Array numFilters
    functions:
    getSRDLists(Boolean mun, Boolean mag)
    filterSRDList(Array filter, JSONArray list)

    concatJSONArrays(Array lists)
    generateRequestedItems(int numIterations)
    getRequestedItems()
    * */
}
