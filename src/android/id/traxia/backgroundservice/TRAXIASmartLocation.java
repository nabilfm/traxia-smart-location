package id.traxia.backgroundservice;

/**
 * Created by perdi on 8/3/2017.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.content.Intent;
import android.location.*;

import net.traxia.roadtek.R;

import org.apache.cordova.firebase.FirebasePlugin;
import org.apache.cordova.firebase.OnNotificationOpenReceiver;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.regex.Pattern;

import id.traxia.API.getAPI;
import id.traxia.plugin.StringParser;

public class TRAXIASmartLocation extends Service implements LocationListener {

    private static getAPI api;
    private Context context = this;
    private static Context mContext;

    private static Thread thread;
    private static Handler handler;
    private Vibrator vibrator;
    private static Runnable runnable;
    private static int limit = 0;
    private static boolean stopRunnable = false;
    private static PowerManager pm;
    private static WakeLock wl;
    private static JSONObject object;
    private static JSONObject object1;
    private static String result;
    private static int requestid;
    private Intent intent;
    private double lat, lon;

    // flag for GPS status
    private static boolean isGPSEnabled = false;

    // flag for network status
    private static boolean isNetworkEnabled = false;

    // flag for GPS status
    private static boolean canGetLocation = false;

    private static Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;


    public Location getLocation(Context mContext) {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
             isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                this.canGetLocation = false;
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled || location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    //if (location == null) {
                    Location tmpLoc;
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            tmpLoc = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (tmpLoc != null) {
                                location = tmpLoc;
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    //}
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }



    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(TRAXIASmartLocation.this);
        }
    }

    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private final IBinder binder = new BackgroundServiceBinder();

    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return super.onUnbind(intent);
    }

    public class BackgroundServiceBinder extends Binder{
        public TRAXIASmartLocation getServiceTRAXIA() {
            return TRAXIASmartLocation.this;
        }
    }

    @Override
    public void onCreate(){
        startRunnable();
    }
    @Override
    public void onDestroy(){
        stopRunnable = true;
        handler.removeCallbacks(runnable);
        try{
            wl.release();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    @Override
    public void onStart(Intent intent, int startid) {

    }

    private void startRunnable() {
        final TRAXIASmartLocation smartLocation = new TRAXIASmartLocation();

        pm =(PowerManager)getApplicationContext().getSystemService(
                getApplicationContext().POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MainActivity");

        wl.acquire();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                api = new getAPI();
                int time = 30000;
                SharedPreferences preferences = getSharedPreferences("localStorage", Context.MODE_PRIVATE);
                String localData = preferences.getString("localData", ""),
                        dodata = preferences.getString("DOData", ""),
                        id = "0",
                        title = "Traxia Smart Logistic",
                        mBody = "Anda sudah sampai ",
                        arrivedDO = preferences.getString("arrivedEndDO", ""),
                        loadDO = preferences.getString("loadDO", "");

                Editor editor = preferences.edit();

                StringParser parser = new StringParser();

                if (localData != ""){
                    String isDriver = parser.getJSONData(localData, "DriverID");
                    if (!isDriver.equalsIgnoreCase("Error")){
                        if (dodata != ""){
                            smartLocation.getLocation(context);
                            if (smartLocation.canGetLocation()) {
                                if (!preferences.getBoolean("isGPSStateOnSent", false)){
                                    String method = "POSTGPSStateUser";
                                    try {
                                        if (api.postGPSState(localData, method, true)){
                                            editor.putBoolean("isGPSStateOnSent", true)
                                                    .putBoolean("isGPSStateOffSent", false)
                                                    .commit();
                                        }
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                lat = smartLocation.getLatitude();
                                lon = smartLocation.getLongitude();
                                String[] result = compareData(lat, lon, dodata);
                                //Bundle data = new Bundle();
                                //data.putInt("id", 1);
                                //data.putString("body", "Anda Sudah Sampai Tujuan");
                                //data.putBoolean("tap", false);
                                //data.putString("tittle","Sampai");
                                //Toast.makeText(context, test + distances, Toast.LENGTH_LONG).show();
                                if (Boolean.valueOf(result[0])) {
                                    if (Double.valueOf(result[1]) <= 500){
                                        time = 5000;
                                        if (Double.valueOf(result[1]) <= 300){
                                            if (!result[2].equalsIgnoreCase("")){
                                                if (!arrivedDO.contains(result[4])){
                                                    boolean showNotification = ((FirebasePlugin.inBackground() || !FirebasePlugin.inBackground()) || !FirebasePlugin.hasNotificationsCallback());
                                                    mBody += "di lokasi bongkar muatan " + result[2];
                                                    id = result[4];
                                                    if (arrivedDO.equalsIgnoreCase("")) arrivedDO += "{" + id +"}";
                                                    else arrivedDO += ",{" + id +"}";

                                                    editor.putString("arrivedEndDO", arrivedDO).commit();
                                                    sendNotification(id, title, mBody, "ARRIVED", showNotification);
                                                }
                                            }
                                            else if (!result[3].equalsIgnoreCase("")){
                                                if (!loadDO.contains(result[4])){
                                                    boolean showNotification = ((FirebasePlugin.inBackground() || !FirebasePlugin.inBackground()) || !FirebasePlugin.hasNotificationsCallback());
                                                    mBody += "di lokasi pemuatan " + result[3];
                                                    id = result[4];
                                                    if (loadDO.equalsIgnoreCase("")) loadDO += "{" + id +"}";
                                                    else loadDO += ",{" + id +"}";

                                                    editor.putString("loadDO", loadDO).commit();
                                                    sendNotification(id, title, mBody, "ARRIVED", showNotification);
                                                }
                                            }
                                        }
                                    }

                                    else if (Double.valueOf(result[1]) <= 1000){
                                        time = 10000;
                                    }

                                    else if (Double.valueOf(result[1]) <= 1500) time = 30000;

                                    else time = 60000;
                                }
                            }

                            else {
                                if (!preferences.getBoolean("isGPSStateOffSent", false)){
                                    String method = "POSTGPSStateUser";
                                    try {
                                        if (api.postGPSState(localData, method, false)){
                                            editor.putBoolean("isGPSStateOffSent", true)
                                                    .putBoolean("isGPSStateOnSent", false)
                                                    .commit();
                                        }
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
                handler.postDelayed(runnable, time);
            }
        };

        handler.post(runnable);
    }

    private String[] compareData (double lat, double lon, String dodata) {
        String[] dataReturn = new String[]{ "false" };
        String[] doArray;
        double latTo, lonTo, latFrom, lonFrom;

        try {
            doArray = dodata.split(Pattern.quote("},"));
            int l = doArray.length;
            double distancePoint = 0, tmpEndDistances, tmpStartDistnaces;
            String s, unloadLocation = "", doid = "0", loadLocation = "";
            for (int i = 0; i < l; i++){
                s = doArray[i] + "}";
                JSONObject object = new JSONObject(s);

                latTo = object.getDouble("LatTo");
                lonTo = object.getDouble("LonTo");

                latFrom = object.getDouble("LatFrom");
                lonFrom = object.getDouble("LonFrom");

                tmpEndDistances = distances(lat, lon, latTo, lonTo);

                unloadLocation = object.getString("AddressTo");
                doid = object.getString("DOID");

                if (i == 0) distancePoint = tmpEndDistances;
                else if (tmpEndDistances < distancePoint){
                    distancePoint = tmpEndDistances;
                    unloadLocation = object.getString("AddressTo");
                    loadLocation = "";
                }

                if (Math.abs(latFrom) > 0 || Math.abs(lonFrom) > 0) {
                    tmpStartDistnaces = distances(lat, lon, latFrom, lonFrom);
                    if (tmpStartDistnaces < distancePoint){
                        distancePoint = tmpStartDistnaces;
                        loadLocation = object.getString("AddressFrom");
                        unloadLocation = "";
                    }
                }
            }

            dataReturn = new String[] { "true", Double.toString(distancePoint), unloadLocation, loadLocation, doid };
        }
        catch (JSONException e){
            System.out.println(e.getMessage().toString());
        }
        catch (Exception e) {
            System.out.println(e.getMessage().toString());
        }

        return dataReturn;
    }

    private double distances(double latLoc, double lonLoc, double latLoc1, double lonLoc1){
        Location startPoint = new Location("locationA");
        startPoint.setLatitude(latLoc);
        startPoint.setLongitude(lonLoc);

        Location endPoint = new Location("locationB");
        endPoint.setLatitude(latLoc1);
        endPoint.setLongitude(lonLoc1);

        return startPoint.distanceTo(endPoint);
    }

    private void sendNotification(String id, String title, String messageBody, String intentWindow, boolean showNotification) {
        Bundle bundle = new Bundle();

        bundle.putBoolean("tap", false);
        bundle.putString("title", title);
        bundle.putString("body", messageBody);
        bundle.putString("id", id);
        bundle.putString("intentwindow", intentWindow);
        bundle.putString("Attribute", "{\"doid\", \"" + id + "\"}");
        FirebasePlugin.sendNotification(bundle);

        if (showNotification) {
            Intent intent = new Intent(context, OnNotificationOpenReceiver.class);
            intent.putExtras(bundle);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id.hashCode(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Uri warningSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.warning);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            if (intentWindow.equalsIgnoreCase("ARRIVED")){
                notificationBuilder.setSound(warningSound)
                        .setVibrate(new long[] {1000, 2000, 1000, 2000, 1000, 2000, 1000, 2000});
            }
            else {
                notificationBuilder.setSound(defaultSoundUri)
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000 });
            }

            int resID = context.getResources().getIdentifier("notification_icon", "drawable", context.getPackageName());
            if (resID != 0) {
                notificationBuilder.setSmallIcon(resID);
            } else {
                notificationBuilder.setSmallIcon(context.getApplicationInfo().icon);
            }

            Notification notification = notificationBuilder.build();

            notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(id.hashCode(), notification);
        } else {
            bundle.putBoolean("tap", false);
            bundle.putString("title", title);
            bundle.putString("body", messageBody);
            FirebasePlugin.sendNotification(bundle);
        }
    }
}
