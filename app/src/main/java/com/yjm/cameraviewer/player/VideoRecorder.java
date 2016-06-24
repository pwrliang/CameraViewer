package com.yjm.cameraviewer.player;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Liang on 2016/5/5.
 */
public class VideoRecorder {
    private File mFile;
    private DataOutputStream mDataOutputStream;
    private int mQuality;

    public VideoRecorder(File file, int quality) {
        this.mFile = file;
        this.mQuality = quality;
    }

    public void init() throws FileNotFoundException {
        mDataOutputStream = new DataOutputStream(new FileOutputStream(mFile));
    }

    public void writeFrame(Bitmap bitmap, int duration) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, mQuality, baos);
        byte[] bytes = baos.toByteArray();
        mDataOutputStream.writeInt(baos.size());
        mDataOutputStream.writeInt(duration);
        mDataOutputStream.write(bytes);
        baos.flush();
    }

    public void finish() throws IOException {
        mDataOutputStream.flush();
        mDataOutputStream.close();
    }
}
