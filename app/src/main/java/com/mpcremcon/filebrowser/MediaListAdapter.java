package com.mpcremcon.filebrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mpcremcon.R;

import java.util.Collection;
import java.util.List;

/**
 * Created by Oleh Chaplya on 05.11.2014.
 */
public class MediaListAdapter extends ArrayAdapter<MediaEntity> {

    List<MediaEntity> dataList;
    LayoutInflater inflater;

    public MediaListAdapter(Context context, List<MediaEntity> objects) {
        super(context, R.layout.media_list_item, objects);

        this.dataList = objects;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ImageView imageView;
        TextView textView;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.media_list_item, parent, false);

            imageView = (ImageView) convertView.findViewById(R.id.MediaListItemImageView);
            textView = (TextView) convertView.findViewById(R.id.MediaListItemTextView);

            convertView.setTag(new ViewHolder(imageView, textView));
        } else {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            imageView = viewHolder.imageView;
            textView = viewHolder.textView;
        }

        MediaEntity e = getItem(position);

        if(e.getName().equals("..")) {
            imageView.setImageResource(R.drawable.folderup);
        } else if(e.getMediaType() == MediaFormats.Type.DIR) {
            imageView.setImageResource(R.drawable.folder);
        } else if(e.getMediaType() == MediaFormats.Type.VIDEO) {
            imageView.setImageResource(R.drawable.videofile);
        } else if(e.getMediaType() == MediaFormats.Type.AUDIO) {
            imageView.setImageResource(R.drawable.soundfile);
        }

        textView.setText(e.getName());

        return convertView;
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
        ImageView imageView;
        TextView textView;

        ViewHolder(ImageView imageView, TextView textView) {
            this.imageView = imageView;
            this.textView = textView;
        }
    }

}
