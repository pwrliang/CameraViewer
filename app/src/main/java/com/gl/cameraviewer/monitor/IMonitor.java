package com.gl.cameraviewer.monitor;

/**
 * Created by Liang on 2016/4/6.
 */
public interface IMonitor {
    void errorPassword();
    void drawBitmap(byte[] bytes);
    void connectFailed();
}
