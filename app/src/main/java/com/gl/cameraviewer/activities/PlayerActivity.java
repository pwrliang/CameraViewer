package com.gl.cameraviewer.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gl.cameraviewer.R;
import com.gl.cameraviewer.player.IRecorder;
import com.gl.cameraviewer.player.RecorderPlayer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerActivity extends AppCompatActivity {
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private String mPath;
    private SeekBar mSBProgress;
    private TextView mTVTime;
    private RecorderPlayer mRecorderPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);//要求全屏
        mSurfaceView = (SurfaceView) findViewById(R.id.ap_surfaceview);
        mSBProgress = (SeekBar) findViewById(R.id.ap_sb_progress);
        mTVTime = (TextView) findViewById(R.id.ap_tv_time);
        mPath = getIntent().getStringExtra("path");
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playFile(mPath, 0);
                    }
                }).start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mRecorderPlayer.stop();
            }
        });
        mSBProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                Log.i("TEST", "" + mSBProgress.getProgress());
                mRecorderPlayer.stop();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        playFile(mPath, mSBProgress.getProgress());
                    }
                }).start();
            }
        });
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击屏幕暂停/继续播放
                Log.i("EEE","CLICKED");
                if (mRecorderPlayer != null) {
                    if (mRecorderPlayer.isPausing()) {
                        mRecorderPlayer.resume();
                    } else {
                        mRecorderPlayer.pause();
                    }
                }
            }
        });
    }

    private void playFile(String path, int position) {
        File file = new File(path);
        try {
            mRecorderPlayer = new RecorderPlayer(file);
            //获取视频长度信息
            final int length = mRecorderPlayer.getVideoLength();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSBProgress.setMax(length);
                }
            });
            mRecorderPlayer.play(position, new IRecorder() {
                @Override
                public void onRender(Bitmap bitmap, final int framePosition) {
                    Canvas canvas = mSurfaceHolder.lockCanvas();
                    if (canvas == null)
                        return;
                    Rect src = new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() - 1);           //将位图绘制成SurfaceView的大小
                    Rect dest = new Rect(0, 0, mSurfaceView.getWidth(), mSurfaceView.getHeight());
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清空画布
                    canvas.drawBitmap(bitmap, src, dest, null);             //绘制图像
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSBProgress.setProgress(framePosition);
                            int currMilliSecond = 0;
                            for (int i = 0; i < framePosition; i++) {
                                currMilliSecond += mRecorderPlayer.getFrameLength(i);
                            }
                            Log.i("S", "" + currMilliSecond);
                            mTVTime.setText(new SimpleDateFormat("mm:ss").format(new Date(currMilliSecond)));
                        }
                    });
                }

                @Override
                public void onPlayFinish() {
                    mSBProgress.setProgress(mSBProgress.getMax());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
