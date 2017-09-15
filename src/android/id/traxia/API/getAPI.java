package id.traxia.API;

import org.json.JSONException;
import org.json.JSONObject;

import id.traxia.plugin.StringParser;

/**
 * Created by perdi on 8/4/2017.
 */


public class getAPI {
    private postAPI api;
    private StringParser parser;
    //apiServer: 'http://fetchapi.traxia.net/service.asmx';
    //apiServer: 'http://traxiafetchapi-sandbox.azurewebsites.net/service.asmx';
    public boolean postGPSState(String data, String url, boolean GPSState) {
        api = new postAPI();
        parser = new StringParser();
        String[] params;
        try {
            JSONObject object = new JSONObject();
            String id = parser.getJSONData(data, "UserID"),
                    token = parser.getJSONData(data, "Token"),
                    uuid = parser.getJSONData(data, "UUID");
            object.put("emailaddress", id);
            object.put("token", token);
            object.put("uuid", uuid);
            object.put("gpsstateuser", GPSState);
            params = new String[]{ object.toString(), url};
            object = new JSONObject(api.postData(params));

            if (object.getString("resultcode").equalsIgnoreCase("200")){
                if (object.getString("BoolACK").equalsIgnoreCase("ACK")){
                    return true;
                }
                else return false;
            }
            else return false;
        }
        catch (JSONException e){
            return false;
        }
    }
}
