package com.delacourt.pheight.realmstatusnotify;


import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends ActionBarActivity {

    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.mainMenu);

        // Defined Array values to show in ListView
        String[] values = new String[] { "Select Realm", "Notification Settings"
        };

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);


        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0 ) {
                    viewRealms(view);
                }

                if (position == 1) {
                    viewNotify(view);
                }

                }

        });
    }

    /** Called when the user clicks the Send button */
    public void viewRealms(View view) {
        Intent intent = new Intent(this, RealmSelection.class);
        startActivity(intent);
    }

    public void viewNotify(View view) {
        Intent intent = new Intent(this, NotifySettings.class);
        startActivity(intent);
    }
}