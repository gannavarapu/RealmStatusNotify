package com.delacourt.pheight.realmstatusnotify;

import android.app.ActivityManager;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class RealmSelection extends ListActivity {

    public final static String EXTRA_MESSAGE = "com.delacourt.pheight.realmstatusnotify.MESSAGE";

    public final static String FILENAME = "Realms";

    private ProgressDialog pDialog;

    // Hide api key
    API key = new API();
    String APIkey = key.getAPI();
    String url = "https://us.api.battle.net/wow/realm/status?locale=en_US&apikey=" + APIkey;

    public String onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        // Comment out until Sprint 2
        /*switch (view.getId()) {
            case R.id.united_states:
                if (checked)
                    url = "https://us.api.battle.net/wow/realm/status?locale=en_US&apikey=" + APIkey;
                break;
            case R.id.es_mx:
                if (checked)
                    url = "https://us.api.battle.net/wow/realm/status?locale=es_MX&apikey=" + APIkey;
                break;
            case R.id.pt_br:
                if (checked)
                    url = "https://us.api.battle.net/wow/realm/status?locale=pt_BR&apikey=" + APIkey;
                break;
        } */

        return url;
    }

    // JSON Node names
    private static final String TAG_REALMS = "realms";
    private static final String TAG_STATUS = "status";
    private static final String TAG_NAME = "name";

    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;

    EditText inputSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_selection);

        contactList = new ArrayList<HashMap<String, String>>();

        ListView lv = getListView();

        //Check to see if file is empty. If it is populate it with data from server.
        String ret = "";

        try {
            InputStream inputStream = openFileInput(FILENAME);
            File file = getBaseContext().getFileStreamPath(FILENAME);
            if ( inputStream != null ) {
                FileInputStream fileIn = openFileInput(FILENAME);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                contactList = (ArrayList<HashMap<String, String>>) in.readObject();
                in.close();
            }
            else
            {
                new GetContacts().execute();
            }

            inputStream.close();

            ListAdapter adapter = new SimpleAdapter(
                    RealmSelection.this, contactList,
                    R.layout.list_item, new String[]{TAG_NAME, TAG_STATUS}, new int[]{R.id.name, R.id.status});

            setListAdapter(adapter);
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            new GetContacts().execute();
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException cnf) {
            //TODO
        }


    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(RealmSelection.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    contacts = jsonObj.getJSONArray(TAG_REALMS);

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

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

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    RealmSelection.this, contactList,
                    R.layout.list_item, new String[]{TAG_NAME, TAG_STATUS}, new int[]{R.id.name, R.id.status});

            setListAdapter(adapter);

            // Put the realms into a retrievable file
            try {
                FileOutputStream fileOut = openFileOutput(FILENAME,MODE_WORLD_READABLE);
                ObjectOutputStream outputStream = new ObjectOutputStream(fileOut);
                outputStream.writeObject(contactList);
                outputStream.flush();
                outputStream.close();
                fileOut.close();
            }
            catch (FileNotFoundException fnf)
            {
                //TODO
            }
            catch (IOException e)
            {
                //TODO
            }
        }


    }

}