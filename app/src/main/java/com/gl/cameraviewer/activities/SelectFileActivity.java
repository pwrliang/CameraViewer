package com.gl.cameraviewer.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.gl.cameraviewer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectFileActivity extends AppCompatActivity {
    private ListView mListView;
    private List<File> mVideoFiles;
    private ArrayAdapter<File> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.asf_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mListView = (ListView) findViewById(R.id.asf_listview);
        mVideoFiles = new ArrayList<>();
        listVideos(mVideoFiles);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mVideoFiles);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SelectFileActivity.this, com.gl.cameraviewer.activities.PlayerActivity.class);
                intent.putExtra("path", mVideoFiles.get(position).getAbsolutePath());
                startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SelectFileActivity.this);
                builder.setMessage("是否删除视频文件？");
                builder.setTitle("确认");

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mVideoFiles.get(position).delete()) {
                            listVideos(mVideoFiles);
                            mAdapter.notifyDataSetChanged();
                        }
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
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void listVideos(List<File> videoFiles) {
        File OutputFile = new File(Environment.getExternalStorageDirectory().getPath());
        File videoDir = new File(OutputFile.getAbsolutePath() + "/DCIM/Monitor");
        File[] files = videoDir.listFiles();
        videoFiles.clear();
        if (files != null) {
            for (File file : files) {
                videoFiles.add(file);
            }
        }
    }
}
