package com.gl.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Data implements Serializable {
    private static final long serialVersionUID = -7768683932651126083L;
    public static final short TAG_HELLO = 0;//心跳包
    public static final short TAG_START_FRONT = 1;//启动前摄像头
    public static final short TAG_START_BACK = 2;//启动后摄像头
    public static final short TAG_STOP = 3;//停止监控
    public static final short TAG_VIDEO = 4;//视频数据
    public static final short TAG_ERR_PASSWORD = 5;//密码错误
    public static final short TAG_CAM_CONN_TO_SERVER = 6;//被监控端连接到服务器
    public static final short TAG_MON_CONN_TO_SERVER = 7;//监控端连接到服务器
    private String password;
    byte[] data;
    short tag;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getData() {
        try {
            return depress(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setData(byte[] data) {
        try {
            this.data = compress(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getTag() {
        return tag;
    }

    public void setTag(short tag) {
        this.tag = tag;
    }


    public Data(short tag, String password) {
        this.tag = tag;
        this.password = password;
    }

    public Data(short tag) {
        this.tag = tag;
    }

    public Data(byte[] data) {
        this.tag = TAG_VIDEO;
        this.data = data;
    }

    public static Data fromBytes(byte[] bytes) throws IOException,
            ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(
                new ByteArrayInputStream(bytes));
        Data data = (Data) inputStream.readObject();
        return data;
    }

    public static Data fromInputStream(InputStream inputStream) {
        Data data = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            data = (Data) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }

    private byte[] compress(byte[] bytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);
        gzipOutputStream.write(bytes);
        gzipOutputStream.close();
        return baos.toByteArray();
    }

    private byte[] depress(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        byte[] buf = new byte[1024];
        while ((len = gzipInputStream.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        baos.close();
        gzipInputStream.close();
        return baos.toByteArray();
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream arrStream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(arrStream);
        objStream.writeObject(this);
        objStream.close();
        return arrStream.toByteArray();
    }
}