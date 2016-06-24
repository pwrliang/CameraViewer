package com.yjm.cameraviewer.player;

import android.graphics.Bitmap;

/**
 * Created by YJM on 2016/5/5.
 */
public interface IRecorder {
    void onRender(Bitmap bitmap,int framePosition);
    void onPlayFinish();
}
