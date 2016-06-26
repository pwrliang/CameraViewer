package com.gl.cameraviewer.db;

import java.io.Serializable;

/**
 * Created by Liang on 2016/4/5.
 */
public class MyCameraInfo implements Serializable {
    public static final int TYPE_DIRECT = 0;
    public static final int TYPE_TRANSFER = 1;
    private String IP;
    private Integer port;
    private String password;
    private String note;
    private String deviceId;
    private int type;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    private int camNumber;
    private boolean checked;

    public int getCamNumber() {
        return camNumber;
    }

    public void setCamNumber(int camNumber) {
        this.camNumber = camNumber;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public MyCameraInfo() {
    }

    public MyCameraInfo(String IP, Integer port, String password) {
        this.IP = IP;
        this.port = port;
        this.password = password;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNote() {
        return note;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
