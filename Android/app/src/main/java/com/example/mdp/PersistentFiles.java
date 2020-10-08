package com.example.mdp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PersistentFiles {

    public static final String SHARED_PREFS = "sharedPrefs";

    Context context;

    public PersistentFiles (Context context) {
        this.context = context;
        if (loadDataString("v1").equals("")) {
            this.saveData("v1", "Default Value 1");
        }
        if (loadDataString("v2").equals("")) {
            this.saveData("v2", "Default Value 2");
        }
    }

    public void saveData(String name, String content) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, content);
        editor.apply();
    }

    public String loadDataString(String name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String theText = sharedPreferences.getString(name, "");
        return theText;
    }
}
