package com.mpcremcon.network;

/**
 * Created by Oleh Chaplya on 16.07.2014.
 */
public class Commands {
    // General commands
    public static final int Play = 887;
    public static final int Pause = 888;
    public static final int PLAY_PAUSE = 889;
    public static final int Stop = 890;
    public static final int Prev = 921;
    public static final int Next = 922;

    public static final int SetPosition = -1;
    public static final int SetVolume = -2;

    public static final int Fullscreen = 830;
    public static final int Mute = 909;

    // custom commands
    public static final int SNAPSHOT = 1;
    public static final int MEDIADATA = 2;
    public static final int DISCONNECTED = 3;
}
