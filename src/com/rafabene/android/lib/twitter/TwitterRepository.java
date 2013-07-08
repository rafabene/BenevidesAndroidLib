package com.rafabene.android.lib.twitter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TwitterRepository extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "twitter";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_CREATED = "created";
    public static final String COLUMN_NAME_IMAGE_URL = "imageUrl";
    public static final String COLUMN_NAME_FROM_USER = "fromUser";
    public static final String COLUMN_NAME_TEXT = "text";

    public static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_CREATED + " INTEGER, " +
            COLUMN_NAME_IMAGE_URL + " TEXT, " +
            COLUMN_NAME_FROM_USER + " TEXT, " +
            COLUMN_NAME_TEXT + " TEXT " +
            " )";

    private static String[] projection = new String[] {
        COLUMN_NAME_ID,
        COLUMN_NAME_CREATED,
        COLUMN_NAME_IMAGE_URL,
        COLUMN_NAME_FROM_USER,
        COLUMN_NAME_TEXT
    };

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "Twitters.db";

    public TwitterRepository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // recreate
        onCreate(db);
    }

    public void insert(TwitterStatus twitter) {
        getWritableDatabase().insert(TABLE_NAME, null, getContentValues(twitter));
        getWritableDatabase().close();
    }

    public boolean isTwitterPersisted(Long id) {

        String selection = COLUMN_NAME_ID + " = ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = new String[] { String.valueOf(id) };

        Cursor c = getReadableDatabase().query(
            TABLE_NAME, // The table to query
            projection, // The columns to return
            selection, // The columns for the WHERE clause
            selectionArgs, // The values for the WHERE clause
            null, // don't group the rows
            null, // don't filter by row groups
            null // The sort order
            );
        boolean isPersisted = c.moveToNext();
        c.close();
        getReadableDatabase().close();
        return isPersisted;
    }

    public List<TwitterStatus> getOrderedUserTwitters(String user) {
        String sortOrder = COLUMN_NAME_CREATED + " DESC";

        String selection = COLUMN_NAME_FROM_USER + " = ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = new String[] { user };

        Cursor c = getReadableDatabase().query(
            TABLE_NAME, // The table to query
            projection, // The columns to return
            selection, // The columns for the WHERE clause
            selectionArgs, // The values for the WHERE clause
            null, // don't group the rows
            null, // don't filter by row groups
            sortOrder // The sort order
            );
        List<TwitterStatus> result = new ArrayList<TwitterStatus>();

        while (c.moveToNext()) {
            TwitterStatus t = new TwitterStatus();
            t.setId(c.getLong(c.getColumnIndex(COLUMN_NAME_ID)));
            t.setFromUser(c.getString(c.getColumnIndex(COLUMN_NAME_FROM_USER)));
            t.setCreated(new Date(c.getLong(c.getColumnIndex(COLUMN_NAME_CREATED)) * 1000));
            t.setProfileImageUrl(c.getString(c.getColumnIndex(COLUMN_NAME_IMAGE_URL)));
            t.setText(c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)));
            result.add(t);
        }
        c.close();
        getReadableDatabase().close();

        return result;
    }

    private ContentValues getContentValues(TwitterStatus twitter) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_ID, twitter.getId());
        cv.put(COLUMN_NAME_CREATED, twitter.getCreated().getTime() / 1000);
        cv.put(COLUMN_NAME_IMAGE_URL, twitter.getProfileImageUrl());
        cv.put(COLUMN_NAME_FROM_USER, twitter.getFromUser());
        cv.put(COLUMN_NAME_TEXT, twitter.getText());
        return cv;
    }

}
