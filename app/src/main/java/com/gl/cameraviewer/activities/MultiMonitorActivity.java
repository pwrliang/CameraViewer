package com.gl.cameraviewer.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.gl.cameraviewer.R;
import com.gl.cameraviewer.db.MyCameraInfo;
import com.gl.cameraviewer.db.Preference;
import com.gl.cameraviewer.monitor.IMonitor;
import com.gl.cameraviewer.monitor.Monitor;
import com.gl.cameraviewer.monitor.MonitorEx;
import com.gl.shared.Data;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liang on 2016/4/7.
 */
public class MultiMonitorActivity extends Activity {
    private SurfaceView mSurfaceView1;
    private SurfaceView mSurfaceView2;
    private SurfaceView mSurfaceView3;
    private SurfaceView mSurfaceView4;
    private List<MyCameraInfo> mMyCameraList;              //远程摄像头信息
    private List<Monitor> mMonitorList = new ArrayList<>();              //与摄像头连接信息
    private String TAG = "CameraViewer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);           //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常量
        setContentView(R.layout.activity_camera_list);
        mSurfaceView1 = (SurfaceView) findViewById(R.id.acl_sv_cam1);
        mSurfaceView2 = (SurfaceView) findViewById(R.id.acl_sv_cam2);
        mSurfaceView3 = (SurfaceView) findViewById(R.id.acl_sv_cam3);
        mSurfaceView4 = (SurfaceView) findViewById(R.id.acl_sv_cam4);

//        cameraList.add(new MyCamera("pwrliang.imwork.net", 1234, "no password"));
//        cameraList.add(new MyCamera("pwrliang.imwork.net", 2345, "no password"));

