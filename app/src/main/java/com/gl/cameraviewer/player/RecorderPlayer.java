package com.gl.cameraviewer.player;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Liang on 2016/5/5.
 */
public class RecorderPlayer {
    private File mFile;
    private DataInputStream mDataInputStream;
    private int mFramePosition;
    private int[] mFrameLength;//每一帧的时长
    private boolean mPausing;
    private boolean mPlaying;

    public RecorderPlayer(File file) {
        this.mFile = file;
    }

    public int getVideoLength() throws IOException {
        int index = 0;
        mDataInputStream = new DataInputStream(new FileInputStream(mFile));
        mFrameLength = new int[1000];
        while (mDataInputStream.available() > 0) {
            int size = mDataInputStream.readInt();//帧长度
            mFrameLength[index++] = mDataInputStream.readInt();//图像停留时间
            mDataInputStream.skipBytes(size);//跳过图像数据
            //空间不够，要扩充
            if (index == mFrameLength.length) {
                int[] tmp = new int[mFrameLength.length + 1000];
                for (int j = 0; j < index; j++) {
                    tmp[j] = mFrameLength[j];
                }
                mFrameLength = tmp;
            }
        }
        mDataInputStream.close();
        return index;
    }

    public int getFrameLength(int framePosition) {
        return mFrameLength[framePosition];
    }

    public void pause() {
        mPausing = true;
    }

    public void resume() {
        mPausing = false;
    }

    public void stop() {
        mPlaying = false;
    }

    public boolean isPausing() {
        return mPausing;
    }

    public void play(int framePosition, IRecorder recorder) throws IOException, InterruptedException {
        mPlaying = true;
        boolean isBreak = false;
        mDataInputStream = new DataInputStream(new FileInputStream(mFile));
        for (int i = 0; i < framePosition; i++) {
            int size = mDataInputStream.readInt();
            mDataInputStream.readInt();
            mDataInputStream.skipBytes(size);
        }
        mFramePosition = framePosition;
        while (mDataInputStream.available() > 0) {
            int size = mDataInputStream.readInt();
            int duration = mDataInputStream.readInt();
            byte[] bytes = new byte[size];
            mDataInputStream.readFully(bytes);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, size);
            recorder.onRender(bitmap, mFramePosition);
            Thread.sleep(duration);
            mFramePosition++;
            if (!mPlaying) {
                isBreak = true;
                break;
            }
            //暂停中
            while (mPausing) {
                Thread.sleep(100);
            }
        }
        if (!isBreak)
            recorder.onPlayFinish();
        mPlaying = false;
        mDataInputStream.close();
    }

    public void play(IRecorder recorder) throws IOException, InterruptedException {
        mPlaying = true;
        boolean isBreak = false;
        mDataInputStream = new DataInputStream(new FileInputStream(mFile));
        mFramePosition = 0;
        while (mDataInputStream.available() > 0) {
            int size = mDataInputStream.readInt();
            int duration = mDataInputStream.readInt();
            byte[] bytes = new byte[size];
            mDataInputStream.readFully(bytes);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, size);
            recorder.onRender(bitmap, mFramePosition);
            Thread.sleep(duration);
            mFramePosition++;
            if (!mPlaying) {
                isBreak = true;
                break;
            }
        }
        if (!isBreak)
            recorder.onPlayFinish();
        mPlaying = false;
        mDataInputStream.close();
    }
}
