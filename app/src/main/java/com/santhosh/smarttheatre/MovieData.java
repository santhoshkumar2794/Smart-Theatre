package com.santhosh.smarttheatre;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

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
    float popularity,vote_average;
    boolean adult,video;
    MovieData() {
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
}
