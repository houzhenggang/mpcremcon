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
public class SConnection implements ServiceConnection {
    BGService service;
    Handler uiHandler;

    public SConnection(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }

    public void onServiceConnected(ComponentName className, IBinder binder) {
        BGService.DataBinder db = (BGService.DataBinder)binder;
        service = db.getService();

        service.task(uiHandler);
        service.loadSnapshot(uiHandler);
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
}
