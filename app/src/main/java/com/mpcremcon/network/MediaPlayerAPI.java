package com.mpcremcon.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mpcremcon.filebrowser.MediaEntity;
import com.mpcremcon.filebrowser.MediaEntityList;
import com.mpcremcon.filebrowser.MediaFormats;
import com.mpcremcon.localdb.LocalSettings;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Basic class for sending requests
 *
 * Created by Oleh Chaplya on 16.07.2014.
 */
public class MediaPlayerAPI {
    final String TAG = "Connection";
    static final HttpClient client = new DefaultHttpClient();

        /**
     * Sends commands to MPC player
     */
    synchronized public void execCommand(final int c) {
        try {
            Document doc = Jsoup.connect(createRequest(c)).get();
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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

            Boolean isMuted;
            if(doc.select("#muted").text().equals("0")) isMuted = false;
            else isMuted = true;

            int t = Integer.valueOf(doc.select("#state").text());
            Boolean playState;

            if(t == 1) playState = false;
            else playState = true;

            ms = new MediaStatus(filename, pos, posStr, dur, durStr, volume, isMuted, playState);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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

    /**
     * Returns current folder data in list
     * Set path to "" for default folder data
     *
     * @return MediaEntityList with elements of null if error
     */
    synchronized public MediaEntityList getBrowser(String path) {
        MediaEntityList mel = null;

        ArrayList<MediaEntity> list = new ArrayList<MediaEntity>();

        if(path.equals("")) path = "/browser.html?";

        try {
            Document doc = Jsoup.connect(createBaseURL() + path).get();
            Elements tr = doc.getElementsByClass("browser-table").last().getElementsByTag("tr");

            // parse path
            path = doc.select("td.text-center").text();
            path = path.substring(path.indexOf(" ")).trim();

            // parse all elements
            for(Element i: tr) {
                // skip first, because there is no data
                if(i == tr.get(0)) continue;

                try {
                    if (i.className().equals("")) {
                        // parse as directory
                        Element e = i.select("td.dirname").first().select("a").first();
                        String dirpath = e.attr("href");
                        String dirname = e.text();
                        String dirtype = e.select("dirtype").text();
                        String dirsize = e.select("dirsize").text();
                        String dirdate = e.select("dirdate").text();

                        list.add(new MediaEntity(dirname, dirtype, dirsize, dirdate, dirpath, MediaFormats.Type.DIR));
                    } else {
                        // parse as audio or video file
                        Elements elements = i.getAllElements();

                        if(Arrays.asList(MediaFormats.VIDEO).contains(i.attr("class").toUpperCase())) {
                            String dirpath = elements.get(0).select("a").attr("href");
                            String dirname = elements.get(0).select("a").text();
                            String dirtype = elements.get(1).select("td span").text();
                            String dirsize = elements.get(2).select("td span").text();
                            String dirdate = elements.get(3).select("td span").text();

                            list.add(new MediaEntity(dirname, dirtype, dirsize, dirdate, dirpath, MediaFormats.Type.VIDEO));
                        } else if (Arrays.asList(MediaFormats.AUDIO).contains(i.attr("class").toUpperCase())) {
                            String dirpath = elements.get(0).select("a").attr("href");
                            String dirname = elements.get(0).select("a").text();
                            String dirtype = elements.get(1).select("td span").text();
                            String dirsize = elements.get(2).select("td span").text();
                            String dirdate = elements.get(3).select("td span").text();

                            list.add(new MediaEntity(dirname, dirtype, dirsize, dirdate, dirpath, MediaFormats.Type.AUDIO));
                        }

                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            mel = new MediaEntityList(path, list);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return mel;
    }

}
