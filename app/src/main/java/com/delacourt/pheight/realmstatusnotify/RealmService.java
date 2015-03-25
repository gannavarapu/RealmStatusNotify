package com.delacourt.pheight.realmstatusnotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class RealmService extends Service {

    private static final String TAG = "RealmService";
    private String SELECTED_REALMS_FILE = "SelectedRealms";
    ArrayList<HashMap<String, String>> realmsSelected;
    HashMap<String, String> tempRealm;

    Integer numNotify;
    String statusRealm;
    String nameRealm;

    Boolean activeThread = true;

    // JSON Node names
    private static final String TAG_REALMS = "realms";
    private static final String TAG_STATUS = "status";
    private static final String TAG_NAME = "name";

    ArrayList<HashMap<String, String>> contactList;

    // realms JSONArray
    JSONArray realms = null;

    // Hide api key in another file
    API key = new API();
    String APIkey = key.getAPI();
    String url = "https://us.api.battle.net/wow/realm/status?locale=en_US&apikey=" + APIkey;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Congrats! MyService Created", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onCreate");

        numNotify = 0;

        contactList = new ArrayList<HashMap<String, String>>();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart");
        try {
            // Read in previously selected realms
            InputStream inputStream = openFileInput(SELECTED_REALMS_FILE);
            File file = getBaseContext().getFileStreamPath(SELECTED_REALMS_FILE);
            if (inputStream != null) {
                FileInputStream fileIn = openFileInput(SELECTED_REALMS_FILE);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                realmsSelected = (ArrayList<HashMap<String, String>>) in.readObject();
                in.close();
            }

        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            Toast.makeText(this, "No realms have been selected.", Toast.LENGTH_LONG).show();
            this.onDestroy();
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException cnf) {
            //TODO
        }

        recursiveThread();

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "MyService Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
        activeThread = false;
    }

    private void recursiveThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Creating service handler class instance
                ServiceHandler sh = new ServiceHandler();

                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                Log.d("Response: ", "> " + jsonStr);

                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        realms = jsonObj.getJSONArray(TAG_REALMS);

                        // looping through All Realms
                        for (int i = 0; i < realms.length(); i++) {
                            JSONObject c = realms.getJSONObject(i);

                            String status = c.getString(TAG_STATUS);
                            String name = c.getString(TAG_NAME);

                            // tmp hashmap for single contact
                            HashMap<String, String> contact = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            contact.put(TAG_STATUS, status);
                            contact.put(TAG_NAME, name);

                            // adding contact to contact list
                            contactList.add(contact);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ServiceHandler", "Couldn't get any data from the url");
                }

                for (int n = 0; n < realmsSelected.size(); ++n) {
                    tempRealm = realmsSelected.get(n);
                    if ( contactList.contains(tempRealm)) {
                        // Remove the object and then change the status
                        // Add the new object back into realmsSelected
                        realmsSelected.remove(n);
                        nameRealm = tempRealm.get("name");
                        statusRealm = tempRealm.get("status");
                        if (statusRealm.equals("true")) {
                            tempRealm.clear();
                            tempRealm.put("status", "false" );
                            tempRealm.put("name", nameRealm);
                        } else {
                            tempRealm.clear();
                            tempRealm.put("status", "true");
                            tempRealm.put("name", nameRealm);
                        }
                        realmsSelected.add(tempRealm);

                        // Write the new selected realm list to file
                        try {
                            FileOutputStream fileOut = openFileOutput(SELECTED_REALMS_FILE, MODE_WORLD_READABLE);
                            ObjectOutputStream outputStream = new ObjectOutputStream(fileOut);
                            outputStream.writeObject(realmsSelected);
                            outputStream.flush();
                            outputStream.close();
                            fileOut.close();
                        } catch (FileNotFoundException fnf) {
                            //TODO
                        } catch (IOException e) {
                            //TODO
                        }
                        // Change status
                        // Notify user through Bar Notifications
                        // Increase the notification #
                        numNotify++;
                        // Change variable from true to up or false to down.
                        if (tempRealm.get("status").equals("true")) {
                            statusRealm = "up";
                        } else {
                            statusRealm = "down";
                        }

                        sendNotification(tempRealm.get("name"), statusRealm);
                    }

                }
                contactList.clear();
                Log.d(TAG, "I'm still running");
                if (activeThread) {
                    SystemClock.sleep(5000);
                    recursiveThread();
                }
            }
        }).start();
    }

    private void sendNotification(String realmName, String realmStatus) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Realm Status")
                        .setContentText(realmName + " is now " + realmStatus);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(numNotify, mBuilder.build());
    }
}
