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
 * Created by Liang on 2016/5/24.
 */
public class MonitorEx extends Monitor {
    private String TAG = "MonitorEx";
    private Socket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    //    private String mIP;
//    private int mPort;
//    private String mPassword;
//    private int mCamId;
    private String mDeviceId;

    public MonitorEx(String IP, int port, String password, short camId, String deviceId) {
        super(IP, port, password, camId);
        this.mDeviceId = deviceId;
    }

    public void connToServer() throws IOException {
        mSocket = new Socket();
        SocketAddress address = new InetSocketAddress(mIP, mPort);
        mSocket.connect(address, 2000);
        mInputStream = mSocket.getInputStream();
        mOutputStream = mSocket.getOutputStream();
        Data data = new Data(Data.TAG_MON_CONN_TO_SERVER);
        data.setData(mDeviceId.getBytes("utf-8"));
        mOutputStream.write(data.toBytes());
        Log.i(TAG,"连接服务器成功");
    }

    public void startMonitor(IMonitor iMonitor) {
        Data data = new Data(mCamId, mPassword);//请求启动摄像头
        mRunning = true;
        try {
            byte[] bytes = data.toBytes();
            if (mOutputStream != null) {
                mOutputStream.write(bytes);
                mOutputStream.flush();
            }
            receiveCommand(mInputStream, iMonitor);
            Log.i(TAG, "接收线程已停止");
        } catch (IOException e) {
            e.printStackTrace();
            iMonitor.connectFailed();
        }
    }

    public void stopMonitor() {
        Log.i(TAG, "stopMonitor");
        Data data = new Data(Data.TAG_STOP);
        mRunning = false;
        try {
            byte[] bytes = data.toBytes();
            if (mOutputStream != null) {
                mOutputStream.write(bytes);
                mOutputStream.flush();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void receiveCommand(InputStream inputStream, IMonitor iMonitor) {
//        while (mRunning) {
//            try {
//                if (inputStream.available() == 0) {
//                    Thread.sleep(10);
//                    continue;
//                }
//                Log.i(TAG, "Ava:" + inputStream.available());
//                Data data = Data.fromInputStream(inputStream);              //从输入流读取数据
//                if (data != null) {
//                    if (data.getTag() == Data.TAG_ERR_PASSWORD) {            //收到密码错误报文
//                        iMonitor.errorPassword();
//                    } else if (data.getTag() == Data.TAG_VIDEO) {            //收到图像报文
//                        byte[] bytes = data.getData();
//                        if (bytes != null)
//                            iMonitor.drawBitmap(bytes);
//                    }
//                }
//                Log.i(TAG, "receiveCommand");
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        Log.i(TAG, "接收线程停止");
//    }
}
