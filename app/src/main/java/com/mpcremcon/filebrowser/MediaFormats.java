package com.mpcremcon.filebrowser;

/**
 * Created by Oleh Chaplya on 10.11.2014.
 */
public class MediaFormats {
    public static final String[] VIDEO =  {  "3GPP", "3GPP2", "3GPP-TT", "BMPEG", "DV", "H261", "H263", "H264", "MP4", "MPV", "MPEG", "NV", "OGG", "RAW", "MKV", "AVI", "FLV" };
    public static final String[] AUDIO =  {  "MP3", "OGG", "FLAC", "AIFF", "M4V" };

    public static enum Type {
        DIR, AUDIO, VIDEO
    }
}
