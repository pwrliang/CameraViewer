package com.yjm.cameraviewer.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.yjm.cameraviewer.R;
import com.yjm.cameraviewer.db.MyCameraInfo;
import com.yjm.cameraviewer.db.Preference;
import com.yjm.cameraviewer.monitor.IMonitor;
import com.yjm.cameraviewer.monitor.Monitor;
import com.yjm.cameraviewer.monitor.MonitorEx;
import com.yjm.cameraviewer.player.VideoRecorder;
import com.yjm.shared.Data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MonitorActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "MonitorActivity";
    private SurfaceView mSVMonitor;
    private ProgressBar mPBLoading;
    private SurfaceHolder mSurfaceHolder;
    private MyCameraInfo myCameraInfo;
    private Monitor mMonitor;
    private MonitorEx mMonitorEx;
    private VideoRecorder mVideoRecorder;
    private boolean mRecording;
    private float mScale;                   //缩放比例
    private boolean mMaxScale;              //是否缩放到最大

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);           //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常量
        setContentView(R.layout.activity_viewer);
        mSVMonitor = (SurfaceView) findViewById(R.id.av_sv_monitor);
        mPBLoading = (ProgressBar) findViewById(R.id.av_pb_loading);
        myCameraInfo = (MyCameraInfo) getIntent().getSerializableExtra("MyCamera");
//        Log.i(TAG, myCameraInfo.getDeviceId());
        mSurfaceHolder = mSVMonitor.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (myCameraInfo.getCamNumber() > 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MonitorActivity.this);
                    builder.setTitle("摄像头选择");
                    builder.setMessage("远程设备有超过一个摄像头，请选择要查看的摄像头");
                    builder.setPositiveButton("前置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (myCameraInfo.getType() == MyCameraInfo.TYPE_DIRECT) {
                                startMonitor(Data.TAG_START_FRONT);             //创建新线程进行监控
                            } else if (myCameraInfo.getType() == MyCameraInfo.TYPE_TRANSFER) {
                                startMonitorEx(Data.TAG_START_FRONT);
                            }
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("后置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (myCameraInfo.getType() == MyCameraInfo.TYPE_DIRECT) {
                                startMonitor(Data.TAG_START_BACK);             //创建新线程进行监控
                            } else if (myCameraInfo.getType() == MyCameraInfo.TYPE_TRANSFER) {
                                startMonitorEx(Data.TAG_START_BACK);
                            }
                            dialog.dismiss();
                        }
                    });
                    builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    builder.setCancelable(false);
                    builder.create().show();
                } else {
                    //单个摄像头，默认后置
                    if (myCameraInfo.getType() == MyCameraInfo.TYPE_DIRECT) {
                        startMonitor(Data.TAG_START_BACK);             //创建新线程进行监控
                    } else if (myCameraInfo.getType() == MyCameraInfo.TYPE_TRANSFER) {
                        startMonitorEx(Data.TAG_START_BACK);
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        mSVMonitor.setOnClickListener(this);
        mSVMonitor.setOnLongClickListener(this);
        mScale = Preference.getInstance(MonitorActivity.this).getScale();
    }

    @Override
    public boolean onLongClick(View v) {
        if (!mRecording) {
            File OutputFile = new File(Environment.getExternalStorageDirectory().getPath());
            File videoDir = new File(OutputFile.getAbsolutePath() + "/DCIM/Monitor");
            if (!videoDir.exists()) {
                videoDir.mkdir();
            }
            String fullPath = videoDir.getAbsolutePath() + "/" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mon";
            mVideoRecorder = new VideoRecorder(new File(fullPath), 100);
            try {
                mVideoRecorder.init();
                Toast.makeText(MonitorActivity.this, "录像开始", Toast.LENGTH_SHORT).show();
                mRecording = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mVideoRecorder.finish();
                mRecording = false;
                Toast.makeText(MonitorActivity.this, "录像停止", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public void onClick(View v) {
        mScale += 0.3;
        //已经放到最大
        if (mMaxScale) {
            mScale = 1.5f;
            mMaxScale = false;
        }
        Preference.getInstance(MonitorActivity.this).setScale(mScale);
    }

    @Override
    protected void onStop() {
        super.onStop();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mMonitor != null)
                    mMonitor.stopMonitor();
                if (mMonitorEx != null) {
                    mMonitorEx.stopMonitor();
                }
            }
        }).start();
    }

    IMonitor iMonitor = new IMonitor() {
        private long lastFrame;
        long count = 0;
        long last = System.currentTimeMillis();
        float size = 0;
        float totalMB = 0;

        @Override
        public void errorPassword() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MonitorActivity.this, "密码错误", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }

        @Override
        public void connectFailed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MonitorActivity.this, "连接失败", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }

        @Override
        public void drawBitmap(byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPBLoading.setVisibility(View.INVISIBLE);            //使原型进度条不可见
                }
            });
            float interval = (System.currentTimeMillis() - last) / 1000.0f;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (lastFrame > 0) {
//                Log.i(TAG, (System.currentTimeMillis() - lastFrame) + "");
                if (mRecording) {
                    try {
                        mVideoRecorder.writeFrame(bitmap, (int) (System.currentTimeMillis() - lastFrame));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            lastFrame = System.currentTimeMillis();
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
            Canvas canvas = mSurfaceHolder.lockCanvas();
            if (canvas == null)             //当Activity被销毁时，canvas为空，这时不再绘制
                return;
            //图片放大后超过控件大小，就全屏
            if (mScale * bitmap.getWidth() > mSVMonitor.getWidth()) {
                mScale = mSVMonitor.getWidth() / (float) bitmap.getWidth();
                mMaxScale = true;
            }
            int newWidth = (int) (bitmap.getWidth() * mScale);
            int newHeight = (int) (bitmap.getHeight() * mScale);
            Rect src = new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() - 1);           //将位图绘制成SurfaceView的大小
            Rect dest = new Rect(0, 0, newWidth - 1, newHeight - 1);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空画布
            canvas.drawBitmap(bitmap, src, dest, null);             //绘制图像
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            textPaint.setTextSize(25.0f);               //字体大小
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);               //粗体
            textPaint.setColor(Color.YELLOW);                   //黄色
            //添加水印
            canvas.drawText("FPS:" + fps + " ↓:" + numberFormat.format(netSpeed) + " KB/s", 0, mSVMonitor.getHeight() * 0.9f, textPaint);
            canvas.drawText("Total:" + numberFormat.format(totalMB) + " MB", 0, mSVMonitor.getHeight() * 0.9f + 30, textPaint);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
            bitmap.recycle();
        }
    };

    private void startMonitor(final short camId) {
        Log.i(TAG, "startMonitor");
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMonitor = new Monitor(myCameraInfo.getIP(), myCameraInfo.getPort(), myCameraInfo.getPassword(), camId);                //创建实例
                if (!mMonitor.checkConnection()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MonitorActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } else {
                    mMonitor.startMonitor(iMonitor);
                }
            }
        }).start();
    }

    private void startMonitorEx(final short camId) {
        Log.i(TAG, "startMonitorEx");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //45.32.89.187
                //192.168.43.71
                mMonitorEx = new MonitorEx(Preference.SERVER_IP, Preference.SERVER_PORT, myCameraInfo.getPassword(), camId, myCameraInfo.getDeviceId());                //创建实例
                try {
                    mMonitorEx.connToServer();
                    mMonitorEx.startMonitor(iMonitor);
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MonitorActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }
        }).start();
    }
}
