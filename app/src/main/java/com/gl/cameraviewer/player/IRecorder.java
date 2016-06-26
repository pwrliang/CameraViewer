package com.gl.cameraviewer.player;

import android.graphics.Bitmap;

/**
 * Created by Liang on 2016/5/5.
 */
public interface IRecorder {
    void onRender(Bitmap bitmap,int framePosition);
    void onPlayFinish();
}
