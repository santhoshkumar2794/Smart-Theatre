package com.santhosh.smarttheatre;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;

/**
 * Created by santhosh-3366 on 05/02/17.
 */

public class RecyclerMovieHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView movieName;
    ImageView movieThumbnail;
    MovieData movieData;
    public RecyclerMovieHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        movieName = (TextView) itemView.findViewById(R.id.holder_text);
        movieThumbnail = (ImageView) itemView.findViewById(R.id.holder_image);
    }

    public RecyclerMovieHolder(View itemView, MovieData movieData) {
        super(itemView);
        itemView.setOnClickListener(this);
        movieName = (TextView) itemView.findViewById(R.id.holder_text);
        movieThumbnail = (ImageView) itemView.findViewById(R.id.holder_image);
        this.movieData = movieData;
    }

    public MovieData getMovieData() {
        return movieData;
    }

    public void setMovieData(MovieData movieData) {
        this.movieData = movieData;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(),MovieActivity.class);
        Bundle bundle = captureValues(movieThumbnail);
        bundle.putParcelable("movieData", movieData);
        intent.putExtra("extra",bundle);
        view.getContext().startActivity(intent);
        ((MainActivity) view.getContext()).overridePendingTransition(0,0);
    }


    private Bundle captureValues(@NonNull final View view) {
        Bundle b = new Bundle();
        int[] screenLocation = new int[2];

        view.getLocationOnScreen(screenLocation);
        b.putInt("PROPNAME_SCREENLOCATION_LEFT", screenLocation[0]);
        b.putInt("PROPNAME_SCREENLOCATION_TOP", screenLocation[1]);
        b.putInt("PROPNAME_WIDTH", view.getWidth());
        b.putInt("PROPNAME_HEIGHT", view.getHeight());
        return b;
    }
}
