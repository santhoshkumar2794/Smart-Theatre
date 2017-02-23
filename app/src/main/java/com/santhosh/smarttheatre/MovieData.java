package com.santhosh.smarttheatre;

import java.io.Serializable;

/**
 * Created by santhosh-3366 on 06/02/17.
 */

public class MovieData implements Serializable {
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
}
