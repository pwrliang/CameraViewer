package com.yjm.cameraviewer.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YJM on 2016/4/6.
 */
public class MyCameraDB {
    private static MyCameraDB myCameraDB;
    private SQLiteDatabase mDatabase;

    public static MyCameraDB getInstance(Context context) {
        if (myCameraDB == null)
            myCameraDB = new MyCameraDB(context);
        return myCameraDB;
    }

    private MyCameraDB(Context context) {
        MySQliteOpenHelper openHelper = new MySQliteOpenHelper(context, "camera.db", 1);
        mDatabase = openHelper.getWritableDatabase();
    }

    public boolean deleteMyCamera(MyCameraInfo myCamera) {
        try {
            mDatabase.execSQL("DELETE FROM MyCamera WHERE IP=? AND PORT=?", new String[]{myCamera.getIP(), myCamera.getPort() + ""});
            return true;
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean saveMyCamera(MyCameraInfo myCameraInfo) {
        try {
            mDatabase.execSQL("INSERT INTO MyCamera(IP,PORT,PASSWORD,NOTE,DEVICE_ID,CAM_NUMBER,TYPE) VALUES (?,?,?,?,?,?,?)",
                    new String[]{myCameraInfo.getIP(), myCameraInfo.getPort() + "", myCameraInfo.getPassword(), myCameraInfo.getNote(),
                            myCameraInfo.getDeviceId(), myCameraInfo.getCamNumber() + "", myCameraInfo.getType() + ""});
            return true;
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateMyCamera(String oldIP, int oldPort, MyCameraInfo myCameraInfo) {
        try {
            mDatabase.execSQL("UPDATE MyCamera SET IP=?,PORT=?,PASSWORD=?,NOTE=?,DEVICE_ID=?,CAM_NUMBER=?,TYPE=? WHERE IP=? AND PORT=?", new String[]{
                    myCameraInfo.getIP(), myCameraInfo.getPort() + "", myCameraInfo.getPassword(), myCameraInfo.getNote(), myCameraInfo.getDeviceId(), myCameraInfo.getCamNumber() + "", myCameraInfo.getType() + "",
                    oldIP, oldPort + ""
            });
            return true;
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
   * 从本地数据库获取摄像头列表
   * 本地数据库为空时返回null
   * */
    public MyCameraInfo getMyCameras(String IP, int port) {
        Cursor cursor =
                mDatabase.rawQuery("SELECT IP,PORT,PASSWORD,NOTE,DEVICE_ID,CAM_NUMBER,TYPE FROM MyCamera WHERE IP=? AND PORT=?", new String[]{IP, port + ""});
        List<MyCameraInfo> myCameras = new ArrayList<>();
        while (cursor.moveToNext()) {
            MyCameraInfo myCamera = new MyCameraInfo();
            myCamera.setIP(cursor.getString(cursor.getColumnIndex("IP")));
            myCamera.setPort(cursor.getInt(cursor.getColumnIndex("PORT")));
            myCamera.setPassword(cursor.getString(cursor.getColumnIndex("PASSWORD")));
            myCamera.setNote(cursor.getString(cursor.getColumnIndex("NOTE")));
            myCamera.setDeviceId(cursor.getString(cursor.getColumnIndex("DEVICE_ID")));
            myCamera.setCamNumber(cursor.getInt(cursor.getColumnIndex("CAM_NUMBER")));
            myCamera.setType(cursor.getInt(cursor.getColumnIndex("TYPE")));
            myCameras.add(myCamera);
        }
        if (myCameras.size() == 0)
            return null;
        return myCameras.get(0);
    }

    /*
    * 从本地数据库获取摄像头列表
    * 本地数据库为空时返回null
    * */
    public List<MyCameraInfo> getMyCameras() {
        Cursor cursor =
                mDatabase.rawQuery("SELECT IP,PORT,PASSWORD,NOTE,DEVICE_ID,CAM_NUMBER,TYPE FROM MyCamera", null);
        List<MyCameraInfo> myCameras = new ArrayList<>();
        while (cursor.moveToNext()) {
            MyCameraInfo myCamera = new MyCameraInfo();
            myCamera.setIP(cursor.getString(cursor.getColumnIndex("IP")));
            myCamera.setPort(cursor.getInt(cursor.getColumnIndex("PORT")));
            myCamera.setPassword(cursor.getString(cursor.getColumnIndex("PASSWORD")));
            myCamera.setNote(cursor.getString(cursor.getColumnIndex("NOTE")));
            myCamera.setDeviceId(cursor.getString(cursor.getColumnIndex("DEVICE_ID")));
            myCamera.setCamNumber(cursor.getInt(cursor.getColumnIndex("CAM_NUMBER")));
            myCamera.setType(cursor.getInt(cursor.getColumnIndex("TYPE")));
            myCameras.add(myCamera);
        }
        if (myCameras.size() == 0)
            return null;
        return myCameras;
    }
}
