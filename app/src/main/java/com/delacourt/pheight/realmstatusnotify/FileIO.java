package com.delacourt.pheight.realmstatusnotify;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class FileIO {
    //Populates variable from file
    public ArrayList<HashMap<String, String>> loadVariableFromFile( String fileName, Context context) throws IOException, ClassNotFoundException {
        ArrayList<HashMap<String, String>> returnVariable = null;
        InputStream inputStream = context.openFileInput(fileName);
        if (inputStream != null) {
            FileInputStream fileIn = context.openFileInput(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            returnVariable = (ArrayList<HashMap<String, String>>) in.readObject();
            in.close();
        }
        inputStream.close();

        return returnVariable;
    }

    // Saves variable data to file for later retrieval.
    public void outputToFile(ArrayList<HashMap<String, String>> variableName, String fileName, Context context) throws IOException {
        FileOutputStream fileOut = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ObjectOutputStream outputStream = new ObjectOutputStream(fileOut);
        outputStream.writeObject(variableName);
        outputStream.flush();
        outputStream.close();
        fileOut.close();

    }

}
