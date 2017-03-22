package com.santhosh.smarttheatre.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by santhosh-3366 on 20/03/17.
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    static final int DATABASE_VERSION = 2;
    static String DATABASE_NAME = "favourites.db";
    static final String MOVIE_TABLE_NAME = "movie_table";
    static String MOVIE_ID = "id";
    static String MOVIE_NAME = "title";
    static String MOVIE_POSTER_PATH = "poster_path";
    static String MOVIE_BACKDROP_PATH = "backdrop_path";
    static String MOVIE_OVERVIEW = "overview";
    static String MOVIE_RELEASE_DATE = "release_date";
    static String MOVIE_ORIGINAL_LANGUAGE = "original_language";
    static String MOVIE_VOTE_COUNT = "vote_count";
    static String MOVIE_POPULARITY = "popularity";
    static String MOVIE_VOTE_AVERAGE = "vote_average";
    static String MOVIE_FAVOURITE = "favourite";

    private static final String TABLE_CREATE = "CREATE TABLE " + MOVIE_TABLE_NAME + " (" +
            MOVIE_ID + " INTEGER PRIMARY KEY, " +
            MOVIE_NAME + " TEXT,"+MOVIE_POSTER_PATH+" TEXT,"+MOVIE_BACKDROP_PATH+" TEXT,"+MOVIE_OVERVIEW+" TEXT,"+
    MOVIE_RELEASE_DATE+" TEXT,"+MOVIE_ORIGINAL_LANGUAGE+" TEXT,"+MOVIE_VOTE_COUNT+" INTEGER,"+MOVIE_FAVOURITE+ " INTEGER);";


    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }
}