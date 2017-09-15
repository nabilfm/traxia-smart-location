package id.traxia.plugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.apache.cordova.*;
import org.json.*;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Created by perdi on 8/7/2017.
 */

public class TRAXIALocationPlugin extends CordovaPlugin {
    private static CallbackContext callback;
    private SharedPreferences preferences;


    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext) throws JSONException{
        Context context =  cordova.getActivity();
        preferences = context.getSharedPreferences("localStorage", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        JSONObject object;

        if (action.equals("saveLocalData")){
            //activity.startService();
            String data = removeFirstandLast(args.getString(0));
            editor.putString("localData", data);
            editor.commit();

            object = new JSONObject(data);

            callbackContext.success(object);
            return true;
        }

        else if (action.equals("clearLocalData")){
            editor.putString("LocalData", "")
                    .putBoolean("isGPSStateOnSent", false)
                    .putBoolean("isGPSStateOffSent", false)
                    .commit();
            object = new JSONObject("{\"resultcode\" : 200}");
            callbackContext.success(object);
            return true;
        }

        else if (action.equals("saveDOData")){
            String data = removeFirstandLast(args.getString(0));
            editor.putString("DOData", data.replace('"', '\"'));
            editor.commit();

            object = new JSONObject(data);

            callbackContext.success(object);
            return true;
        }

        else if (action.equals("responseData")){
            callback = callbackContext;
            return true;
        }

        return false;
    }

    public void responseData(String s) {
        if (callback != null){
            try {
                JSONObject object = new JSONObject(s);
                PluginResult result = new PluginResult(PluginResult.Status.OK, object);
                result.setKeepCallback(true);
                callback.sendPluginResult(result);
            }
            catch (JSONException e){
                Log.e("ERROR", e.toString());
            }
            catch (Exception e) {
                Log.e("ERROR", e.toString());
            }
        }
    }

    public String removeFirstandLast(String s){
        if (s.substring(0, 1).equalsIgnoreCase("[")){
            s = s.substring(1);
        }
        if (s.substring(s.length()-1).equalsIgnoreCase("]")){
            s = s.substring(0, s.length()-1);
        }

        return s;
    }
}
