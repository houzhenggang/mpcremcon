package com.mpcremcon.network;

/**
 * Created by Oleh Chaplya on 16.07.2014.
 */
public class Commands {
    // General commands
    public static final int PLAY = 887;
    public static final int PAUSE = 888;
    public static final int PLAY_PAUSE = 889;
    public static final int STOP = 890;
    public static final int PREV = 921;
    public static final int NEXT = 922;

    public static final int SET_POSITION = -1;
    public static final int SET_VOLUME = -2;

    public static final int FULLSCREEN = 830;
    public static final int MUTE = 909;
    public static final int EXIT_PLAYER = 816;

    // custom commands
    public static final int SNAPSHOT = 1;
    public static final int MEDIADATA = 2;
    public static final int DISCONNECTED = 3;

    public static final int NEWDATA = 4;
    public static final int ERROR = 5;
    public static final int NOTIFY_DATA_CHANGED = 6;
}
