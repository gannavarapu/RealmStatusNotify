package com.delacourt.pheight.realmstatusnotify;


import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class RealmSelection extends ListActivity {

    // Set default filenames
    private String FILENAME = "USrealms";
    private String SELECTED_REALMS_FILE = "SelectedRealms";


    private ProgressDialog pDialog;

    // JSON Node names
    private static final String TAG_REALMS = "realms";
    private static final String TAG_STATUS = "status";
    private static final String TAG_NAME = "name";

    // realms JSONArray
    JSONArray realms = null;

    // List for storing and removing selected realms
    ArrayList<HashMap<String, String>> selectedRealms = new ArrayList<HashMap<String, String>>();

    // Hide api key in another file
    API key = new API();
    String APIkey = key.getAPI();
    String url = "https://us.api.battle.net/wow/realm/status?locale=en_US&apikey=" + APIkey;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> contactList;

    EditText inputSearch;

    FileIO fileManipulation = new FileIO();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_selection);

        inputSearch = (EditText) findViewById(R.id.inputSearch);
        contactList = new ArrayList<HashMap<String, String>>();

        try {
            selectedRealms = fileManipulation.loadVariableFromFile(SELECTED_REALMS_FILE, getBaseContext());
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException cnf) {
        //TODO
        }

        findRealms();

        inputSearch.addTextChangedListener(new EditTextWatcher());
    }

    private class EditTextWatcher implements TextWatcher {

        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1,
                                      int arg2, int arg3) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
            // TODO Auto-generated method stub
            ((SimpleAdapter) RealmSelection.this.getListAdapter()).getFilter().filter(arg0);
        }
    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.united_states:
                if (checked)
                    url = "https://us.api.battle.net/wow/realm/status?locale=en_US&apikey=" + APIkey;
                FILENAME = "USrealms";
                contactList.clear();
                findRealms();
                break;
            case R.id.europe:
                if (checked)
                    url = "https://eu.api.battle.net/wow/realm/status?locale=es_MX&apikey=" + APIkey;
                FILENAME = "EUrealms";
                contactList.clear();
                findRealms();
                break;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Do something when a list item is clicked
        TextView txtTechCharacteristic = (TextView) v.findViewById(R.id.name);
        TextView txtTechCharacteristic2 = (TextView) v.findViewById(R.id.status);
        String nameSTR = (String) txtTechCharacteristic.getText();
        String statusSTR = (String) txtTechCharacteristic2.getText();
        // tmp hashmap for single contact
        HashMap<String, String> realm = new HashMap<String, String>();

        // adding each child node to HashMap key => value
        realm.put(TAG_STATUS, statusSTR);
        realm.put(TAG_NAME, nameSTR);

        if (selectedRealms.contains(realm)) {
            selectedRealms.remove(realm);
        } else {
            selectedRealms.add(realm);

            // Store the list of selected realms into SharedPreferences for later retrieval
            try {
                fileManipulation.outputToFile(selectedRealms, SELECTED_REALMS_FILE, getBaseContext());
            } catch (FileNotFoundException fnf) {
                //TODO
            } catch (IOException e) {
                //TODO
            }
        }
    }



    private void findRealms() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo checkNetwork = connManager.getActiveNetworkInfo();

        // Check if network is connected
        // If it isn't, pull up realms from storage
        // If it is, pull down realms from server
        if (checkNetwork == null)
        {
            // If not connected
            try {
                contactList = fileManipulation.loadVariableFromFile(FILENAME, getBaseContext());

                ListAdapter adapter = new SimpleAdapter(
                        RealmSelection.this, contactList,
                        R.layout.list_item, new String[]{TAG_NAME, TAG_STATUS}, new int[]{R.id.name, R.id.status});

                setListAdapter(adapter);
            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
                new GetRealms().execute();
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            } catch (ClassNotFoundException cnf) {
                //TODO
            }
        } else

        {
            new GetRealms().execute();
        }
    }

    public class GetRealms extends AsyncTask<Void, Void, Void> {

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

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            // Updating parsed JSON data into ListView
            ListAdapter adapter = new SimpleAdapter(
                    RealmSelection.this, contactList,
                    R.layout.list_item, new String[]{TAG_NAME, TAG_STATUS}, new int[]{R.id.name, R.id.status});

            setListAdapter(adapter);

            // Put the realms into a retrievable file
            try {
                fileManipulation.outputToFile(contactList, FILENAME, getBaseContext());
            } catch (FileNotFoundException fnf) {
                //TODO
            } catch (IOException e) {
                //TODO
            }
        }


    }
}

