package com.santhosh.smarttheatre;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by santhosh-3366 on 05/02/17.
 */

public class MovieAdapter extends ArrayAdapter<MovieHolder> {
    public MovieAdapter(Context context, int resource) {
        super(context, resource);
    }

    public MovieAdapter(Context context, int resource, List<MovieHolder> objects) {
        super(context, resource, objects);
    }



    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieHolder movieHolder = getItem(position);
        if (convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_holder, parent,false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.holder_text);
        //textView.setCompoundDrawablesWithIntrinsicBounds(0,movieHolder.movieThumbnailID,0,0);
        textView.setText(movieHolder.movieName);
        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }
}
