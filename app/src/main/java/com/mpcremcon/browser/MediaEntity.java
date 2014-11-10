package com.mpcremcon.browser;

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

    private MediaFormats.Format Format;

    public MediaEntity(String name) {
        Name = name;
    }

    public MediaEntity(String name, String type, String size, String date, String DirPath, MediaFormats.Format format) {
        this.Name = name;
        this.Type = type;
        this.Size = size;
        this.Date = date;
        this.DirPath = DirPath;
        Format = format;
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

    public MediaFormats.Format getFormat() {
        return Format;
    }

    @Override
    public String toString() {
        return "MediaEntity{" +
                "Name='" + Name + '\'' +
                '}';
    }
}