        mMyCameraList = (List<MyCameraInfo>) getIntent().getSerializableExtra("cameraList");
        if (mMyCameraList.size() == 0) {
            Toast.makeText(this, "未选择远程摄像头!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mSurfaceView1.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mMyCameraList.size() >= 1) {
                    if (mMyCameraList.get(0).getType() == MyCameraInfo.TYPE_DIRECT) {
                        new MonitorThread(mMyCameraList.get(0), mSurfaceView1).start();
                    } else if (mMyCameraList.get(0).getType() == MyCameraInfo.TYPE_TRANSFER) {
                        new MonitorExThread(mMyCameraList.get(0), mSurfaceView1).start();
                    }
                } else {
                    drawText(mSurfaceView1, "Camera1无信号");
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        mSurfaceView2.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mMyCameraList.size() >= 2) {
                    if (mMyCameraList.get(1).getType() == MyCameraInfo.TYPE_DIRECT) {
                        new MonitorThread(mMyCameraList.get(1), mSurfaceView2).start();
                    } else if (mMyCameraList.get(1).getType() == MyCameraInfo.TYPE_TRANSFER) {
                        new MonitorExThread(mMyCameraList.get(1), mSurfaceView2).start();
                    }
                } else {
                    drawText(mSurfaceView2, "Camera2无信号");
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        mSurfaceView3.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mMyCameraList.size() >= 3) {
                    if (mMyCameraList.get(2).getType() == MyCameraInfo.TYPE_DIRECT) {
                        new MonitorThread(mMyCameraList.get(2), mSurfaceView3).start();
                    } else if (mMyCameraList.get(2).getType() == MyCameraInfo.TYPE_TRANSFER) {
                        new MonitorExThread(mMyCameraList.get(2), mSurfaceView3).start();
                    }
                } else {
                    drawText(mSurfaceView3, "Camera3无信号");
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        mSurfaceView4.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mMyCameraList.size() >= 4) {
                    if (mMyCameraList.get(3).getType() == MyCameraInfo.TYPE_DIRECT) {
                        new MonitorThread(mMyCameraList.get(3), mSurfaceView4).start();
                    } else if (mMyCameraList.get(3).getType() == MyCameraInfo.TYPE_TRANSFER) {
                        new MonitorExThread(mMyCameraList.get(3), mSurfaceView4).start();
                    }
                } else {
                    drawText(mSurfaceView4, "Camera4无信号");

                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (final Monitor monitor : mMonitorList) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    monitor.stopMonitor();
                }
            }).start();
        }
    }

    /*
    * 在SurfaceView中央绘制文字
    * */
    private void drawText(SurfaceView surfaceView, String text) {
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null)
            return;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空画布
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        textPaint.setTextSize(25.0f);               //字体大小
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);               //粗体
        textPaint.setColor(Color.YELLOW);                   //黄色
        //添加水印
        canvas.drawText(text, surfaceView.getWidth() / 2 - 90, surfaceView.getHeight() / 2.0f, textPaint);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private class MonitorThread extends Thread {
        long count = 0;
        long last = System.currentTimeMillis();
        float size = 0;
        float totalMB = 0;
        MyCameraInfo myCamera;
        SurfaceView surfaceView;

        private MonitorThread(MyCameraInfo myCamera, SurfaceView surfaceView) {
            this.myCamera = myCamera;
            this.surfaceView = surfaceView;
        }


        @Override
        public void run() {
            Monitor monitor = new Monitor(myCamera.getIP(), myCamera.getPort(), myCamera.getPassword(), Data.TAG_START_BACK);                //创建实例
            mMonitorList.add(monitor);
            if (!monitor.checkConnection()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawText(surfaceView, "连接失败");
                    }
                });
            } else {
                monitor.startMonitor(new IMonitor() {
                    @Override
                    public void errorPassword() {
                        drawText(surfaceView, "密码错误");
                    }

                    @Override
                    public void connectFailed() {
                        drawText(surfaceView, "连接失败");
                    }

                    @Override
                    public void drawBitmap(byte[] bytes) {
                        float interval = (System.currentTimeMillis() - last) / 1000.0f;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Log.i(TAG, "getByteCount:" + bytes.length);
                        count++;                //统计1s内多少帧
                        size += bytes.length / 1024.0;              //统计1s内多少KB
                        totalMB += bytes.length / 1024.0 / 1024.0;              //统计流量总额
                        int fps = (int) (1 / interval * count);
                        float netSpeed = 1 / interval * size;
                        if (System.currentTimeMillis() - last > 1000) {         //超过1s重新统计
                            last = System.currentTimeMillis();
                            count = 0;
                            size = 0;
                        }
                        NumberFormat numberFormat = NumberFormat.getNumberInstance();
                        numberFormat.setMaximumFractionDigits(2);
                        numberFormat.setMinimumFractionDigits(2);
                        SurfaceHolder surfaceHolder = surfaceView.getHolder();
                        Canvas canvas = surfaceHolder.lockCanvas();
                        if (canvas == null)             //当Activity被销毁时，canvas为空，这时不再绘制
                            return;
                        //图片放大后超过控件大小，就全屏
                        int newWidth = surfaceView.getWidth();
                        int newHeight = surfaceView.getHeight();
                        Rect src = new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() - 1);           //将位图绘制成SurfaceView的大小
                        Rect dest = new Rect(0, 0, newWidth - 1, newHeight - 1);
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空画布
                        canvas.drawBitmap(bitmap, src, dest, null);             //绘制图像
                        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
                        textPaint.setTextSize(25.0f);               //字体大小
                        textPaint.setTypeface(Typeface.DEFAULT_BOLD);               //粗体
                        textPaint.setColor(Color.YELLOW);                   //黄色
                        //添加水印
                        canvas.drawText("FPS:" + fps + " ↓:" + numberFormat.format(netSpeed) + " KB/s", 0, surfaceView.getHeight() * 0.9f, textPaint);
                        canvas.drawText("Total:" + numberFormat.format(totalMB) + " MB", 0, surfaceView.getHeight() * 0.9f + 30, textPaint);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        bitmap.recycle();
                    }
                });
            }
        }
    }

    private class MonitorExThread extends Thread {
        long count = 0;
        long last = System.currentTimeMillis();
        float size = 0;
        float totalMB = 0;
        MyCameraInfo myCamera;
        SurfaceView surfaceView;

        private MonitorExThread(MyCameraInfo myCamera, SurfaceView surfaceView) {
            this.myCamera = myCamera;
            this.surfaceView = surfaceView;
        }


        @Override
        public void run() {
            MonitorEx monitorEx = new MonitorEx(Preference.SERVER_IP, Preference.SERVER_PORT, myCamera.getPassword(), Data.TAG_START_BACK, myCamera.getDeviceId());                //创建实例
            mMonitorList.add(monitorEx);
            try {
                monitorEx.connToServer();
                monitorEx.startMonitor(new IMonitor() {
                    @Override
                    public void errorPassword() {
                        drawText(surfaceView, "密码错误");
                    }

                    @Override
                    public void connectFailed() {
                        drawText(surfaceView, "连接失败");
                    }

                    @Override
                    public void drawBitmap(byte[] bytes) {
                        float interval = (System.currentTimeMillis() - last) / 1000.0f;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Log.i(TAG, "getByteCount:" + bytes.length);
                        count++;                //统计1s内多少帧
                        size += bytes.length / 1024.0;              //统计1s内多少KB
                        totalMB += bytes.length / 1024.0 / 1024.0;              //统计流量总额
                        int fps = (int) (1 / interval * count);
                        float netSpeed = 1 / interval * size;
                        if (System.currentTimeMillis() - last > 1000) {         //超过1s重新统计
                            last = System.currentTimeMillis();
                            count = 0;
                            size = 0;
                        }
                        NumberFormat numberFormat = NumberFormat.getNumberInstance();
                        numberFormat.setMaximumFractionDigits(2);
                        numberFormat.setMinimumFractionDigits(2);
                        SurfaceHolder surfaceHolder = surfaceView.getHolder();
                        Canvas canvas = surfaceHolder.lockCanvas();
                        if (canvas == null)             //当Activity被销毁时，canvas为空，这时不再绘制
                            return;
                        //图片放大后超过控件大小，就全屏
                        int newWidth = surfaceView.getWidth();
                        int newHeight = surfaceView.getHeight();
                        Rect src = new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() - 1);           //将位图绘制成SurfaceView的大小
                        Rect dest = new Rect(0, 0, newWidth - 1, newHeight - 1);
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空画布
                        canvas.drawBitmap(bitmap, src, dest, null);             //绘制图像
                        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
                        textPaint.setTextSize(25.0f);               //字体大小
                        textPaint.setTypeface(Typeface.DEFAULT_BOLD);               //粗体
                        textPaint.setColor(Color.YELLOW);                   //黄色
                        //添加水印
                        canvas.drawText("FPS:" + fps + " ↓:" + numberFormat.format(netSpeed) + " KB/s", 0, surfaceView.getHeight() * 0.9f, textPaint);
                        canvas.drawText("Total:" + numberFormat.format(totalMB) + " MB", 0, surfaceView.getHeight() * 0.9f + 30, textPaint);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawText(surfaceView, "连接失败");
                    }
                });
            }

        }
    }
}
