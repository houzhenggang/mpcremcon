package com.mpcremcon.filebrowser;

/**
 * Represents entity from media browser
 *
 * Created by Oleg on 05.11.2014.
 */
public class MediaEntity {
    private String Name;
    private String Type;
    private String Size;
    private String Date;
    private String DirPath;

    private MediaFormats.Type MediaType;

    public MediaEntity(String name) {
        Name = name;
    }

    public MediaEntity(String name, String type, String size, String date, String DirPath, MediaFormats.Type format) {
        this.Name = name;
        this.Type = type;
        this.Size = size;
        this.Date = date;
        this.DirPath = DirPath;
        this.MediaType = format;
    }

    public String getName() {
        return Name;
    }

    public String getType() {
        return Type;
    }

    public String getSize() {
        return Size;
    }

    public String getDate() {
        return Date;
    }

    public String getDirPath() {
        return DirPath;
    }

    public MediaFormats.Type getMediaType() {
        return MediaType;
    }

    @Override
    public String toString() {
        return "MediaEntity{" +
                "Name='" + Name + '\'' +
                '}';
    }
}
