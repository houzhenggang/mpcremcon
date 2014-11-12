package com.mpcremcon.network;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.mpcremcon.filebrowser.MediaEntityList;
import com.mpcremcon.ui.MainActivity;

/**
 * Main service, which runs tasks in background.
 * Sends requests to MPC server and returns responses
 * to UI thread for updating UI
 *
 * Created by Oleh Chaplya on 3.10.2014.
 */
public class BackgroundService extends Service {

    public static final int POOLING_WAIT_TIME = 250;
    public static final int SNAPSHOT_WAIT_TIME = 5000;
    final String TAG = "BGService";
    final IBinder dataBinder;
    MediaPlayerAPI mediaPlayerAPI;

    public static Boolean stopAll = false;

    public BackgroundService() {
        dataBinder = new DataBinder(this);
    }

    public void onCreate() {
        super.onCreate();
        mediaPlayerAPI = new MediaPlayerAPI();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return dataBinder;
    }

    /**
     * Runs a thread in background and pools server every 300 ms
     * Response is returned as MediaStatus object by handler
     * to UI thread
     *
     * @param handler Handler for whom message will be sent
     */
    synchronized public void task(final Handler handler) {
        new Thread(new Runnable() {
            synchronized public void run() {
                MediaStatus ms;
                try {
                    ms = mediaPlayerAPI.getStatus();
                    if(ms != null) {
                        Message msg = new Message();
                        msg.obj = ms;
                        msg.what = Commands.MEDIADATA;
                        handler.sendMessage(msg);
                    } else {
                        handler.sendEmptyMessage(Commands.DISCONNECTED);
                    }

                    wait(POOLING_WAIT_TIME);
                } catch (InterruptedException e) {
                    Log.d(TAG, "task error");
                }
            }
        }).start();
    }

    /**
     * Loads snapshot of video and returns it by handler to UI thread
     *
     * @param handler handler, where to send the result image
     */
    synchronized public void loadSnapshot(final Handler handler) {
        new Thread(new Runnable() {
            synchronized public void run() {
                Bitmap bmp;

                try {
                    bmp = mediaPlayerAPI.loadSnapshot();
                    if(bmp != null) {
                        Message msg = new Message();
                        msg.obj = bmp;
                        msg.what = Commands.SNAPSHOT;
                        handler.sendMessage(msg);
                    } else {
                        handler.sendEmptyMessage(Commands.DISCONNECTED);
                    }
                    wait(SNAPSHOT_WAIT_TIME);
                } catch (InterruptedException e) {
                    Log.d(TAG, "loadSnapshot error");
                }
            }
        }).start();
    }

    /**
     * Sends some request
     * Code is taken from 'Commands' class
     * @param value Request code
     */
    public void execCommand(final int value) {
        new Thread(new Runnable() {
            public void run() {
                mediaPlayerAPI.execCommand(value);
            }
        }).start();
    }

    /**
     * Sets the position of seek bar
     * @param value from 0 to max duration of video
     */
    public void setPosition(final int value) {
        new Thread(new Runnable() {
            public void run() {
                mediaPlayerAPI.setPosition(value);
            }
        }).start();
    }

    /**
     * Sets the volume from 0 to 100%
     * @param value from 0 to 99
     */
    public void setVolume(final int value) {
        new Thread(new Runnable() {
            public void run() {
                mediaPlayerAPI.setVolume(value);
            }
        }).start();
    }

    /**
     * Sends queries to get state of media browser
     * @param handler handler where send results
     * @param path path to query
     */
    synchronized public void queryMediaBrowser(final Handler handler, final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.IS_DATA_UPDATING = true;
                MediaEntityList m = mediaPlayerAPI.getBrowser(path);
                Message msg = new Message();

                if(m != null) {
                    msg.what = Commands.NEWDATA;
                    msg.obj = m;

                    handler.sendMessage(msg);
                } else {
                    handler.sendEmptyMessage(Commands.ERROR);
                }
                MainActivity.IS_DATA_UPDATING = false;
            }
        }).start();
    }

    /**
     * Data binder
     * Used only to get a service instance
     */
    public class DataBinder extends Binder {
        BackgroundService service;

        public DataBinder(BackgroundService service) {
            this.service = service;
        }

        BackgroundService getService() {
            return service;
        }
    }

}
