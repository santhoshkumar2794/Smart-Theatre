package com.santhosh.smarttheatre.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.santhosh.smarttheatre.MovieData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by santhosh-3366 on 20/03/17.
 */

public class FavouriteDataSet {

    private SQLiteDatabase sqLiteDatabase;
    private DataBaseHelper sqLiteOpenHelper;

    public FavouriteDataSet(Context context) {
        sqLiteOpenHelper = new DataBaseHelper(context, DataBaseHelper.DATABASE_NAME, null, DataBaseHelper.DATABASE_VERSION);
    }

    public void openDB() {
        sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
    }

    public void closeDB() {
        sqLiteOpenHelper.close();
    }

    public void insertItem(MovieData movieData) {
        if (!movieData.isFavourites()) {
            updateDB(movieData);
            return;
        }
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.MOVIE_ID, movieData.getId());
        values.put(DataBaseHelper.MOVIE_NAME, movieData.getTitle());
        values.put(DataBaseHelper.MOVIE_POSTER_PATH, movieData.getPoster_path());
        values.put(DataBaseHelper.MOVIE_BACKDROP_PATH, movieData.getBackdrop_path());
        values.put(DataBaseHelper.MOVIE_OVERVIEW, movieData.getOverview());
        values.put(DataBaseHelper.MOVIE_RELEASE_DATE, movieData.getRelease_date());
        values.put(DataBaseHelper.MOVIE_ORIGINAL_LANGUAGE, movieData.getOriginal_language());
        values.put(DataBaseHelper.MOVIE_VOTE_COUNT, movieData.getVote_count());
        values.put(DataBaseHelper.MOVIE_FAVOURITE,(movieData.isFavourites()?1:0));
        long colID = sqLiteDatabase.insert(DataBaseHelper.MOVIE_TABLE_NAME, null, values);
    }

    public int getData(int movieID) {
        Cursor cursor = sqLiteDatabase.query(DataBaseHelper.MOVIE_TABLE_NAME, new String[]{DataBaseHelper.MOVIE_ID, DataBaseHelper.MOVIE_NAME}, DataBaseHelper.MOVIE_ID + "=?", new String[]{String.valueOf(movieID)}, null, null, null, null);
        cursor.moveToFirst();
        cursor.close();
        return movieID;
    }

    public boolean hasItem(int movieID) {
        Cursor cursor = sqLiteDatabase.query(DataBaseHelper.MOVIE_TABLE_NAME, new String[]{DataBaseHelper.MOVIE_ID, DataBaseHelper.MOVIE_NAME}, DataBaseHelper.MOVIE_ID + "=?", new String[]{String.valueOf(movieID)}, null, null, null, null);
        return cursor.getCount() == 1;
    }

    public void updateDB(MovieData movieData) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.MOVIE_ID, movieData.getId());
        values.put(DataBaseHelper.MOVIE_NAME, movieData.getTitle());
        int deleteStatus = sqLiteDatabase.delete(DataBaseHelper.MOVIE_TABLE_NAME, DataBaseHelper.MOVIE_ID + "=" + movieData.getId(), null);
    }

    public List<MovieData> getFavList() {
        List<MovieData> movieDataList = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + DataBaseHelper.MOVIE_TABLE_NAME, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                MovieData movieData = new MovieData();

                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    Field name = movieData.getClass().getDeclaredField(cursor.getColumnName(i));
                    name.setAccessible(true);
                    switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_INTEGER:
                            name.set(movieData, cursor.getInt(i));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            name.set(movieData, cursor.getString(i));
                            break;
                    }

                }
                cursor.moveToNext();
                movieDataList.add(movieData);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        cursor.close();
        return movieDataList;
    }
}
