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




    public SRDHelper(int numMundane, int numMagical, String[] filters) {
        this.numMundane = numMundane;
        this.numMagical = numMagical;
        this.filters = filters;
    }
    public SRDHelper(int numMundane, int numMagical) {
        this.numMundane = numMundane;
        this.numMagical = numMagical;
    }
    public SRDHelper() {

    }
    public JSONObject getSRDItem(String itemName, String type) {
        JSONObject returnItem = new JSONObject();
        JSONParser parser = new JSONParser();
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
        //this is lazy, but I'm tired and return values complain if originating in lambda forEach functions
        for (int i = 0; i < itemList.size(); i++) {
            JSONObject item = (JSONObject) itemList.get(i);
            if (item.get("name").equals(itemName)) {
                returnItem = item;
            }
        }
        return returnItem;
    }
    private boolean checkRemoveItem(JSONObject item) {
        for (String rule : this.filters) {
            item = (JSONObject) item.get("equipment_category");
            String index = (String) item.get("index");
            boolean filterCheck = (rule.equals(index));
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
        JSONArray items = new JSONArray();
        if (count < 1) { return items; }
        JSONParser parser = new JSONParser();
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
        itemList = (filters.length < 1) ? itemList : filterSRDList(itemList);
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
            retArr[i].addAll(magical);
        }
        return retArr;
    }
}
