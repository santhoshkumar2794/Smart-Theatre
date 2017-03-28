package com.santhosh.smarttheatre.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Created by santhosh-3366 on 28/03/17.
 */

public class FavContentProvider extends ContentProvider {

    static final String AUTHORITY = "com.santhosh.smarttheatre";
    private DataBaseHelper mDataBaseHelper;
    private static UriMatcher sUriMatcher = buildUriMatcher();
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY).buildUpon().appendPath(DataBaseHelper.MOVIE_TABLE_NAME).build();

    static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,DataBaseHelper.MOVIE_TABLE_NAME,100);
        uriMatcher.addURI(AUTHORITY,DataBaseHelper.MOVIE_TABLE_NAME+"/"+DataBaseHelper.MOVIE_ID+"/#",101);
        return uriMatcher;
    }
    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDataBaseHelper = new DataBaseHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase sqLiteDatabase = mDataBaseHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor;
        switch (match){
            case 100:
                cursor = sqLiteDatabase.query(DataBaseHelper.MOVIE_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case 101:
                cursor = sqLiteDatabase.query(DataBaseHelper.MOVIE_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI : "+uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase sqLiteDatabase = mDataBaseHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match){
            case 100:
                long ID = sqLiteDatabase.insert(DataBaseHelper.MOVIE_TABLE_NAME,null,values);
                if (ID>0){
                    returnUri = ContentUris.withAppendedId(CONTENT_URI,ID);
                }else {
                    throw new android.database.SQLException("Failed to insert row into "+uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI : "+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase sqLiteDatabase = mDataBaseHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int status;
        switch (match){
            case 100:
                status = sqLiteDatabase.delete(DataBaseHelper.MOVIE_TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI : "+uri);
        }
        return status;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
