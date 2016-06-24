package com.yjm.cameraviewer.monitor;

import android.util.Log;

import com.yjm.shared.Data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by YJM on 2016/4/5.
 */
public class Monitor {
    private String TAG = "Monitor";
    protected boolean mRunning;
    protected String mIP;
    protected int mPort;
    protected String mPassword;
    protected short mCamId;

    public Monitor(String IP, int port, String password, short camId) {
        this.mIP = IP;
        this.mPort = port;
        this.mPassword = password;
        this.mCamId = camId;
    }

    public boolean checkConnection() {
        boolean flag = false;
        Socket socket = null;
        try {
            socket = new Socket();
            SocketAddress address = new InetSocketAddress(mIP, mPort);
            socket.connect(address, 2000);
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public void startMonitor(IMonitor iMonitor) {
        Data data = new Data(mCamId, mPassword);//请求启动摄像头
        mRunning = true;
        try {
            byte[] bytes = data.toBytes();
            Socket socket = new Socket(mIP, mPort);
            OutputStream mOutputStream = socket.getOutputStream();
            mOutputStream.write(bytes);
            mOutputStream.flush();
            receiveCommand(socket.getInputStream(), iMonitor);
            Log.i(TAG, "接收线程已停止");
        } catch (IOException e) {
            e.printStackTrace();
            stopMonitor();
            iMonitor.connectFailed();
        }
    }

    public void stopMonitor() {
        Data data = new Data(Data.TAG_STOP);
        mRunning = false;
        try {
            byte[] bytes = data.toBytes();
            Socket socket = new Socket(mIP, mPort);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void receiveCommand(InputStream inputStream, IMonitor iMonitor) {
        while (mRunning) {
            try {
                if (inputStream.available() == 0) {
                    Thread.sleep(10);
                    continue;
                }
                Data data = Data.fromInputStream(inputStream);              //从输入流读取数据
                if (data != null) {
                    if (data.getTag() == Data.TAG_ERR_PASSWORD) {            //收到密码错误报文
                        iMonitor.errorPassword();
                    } else if (data.getTag() == Data.TAG_VIDEO) {            //收到图像报文
                        byte[] bytes = data.getData();
                        if (bytes != null)
                            iMonitor.drawBitmap(bytes);
                    }
                    Log.i(TAG, "receiveCommand");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
