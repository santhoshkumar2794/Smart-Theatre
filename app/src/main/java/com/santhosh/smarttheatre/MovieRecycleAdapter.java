package com.santhosh.smarttheatre;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by santhosh-3366 on 05/02/17.
 */

public class MovieRecycleAdapter extends RecyclerView.Adapter<RecyclerMovieHolder> {


    private List<MovieData> movieDataList;
    public MovieRecycleAdapter(List<MovieData> movieDatas) {
        this.movieDataList = movieDatas;
    }



    @Override
    public RecyclerMovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerMovieHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_holder,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerMovieHolder holder, int position) {
        MovieData movieData = movieDataList.get(position);
        holder.setMovieData(movieData);
        String imageUrl = "https://image.tmdb.org/t/p/w342/"+movieData.poster_path;
        holder.movieName.setText(movieData.title);
        Picasso.with(holder.movieThumbnail.getContext()).load(imageUrl).into(holder.movieThumbnail);
    }

    @Override
    public int getItemCount() {
        return movieDataList.size();
    }


}
