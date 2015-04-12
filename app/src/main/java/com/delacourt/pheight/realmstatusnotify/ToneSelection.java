package com.delacourt.pheight.realmstatusnotify;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Field;


public class ToneSelection extends ListActivity {

    private MediaPlayer mMediaPlayer;

    String[] toneNames;

    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tone_selection);

        try{
            toneNames = getAllRawResources();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, toneNames);

        setListAdapter(adapter);

    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {
        Resources res = getBaseContext().getResources();
        int soundId = res.getIdentifier(toneNames[position], "raw", getBaseContext().getPackageName());
        play(getBaseContext(), soundId);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("userTone", toneNames[position]);

        // Commit the edits!
        editor.commit();
    }


    private String[] getAllRawResources() {

        if (R.raw.class.getDeclaredFields() == null) {
            throw new NullPointerException();
        }
        assert R.raw.class.getDeclaredFields() != null : "No files added to raw folder";
        Field fields[] = R.raw.class.getDeclaredFields() ;
        String[] names = new String[fields.length] ;

        try {
            for( int i=0; i< fields.length; i++ ) {
                Field f = fields[i] ;
                names[i] = f.getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return names ;
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void play(Context c, int rid) {
        stop();

        mMediaPlayer = MediaPlayer.create(c, rid);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stop();
            }
        });

        mMediaPlayer.start();
    }

}
