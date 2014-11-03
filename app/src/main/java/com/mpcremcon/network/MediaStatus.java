package com.mpcremcon.network;

/**
 * Represents current status of media played
 *
 * Created by Oleh Chaplya on 03.11.2014.
 */
public class MediaStatus {
    String filename;
    int position;

    String positionString;
    int duration;

    String durationString;
    int volumeLevel;

    Boolean isMuted;
    Boolean playState;

    public MediaStatus(String filename, int position, String positionString, int duration, String durationString, int volumeLevel, Boolean isMuted, Boolean playState) {
        this.filename = filename;
        this.position = position;
        this.positionString = positionString;
        this.duration = duration;
        this.durationString = durationString;
        this.volumeLevel = volumeLevel;
        this.isMuted = isMuted;
        this.playState = playState;
    }

    public String getFilename() {
        return filename;
    }

    public int getPosition() {
        return position;
    }

    public String getPositionString() {
        return positionString;
    }

    public int getDuration() {
        return duration;
    }

    public String getDurationString() {
        return durationString;
    }

    public int getVolumeLevel() {
        return volumeLevel;
    }

    public Boolean getIsMuted() {
        return isMuted;
    }

    public Boolean getPlayState() {
        return playState;
    }

    @Override
    public String toString() {
        return "MediaStatus{" +
                "filename='" + filename + '\'' +
                ", position=" + position +
                ", positionString='" + positionString + '\'' +
                ", duration=" + duration +
                ", durationString='" + durationString + '\'' +
                ", volumeLevel=" + volumeLevel +
                ", isMuted=" + isMuted +
                ", playState=" + playState +
                '}';
    }
}
