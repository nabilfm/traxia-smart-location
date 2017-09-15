package id.traxia.plugin;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by perdi on 8/21/2017.
 */

public class StringParser {

    public String getJSONData(String s, String key){
        try {
            String returnVal;
            JSONObject object = new JSONObject(s);
            return object.getString(key);
        }
        catch (JSONException e){
            return "Error";
        }
    }
}
