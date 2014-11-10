package com.mpcremcon.network;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.mpcremcon.ui.Main;

/**
 * Main service, which runs tasks in background.
 * Sends requests to MPC server and returns responses
 * to UI thread for updating UI
 *
 * Created by Oleh Chaplya on 3.10.2014.
 */
public class BGService extends Service {

    public static final int POOLING_WAIT_TIME = 250;
    public static final int SNAPSHOT_WAIT_TIME = 5000;
    final String TAG = "BGService";
    final IBinder dataBinder;
    Connection connection;

    public static Boolean stopAll = false;

    public BGService() {
        dataBinder = new DataBinder(this);
    }

    public void onCreate() {
        super.onCreate();
        connection = new Connection();
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
     * @param uiHandler Handler for whom message will be sent
     */
    synchronized public void task(final Handler uiHandler) {
        new Thread(new Runnable() {
            synchronized public void run() {
                MediaStatus ms;

                while (!stopAll) {
                    try {
                        ms = connection.getStatus();
                        if(ms != null) {
                            Message msg = new Message();
                            msg.obj = ms;
                            msg.what = Commands.MEDIADATA;
                            uiHandler.sendMessage(msg);
                        } else {
                            uiHandler.sendEmptyMessage(Commands.DISCONNECTED);
                        }

                        wait(POOLING_WAIT_TIME);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "task error");
                    }
                }

                //stopSelf();
            }
        }).start();
    }

    /**
     * Loads snapshot of video and returns it by handler to UI thread
     *
     * @param uiHandler handler, where to send the result image
     */
    synchronized public void loadSnapshot(final Handler uiHandler) {
        new Thread(new Runnable() {
            synchronized public void run() {
                Bitmap bmp;

                while (!stopAll) {
                    try {
                        if(Main.isPaused) {
                            bmp = connection.loadSnapshot();
                            if(bmp != null) {
                                Message msg = new Message();
                                msg.obj = bmp;
                                msg.what = Commands.SNAPSHOT;
                                uiHandler.sendMessage(msg);
                            } else {
                                uiHandler.sendEmptyMessage(Commands.DISCONNECTED);
                            }
                            wait(SNAPSHOT_WAIT_TIME);
                        }

                    } catch (InterruptedException e) {
                        Log.d(TAG, "loadSnapshot error");
                    }
                }

                //stopSelf();
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
                connection.execCommand(value);
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
                connection.setPosition(value);
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
                connection.setVolume(value);
            }
        }).start();
    }

    /**
     * Sends queries to get state of media browser
     * @param listHandler
     */
    @Deprecated
    synchronized public void queryMediaBrowser(final Handler listHandler) {
        new Thread(new Runnable() {
            synchronized public void run() {
                    /*try {
                        if(Main.isPaused) {
                            if(bmp != null) {
                                Message msg = new Message();
                                msg.obj = bmp;
                                msg.what = Commands.SNAPSHOT;
                                listHandler.sendMessage(msg);
                            } else {
                                listHandler.sendEmptyMessage(Commands.DISCONNECTED);
                            }
                        }

                    } catch (InterruptedException e) {
                        Log.d(TAG, "loadSnapshot error");
                }
*/
                //stopSelf();
            }
        }).start();
    }

    /**
     * Data binder
     * Used only to get a service instance
     */
    public class DataBinder extends Binder {
        BGService service;

        public DataBinder(BGService service) {
            this.service = service;
        }

        BGService getService() {
            return service;
        }
    }

}
