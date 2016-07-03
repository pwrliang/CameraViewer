package com.gl.cameraviewer.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Liang on 2016/4/7.
 */
public class Preference {
    public static final String SERVER_IP = "43.224.34.90";
    public static final int SERVER_PORT = 3295;

    private static Preference preference;
    private SharedPreferences sharedPreferences;

    public static Preference getInstance(Context context) {
        if (preference == null) {
            preference = new Preference(context);
        }
        return preference;
    }

    private Preference(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setScale(float scale) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("scale", scale);
        editor.apply();
    }

    public float getScale() {
        return sharedPreferences.getFloat("scale", 1.5f);
    }
}
