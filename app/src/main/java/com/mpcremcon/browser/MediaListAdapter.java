package com.mpcremcon.browser;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mpcremcon.R;
import com.mpcremcon.network.Commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Oleh Chaplya on 05.11.2014.
 */
public class MediaListAdapter extends ArrayAdapter<MediaEntity> {

    List<MediaEntity> dataList;
    LayoutInflater inflater;
    View rowView;

    public MediaListAdapter(Context context, List<MediaEntity> objects) {
        super(context, R.layout.media_list_item, objects);

        this.dataList = objects;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MediaEntity e = dataList.get(position);
        rowView = inflater.inflate(R.layout.media_list_item, parent, false);

        ImageView img = (ImageView) rowView.findViewById(R.id.MediaListItemImageView);
        TextView textView = (TextView) rowView.findViewById(R.id.MediaListItemTextView);

        if(e.getName().equals("..")) {
            img.setImageResource(R.drawable.folderup);
        } else if(e.getFormat() == MediaFormats.Format.DIR) {
            img.setImageResource(R.drawable.folder);
        } else if(e.getFormat() == MediaFormats.Format.VIDEO) {
            img.setImageResource(R.drawable.videofile);
        } else if(e.getFormat() == MediaFormats.Format.AUDIO) {
            img.setImageResource(R.drawable.soundfile);
        }

        textView.setText(e.getName());

        return rowView;
    }

    @Override
    public void addAll(final Collection<? extends MediaEntity> collection) {
        dataList.clear();
        dataList.addAll(collection);
    }

    @Override
    public MediaEntity getItem(int position) {
        return dataList.get(position);
    }

    static class ViewHolder {
        TextView name;
        int position;
    }
}
