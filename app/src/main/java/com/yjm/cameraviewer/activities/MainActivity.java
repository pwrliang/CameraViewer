package com.yjm.cameraviewer.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.yjm.cameraviewer.R;
import com.yjm.cameraviewer.db.MyCameraDB;
import com.yjm.cameraviewer.db.MyCameraInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by YJM on 2016/4/5.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private MyAdapter myAdapter;
    private ListView mLVCamera;
    private List<MyCameraInfo> mCameraList = new ArrayList<>();

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.abm_toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mLVCamera = (ListView) findViewById(R.id.am_lv_camlist);
        registerForContextMenu(mLVCamera);
        MyCameraDB myCameraDB = MyCameraDB.getInstance(this);
        myAdapter = new MyAdapter(this, R.layout.item_camera, mCameraList);
        mLVCamera.setAdapter(myAdapter);
        mLVCamera.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                MyCameraInfo myCameraInfo =  mCameraList.get(position);
                Intent intent = new Intent(MainActivity.this, MonitorActivity.class);
                intent.putExtra("MyCamera", myCameraInfo);
                startActivity(intent);
            }
        });
        if (myCameraDB.getMyCameras() != null) {
            myAdapter.clear();
            myAdapter.addAll(myCameraDB.getMyCameras());
            myAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "未发现远程摄像头，请点击菜单键添加", Toast.LENGTH_SHORT).show();
        }
    }


    private class MyAdapter extends ArrayAdapter<MyCameraInfo> {
        Context mContext;
        int resource;
        List<MyCameraInfo> cameras;

        MyAdapter(Context context, int resource, List<MyCameraInfo> cameras) {
            super(context, resource, cameras);
            this.mContext = context;
            this.resource = resource;
            this.cameras = cameras;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            View view;
            final MyCameraInfo myCamera = cameras.get(position);
            if (convertView == null) {//缓存为空
                view = LayoutInflater.from(MainActivity.this).inflate(resource, null);
                viewHolder = new ViewHolder();
                viewHolder.checkBox = (CheckBox) view.findViewById(R.id.ic_cb_camera);
                viewHolder.tvInfo = (TextView) view.findViewById(R.id.ic_tv_camera);
                viewHolder.aSwitch = (Switch) view.findViewById(R.id.ic_sw_type);
                viewHolder.tvNote = (TextView) view.findViewById(R.id.ic_tv_note);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.checkBox.setChecked(myCamera.isChecked());
            viewHolder.tvInfo.setText(myCamera.getIP() + ":" + myCamera.getPort());
            if (!TextUtils.isEmpty(myCamera.getNote())) {
                viewHolder.tvNote.setText(myCamera.getNote());
                viewHolder.tvNote.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tvNote.setVisibility(View.GONE);
            }
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    myCamera.setChecked(isChecked);
                }
            });
            if (myCamera.getType() == MyCameraInfo.TYPE_DIRECT) {
                viewHolder.aSwitch.setChecked(true);
            } else if (myCamera.getType() == MyCameraInfo.TYPE_TRANSFER) {
                viewHolder.aSwitch.setChecked(false);
            }
            viewHolder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MyCameraInfo myCameraInfo = cameras.get(position);
                    if (isChecked) {
                        myCameraInfo.setType(MyCameraInfo.TYPE_DIRECT);
                    } else {
                        myCameraInfo.setType(MyCameraInfo.TYPE_TRANSFER);
                    }
                    MyCameraDB.getInstance(mContext).updateMyCamera(myCameraInfo.getIP(), myCameraInfo.getPort(), myCameraInfo);
                }
            });
            return view;
        }

        class ViewHolder {
            CheckBox checkBox;
            TextView tvInfo;
            Switch aSwitch;
            TextView tvNote;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.am_lv_camlist) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_update_camera: {
                final MyCameraInfo myCameraInfo = mCameraList.get(info.position);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View view = LayoutInflater.from(this).inflate(R.layout.view_add_cam, null);
                builder.setView(view);
                final EditText edtIP = (EditText) view.findViewById(R.id.vac_edt_ip);
                final EditText edtPort = (EditText) view.findViewById(R.id.vac_edt_port);
                final EditText edtPassword = (EditText) view.findViewById(R.id.vac_edt_password);
                final EditText edtNote = (EditText) view.findViewById(R.id.vac_edt_note);
                final EditText edtDeviceId = (EditText) view.findViewById(R.id.vac_edt_android_id);
                final RadioButton rbCamOne = (RadioButton) view.findViewById(R.id.vac_rb_one);
                final RadioButton rbCamTwo = (RadioButton) view.findViewById(R.id.vac_rb_two);
                edtIP.setText(myCameraInfo.getIP());
                edtPort.setText(myCameraInfo.getPort() + "");
                edtPassword.setText(myCameraInfo.getPassword());
                edtNote.setText(myCameraInfo.getNote());
                edtDeviceId.setText(myCameraInfo.getDeviceId());
                if (myCameraInfo.getCamNumber() == 1) {
                    rbCamOne.setChecked(true);
                    rbCamTwo.setChecked(false);
                } else if (myCameraInfo.getCamNumber() == 2) {
                    rbCamOne.setChecked(false);
                    rbCamTwo.setChecked(true);
                }
                builder.setTitle("添加摄像头");
                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(edtPort.getText())) {
                            Toast.makeText(MainActivity.this, "端口号为空", Toast.LENGTH_SHORT).show();
                        } else if (TextUtils.isEmpty(edtIP.getText())) {
                            Toast.makeText(MainActivity.this, "IP地址为空", Toast.LENGTH_SHORT).show();
                        } else if (TextUtils.isEmpty(edtPassword.getText())) {
                            Toast.makeText(MainActivity.this, "密码为空", Toast.LENGTH_SHORT).show();
                        } else if (TextUtils.isEmpty(edtDeviceId.getText())) {
                            Toast.makeText(MainActivity.this, "设备ID为空", Toast.LENGTH_SHORT).show();
                        } else {
                            MyCameraInfo newMyCamera = new MyCameraInfo();
                            newMyCamera.setIP(edtIP.getText().toString());
                            newMyCamera.setPort(Integer.valueOf(edtPort.getText().toString()));
                            newMyCamera.setPassword(edtPassword.getText().toString());
                            newMyCamera.setDeviceId(edtDeviceId.getText().toString());
                            newMyCamera.setNote(edtNote.getText().toString());
                            newMyCamera.setType(myCameraInfo.getType());
                            if (rbCamOne.isChecked()) {
                                newMyCamera.setCamNumber(1);
                            } else if (rbCamTwo.isChecked()) {
                                newMyCamera.setCamNumber(2);
                            }
                            //保存到本地数据库
                            MyCameraDB myCameraDB = MyCameraDB.getInstance(MainActivity.this);
                            if (!myCameraDB.updateMyCamera(myCameraInfo.getIP(), myCameraInfo.getPort(), newMyCamera)) {
                                Toast.makeText(MainActivity.this, "修改远程摄像头失败，目标已存在", Toast.LENGTH_SHORT).show();
                            } else {
                                myAdapter.clear();
                                myAdapter.addAll(myCameraDB.getMyCameras());
                                myAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
            case R.id.action_del_camera: {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("是否删除远程摄像头？");
                builder.setTitle("确认");

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyCameraDB.getInstance(MainActivity.this).deleteMyCamera(mCameraList.get(info.position));//从本地数据库中删除
                        mCameraList.remove(info.position);
                        myAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create().show();
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_add_camera) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.view_add_cam, null);
            builder.setView(view);
            final EditText edtIP = (EditText) view.findViewById(R.id.vac_edt_ip);
            final EditText edtPort = (EditText) view.findViewById(R.id.vac_edt_port);
            final EditText edtPassword = (EditText) view.findViewById(R.id.vac_edt_password);
            final EditText edtNote = (EditText) view.findViewById(R.id.vac_edt_note);
            final EditText edtDeviceId = (EditText) view.findViewById(R.id.vac_edt_android_id);
            final RadioButton rbCamOne = (RadioButton) view.findViewById(R.id.vac_rb_one);
            final RadioButton rbCamTwo = (RadioButton) view.findViewById(R.id.vac_rb_two);
            rbCamTwo.setChecked(true);
            edtPort.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
            builder.setTitle("添加摄像头");
            builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (TextUtils.isEmpty(edtPort.getText())) {
                        Toast.makeText(MainActivity.this, "端口号为空", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(edtIP.getText())) {
                        Toast.makeText(MainActivity.this, "IP地址为空", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(edtPassword.getText())) {
                        Toast.makeText(MainActivity.this, "密码为空", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(edtDeviceId.getText())) {
                        Toast.makeText(MainActivity.this, "设备ID为空", Toast.LENGTH_SHORT).show();
                    } else {
                        MyCameraInfo myCamera = new MyCameraInfo();
                        myCamera.setIP(edtIP.getText().toString());
                        myCamera.setPort(Integer.valueOf(edtPort.getText().toString()));
                        myCamera.setPassword(edtPassword.getText().toString());
                        myCamera.setDeviceId(edtDeviceId.getText().toString());
                        myCamera.setNote(edtNote.getText().toString());
                        myCamera.setType(MyCameraInfo.TYPE_DIRECT);
                        if (rbCamOne.isChecked()) {
                            myCamera.setCamNumber(1);
                        } else if (rbCamTwo.isChecked()) {
                            myCamera.setCamNumber(2);
                        }
                        //保存到本地数据库
                        if (!MyCameraDB.getInstance(MainActivity.this).saveMyCamera(myCamera)) {
                            Toast.makeText(MainActivity.this, "添加远程摄像头失败，目标已存在", Toast.LENGTH_SHORT).show();
                        } else {
                            mCameraList.add(myCamera);
                            ((MyAdapter) mLVCamera.getAdapter()).notifyDataSetChanged();
                        }
                    }
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (id == R.id.nav_scan_qr) {
            new IntentIntegrator(this).initiateScan();
        } else if (id == R.id.nav_playback) {
            Intent intent = new Intent(this, SelectFileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_multi_monitor) {
            Intent intent = new Intent(this, MultiMonitorActivity.class);
            ArrayList<MyCameraInfo> checkedCameraList = new ArrayList<>();
            for (MyCameraInfo myCamera : mCameraList) {
                if (myCamera.isChecked()) {
                    checkedCameraList.add(myCamera);
                }
            }
            intent.putExtra("cameraList", checkedCameraList);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            File file = new File(this.getPackageCodePath());
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            final EditText contentEdt = new EditText(this);
            contentEdt.setHint("请输入对本应用程序的建议");
            contentEdt.setMinLines(5);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("用户反馈")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(contentEdt)
                    .setPositiveButton("发送", new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (contentEdt.getText().toString().trim().length() == 0) {
                                Snackbar.make(mLVCamera, "请输入内容", Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            new AsyncTask<String, Integer, Boolean>() {
                                @Override
                                protected void onPostExecute(Boolean aBoolean) {
                                    Snackbar.make(mLVCamera, "反馈成功，感谢您的参与", Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                protected Boolean doInBackground(String... params) {
                                    try {
                                        Properties props = new Properties();
                                        props.setProperty("mail.debug", "true");
                                        props.setProperty("mail.smtp.auth", "true");
                                        props.setProperty("mail.host", "smtp.163.com");
                                        props.setProperty("mail.transport.protocol", "smtp");
                                        Session session = Session.getInstance(props);
                                        MimeMessage msg = new MimeMessage(session);
                                        msg.setSubject("远程监控系统 - 用户反馈");
                                        msg.setText(params[0]);
                                        msg.setFrom(new InternetAddress("pwrliang@163.com"));
                                        Transport transport = session.getTransport();
                                        transport.connect("smtp.163.com", "pwrliang@163.com", "ddgeng_93418");
                                        transport.sendMessage(msg, new javax.mail.Address[]{
                                                new InternetAddress("pwrliang@163.com")});
                                        transport.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return true;
                                }
                            }.execute(contentEdt.getText().toString());
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //扫码成功返回JSON
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String jsonText = scanResult.getContents();
            if (TextUtils.isEmpty(jsonText)) return;
            try {
                JSONObject jsonObject = new JSONObject(jsonText);
                String IP = jsonObject.getString("IP");
                int Port = jsonObject.getInt("PORT");
                String Password = jsonObject.getString("PASSWORD");
                int camNumber = jsonObject.getInt("CAMNUMBER");
                String deviceId = jsonObject.getString("DEVICEID");
                MyCameraInfo myCamera = new MyCameraInfo();
                myCamera.setIP(IP);
                myCamera.setPort(Port);
                myCamera.setPassword(Password);
                myCamera.setCamNumber(camNumber);
                myCamera.setDeviceId(deviceId);
                //保存到本地数据库
                if (!MyCameraDB.getInstance(MainActivity.this).saveMyCamera(myCamera)) {
                    Toast.makeText(MainActivity.this, "添加远程摄像头失败，目标已存在", Toast.LENGTH_SHORT).show();
                } else {
                    mCameraList.add(myCamera);
                    ((MyAdapter) mLVCamera.getAdapter()).notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
