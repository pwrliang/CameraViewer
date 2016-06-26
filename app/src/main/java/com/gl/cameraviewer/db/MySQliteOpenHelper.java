package com.gl.cameraviewer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Liang on 2016/4/6.
 */
public class MySQliteOpenHelper extends SQLiteOpenHelper {
    private final String CREATE_TABLE="CREATE TABLE MyCamera(IP TEXT NOT NULL,PORT INTEGER NOT NULL,PASSWORD TEXT NOT NULL,NOTE TEXT,DEVICE_ID TEXT NOT NULL,CAM_NUMBER INTEGER NOT NULL,TYPE INTEGER NOT NULL,PRIMARY KEY (IP,Port))";
    public MySQliteOpenHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
