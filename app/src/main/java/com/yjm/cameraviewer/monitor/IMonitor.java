package com.yjm.cameraviewer.monitor;

/**
 * Created by YJM on 2016/4/6.
 */
public interface IMonitor {
    void errorPassword();
    void drawBitmap(byte[] bytes);
    void connectFailed();
}
