package com.mpcremcon.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mpcremcon.R;
import com.mpcremcon.browser.MediaEntity;
import com.mpcremcon.browser.MediaEntityList;
import com.mpcremcon.browser.MediaListAdapter;
import com.mpcremcon.network.Commands;
import com.mpcremcon.network.Connection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleh Chaplya on 05.11.2014.
 */
public class MediaBrowser extends Activity {

    static final String TAG = "MediaBrowser";
    static Boolean IS_DATA_UPDATING = false;

    // ui
    ListView mediaList;
    MediaListAdapter adapter;

    Handler listHandler;
    Connection conn;

    String currentPath = "";
    private List<MediaEntity> dataList = new ArrayList<MediaEntity>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_list);

        conn = new Connection();
        mediaList = (ListView) findViewById(R.id.mediaList);
        adapter = new MediaListAdapter(this, dataList);

        mediaList.setAdapter(adapter);
        mediaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                String path = adapter.getItem(pos).getDirPath();
                if(!IS_DATA_UPDATING) queryMediaBrowser(path);
            }
        });

        initHandler();
        if(!IS_DATA_UPDATING) queryMediaBrowser("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //queryMediaBrowser("");
    }

    private void initHandler() {
        listHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case Commands.NEWDATA: {
                        // update ui dataList adn change data to recent
                        MediaEntityList data = (MediaEntityList) msg.obj;
                        setTitle(data.getPath());
                        updateListData(data);
                        //adapter.addAll(data.getList());
                        //adapter.notifyDataSetChanged();
                        break;
                    }
                    case Commands.ERROR: {
                        break;
                    }
                    case Commands.NOTIFY_DATA_CHANGED: {
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        };
    }

    private void updateListData(final MediaEntityList data) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                dataList.clear();
                dataList.addAll(data.getList());
                listHandler.sendEmptyMessage(Commands.NOTIFY_DATA_CHANGED);
            }
        }).start();
    }

    /**
     * Sends requests to get current state of media browser
     */
    private void queryMediaBrowser(final String path) {
        setTitle("Loading new data...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                IS_DATA_UPDATING = true;
                MediaEntityList m = conn.getBrowser(path);
                Message msg = new Message();

                if(m != null) {
                    msg.what = Commands.NEWDATA;
                    msg.obj = m;

                    listHandler.sendMessage(msg);
                } else {
                    listHandler.sendEmptyMessage(Commands.ERROR);
                }
                IS_DATA_UPDATING = false;
            }
        }).start();
    }
}
