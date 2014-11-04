package com.mpcremcon.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mpcremcon.localdb.LocalSettings;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;

/**
 * Basic class for sending requests
 *
 * Created by Oleh Chaplya on 16.07.2014.
 */
public class Connection {
    final String TAG = "Connection";
    static final HttpClient client = new DefaultHttpClient();

    /**
     * Sends commands to MPC player
     */
    synchronized public void execCommand(final int c) {
        try {
            Document doc = Jsoup.connect(createRequest(c)).get();
        } catch (Exception e) {
            Log.d(TAG, "execCommand error");
        }
    }

    /**
     * Sets the position value in percents
     * @param value in percents from 0 to 99
     */
    synchronized public void setPosition(int value) {
        try {
            String req = createRequest(Commands.SET_POSITION) + "&percent=" + String.valueOf(value);
            Document doc = Jsoup.connect(req).get();
            Log.d(TAG, req);
        } catch (Exception e) {
            Log.d(TAG, "setPosition error");
        }
    }

    /**
     * Sets the volume value in percents
     * @param value in percents from 0 to 99
     */
    synchronized public void setVolume(int value) {
        try {
            String req = createRequest(Commands.SET_VOLUME) + "&volume=" + String.valueOf(value);
            Document doc = Jsoup.connect(req).get();
            Log.d(TAG, req);
        } catch (Exception e) {
            Log.d(TAG, "setVolume error");
        }
    }

    /**
     * Gets a status of current media player
     * @return MediaStatus object. Returns null if getStatus failed
     */
    synchronized public MediaStatus getStatus() {
        MediaStatus ms = null;

        try {
            Document doc = Jsoup.connect(createBaseURL() + "/variables.html").get();

            String[] fp = doc.select("#filepath").text().split("\\\\");
            String filename = fp[fp.length-1];

            int pos = Integer.valueOf(doc.select("#position").text());
            String posStr = doc.select("#positionstring").text();

            int dur = Integer.valueOf(doc.select("#duration").text());
            String durStr = doc.select("#durationstring").text();

            int volume = Integer.valueOf(doc.select("#volumelevel").text());
            Boolean isMuted = Boolean.valueOf(doc.select("#muted").text());

            int t = Integer.valueOf(doc.select("#state").text());
            Boolean playState;

            if(t == 1) playState = false;
            else playState = true;

            ms = new MediaStatus(filename, pos, posStr, dur, durStr, volume, isMuted, playState);
        } catch (Exception e) {
            Log.d(TAG, "getStatus error");
        }
        return ms;
    }

    /**
     * Loads a shapshot image
     * @return a Bitmap image of null if error
     */
    synchronized public Bitmap loadSnapshot() {
        Bitmap bmp = null;
        try {
            URL url = new URL(createBaseURL() + "/snapshot.jpg");
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (Exception e) {
            Log.d(TAG, "snapshot load error");
        }
        return bmp;
    }

    /**
     * Creates a URI from command code
     * @param c Command code
     * @return full http get uri
     */
    private String createRequest(int c) {
        return createBaseURL() + "/command.html?wm_command=" + String.valueOf(c);
    }

    /**
     * Creates base URI based on local settings
     * @return a URI string
     */
    private String createBaseURL() {
        return "http://" + LocalSettings.getIPAddress() + ":" + LocalSettings.getPort();
    }
}
