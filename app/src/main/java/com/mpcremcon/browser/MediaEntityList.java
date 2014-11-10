package com.mpcremcon.browser;

import java.util.ArrayList;

/**
 * Created by Oleg on 05.11.2014.
 */
public class MediaEntityList {
    private String path;
    private ArrayList<MediaEntity> list;

    public MediaEntityList(String path) {
        this.path = path;
        list = new ArrayList<MediaEntity>();
    }

    public MediaEntityList(String path, ArrayList<MediaEntity> list) {
        this.path = path;
        this.list = list;
    }

    public String getPath() {
        return path;
    }

    public ArrayList<MediaEntity> getList() {
        return list;
    }
}
