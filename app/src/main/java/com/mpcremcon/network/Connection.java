package com.mpcremcon.network;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

/**
 * Main service connection class
 *
 * Created by Oleh Chaplya on 02.11.2014.
 */
public class Connection implements ServiceConnection {
    BackgroundService service;
    Handler uiHandler;

    public Connection(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }

    public void onServiceConnected(ComponentName className, IBinder binder) {
        BackgroundService.DataBinder db = (BackgroundService.DataBinder)binder;
        service = db.getService();

        if(uiHandler != null) {
            service.task(uiHandler);
            service.loadSnapshot(uiHandler);
            service.queryMediaBrowser(uiHandler, "");
        }
    }

    public void onServiceDisconnected(ComponentName className) {
        service = null;
    }

    /**
     * Send a command to MPC server
     * @param s Command
     */
    public void execCommand(int s) {
        service.execCommand(s);
    }

    /**
     * Set seek bar position in currently playing media
     * @param value value from 0 to 99
     */
    public void setPosition(int value) {
        service.setPosition(value);
    }

    /**
     * Set the volume bar position in currently playing media
     * @param value value from 0 to 99
     */
    public void setVolume(int value) {
        service.setVolume(value);
    }

    /**
     * Get data from path
     * @param path path to query
     */
    public void queryMediaBrowser(final String path) {
        service.queryMediaBrowser(uiHandler, path);
    }
}
