package com.delacourt.pheight.realmstatusnotify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class NotifySettings extends ActionBarActivity {

    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_settings);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.notificationMenu);

        // Defined Array values to show in ListView
        String[] values = new String[] { "Turn on Notifications", "Turn off Notifications" ,"Select Tone"
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
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (position == 0 ) {
                   startRealmService();
                }

                if (position == 1 ) {
                    stopRealmService();
                }

            }

        });
    }

    public void startRealmService() {
        startService(new Intent(this, RealmService.class));
    }

    public void stopRealmService() {
        stopService(new Intent(this, RealmService.class));
    }
}
