package id.traxia.API;

/**
 * Created by perdi on 8/4/2017.
 */

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.UUID;
import java.net.*;
import org.json.JSONException;

import javax.net.ssl.HttpsURLConnection;

public class postAPI{

    public class postServer extends AsyncTask<String, Integer, String> {
        protected void onPreExecute() {
            super.onPreExecute();

            // Do something like display a progress bar
        }

        // This is run in a background thread\
        protected String doInBackground(String... params) {
            // get the string from params, which is an array
            try {
                JSONObject jsonObject = new JSONObject(params[0]);
                String apiServer = "http://traxiasmapi.azurewebsites.net/TraxiaAPI/service.asmx/";
                URL url = new URL(apiServer + params[1]);
                OutputStreamWriter os;
                InputStream in;

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setInstanceFollowRedirects(false);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setConnectTimeout(10000);
                os = new OutputStreamWriter(con.getOutputStream());
                os.write(jsonObject.toString());
                os.close();

                in = new BufferedInputStream(con.getInputStream());

                byte[] contents = new byte[1024];
                String result = "";
                int bytesRead = 0;
                while ((bytesRead = in.read(contents)) != -1) {
                    result += new String(contents, 0, bytesRead);
                }

                in.close();
                con.disconnect();

                jsonObject = new JSONObject(result);
                jsonObject.put("resultcode", 200);
                result = jsonObject.toString();

                return result;
            } catch (JSONException e) {
                System.out.println(e);
                return "{\"resultcode\": 500}";
            } catch (MalformedURLException e) {
                System.out.println(e);
                return "{\"resultcode\": 500}";
            } catch (ConnectException e) {
                System.out.println(e);
                return "{\"resultcode\": 500}";
            } catch (Exception e) {
                System.out.println(e);
                return "{\"resultcode\": 500}";
            }
        }

        // This is called from background thread but runs in UI\
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes\
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Do things like hide the progress bar or change a TextView
        }
    }

    public String postData(String... params){
        postServer server= new postAPI.postServer();
        try {
            return server.execute(params).get();
        }
        catch (Exception e)
        {
            return e.toString();
        }
    }
}
