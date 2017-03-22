package com.santhosh.smarttheatre;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by santhosh-3366 on 06/02/17.
 */

public class MovieData implements Parcelable {
    String title;
    String original_title,original_language;
    String poster_path;
    String backdrop_path;
    String overview;
    String release_date;
    int id,vote_count;
    int favourite=0;
    float popularity,vote_average;
    boolean adult,video, favourites = false;

    public MovieData() {
    }

    public MovieData(Cursor cursor){
        for (int i=0;i<cursor.getColumnCount();i++){

        }
    }

    protected MovieData(Parcel in) {
        title = in.readString();
        original_title = in.readString();
        original_language = in.readString();
        poster_path = in.readString();
        backdrop_path = in.readString();
        overview = in.readString();
        release_date = in.readString();
        id = in.readInt();
        vote_count = in.readInt();
        popularity = in.readFloat();
        vote_average = in.readFloat();
        adult = in.readByte() != 0;
        video = in.readByte() != 0;
    }

    public static final Creator<MovieData> CREATOR = new Creator<MovieData>() {
        @Override
        public MovieData createFromParcel(Parcel in) {
            return new MovieData(in);
        }

        @Override
        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(original_title);
        parcel.writeString(original_language);
        parcel.writeString(poster_path);
        parcel.writeString(backdrop_path);
        parcel.writeString(overview);
        parcel.writeString(release_date);
        parcel.writeInt(id);
        parcel.writeInt(vote_count);
        parcel.writeFloat(popularity);
        parcel.writeFloat(vote_average);
        parcel.writeByte((byte) (adult ? 1 : 0));
        parcel.writeByte((byte) (video ? 1 : 0));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public void setOriginal_language(String original_language) {
        this.original_language = original_language;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVote_count() {
        return vote_count;
    }

    public void setVote_count(int vote_count) {
        this.vote_count = vote_count;
    }

    public float getPopularity() {
        return popularity;
    }

    public void setPopularity(float popularity) {
        this.popularity = popularity;
    }

    public float getVote_average() {
        return vote_average;
    }

    public void setVote_average(float vote_average) {
        this.vote_average = vote_average;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public boolean isFavourites() {
        return (favourites | favourite==1);
    }

    public void setFavourites(boolean favourites) {
        this.favourites = favourites;
    }

    public void setFavourite(int favourite) {
        this.favourites = favourite==1;
    }

}
