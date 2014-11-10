package com.mpcremcon.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mpcremcon.R;
import com.mpcremcon.filebrowser.MediaEntity;
import com.mpcremcon.filebrowser.MediaEntityList;
import com.mpcremcon.filebrowser.MediaListAdapter;
import com.mpcremcon.network.Commands;
import com.mpcremcon.network.MediaPlayerAPI;
import com.mpcremcon.network.Connection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleh Chaplya on 05.11.2014.
 */
public class FileBrowser extends Activity {

    static final String TAG = "FileBrowser";
    public static Boolean IS_DATA_UPDATING = false;

    // ui
    ListView mediaList;
    MediaListAdapter adapter;

    Handler listHandler;
    MediaPlayerAPI conn;

    private List<MediaEntity> dataList = new ArrayList<MediaEntity>();
    private Connection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_list);

        conn = new MediaPlayerAPI();
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
    }

    @Override
    protected void onResume() {
        try {
            if (Main.service != null && serviceConnection != null)
                bindService(Main.service, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch(Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            if(serviceConnection != null)
                unbindService(serviceConnection);
            listHandler.removeCallbacksAndMessages(null);
        } catch(Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        try {
            if(serviceConnection != null)
                unbindService(serviceConnection);
            listHandler.removeCallbacksAndMessages(null);
        } catch(Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
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

        serviceConnection = new Connection();
        serviceConnection.setListHandler(listHandler);
        bindService(Main.service, serviceConnection, Context.BIND_AUTO_CREATE);
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
        serviceConnection.queryMediaBrowser(path);
    }
}
