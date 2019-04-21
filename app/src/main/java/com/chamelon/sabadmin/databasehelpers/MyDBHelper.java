package com.chamelon.sabadmin.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import android.util.Log;

import com.chamelon.sabadmin.info.Info;

import java.util.ArrayList;
import java.util.List;

public class MyDBHelper extends SQLiteOpenHelper implements Info {

    private static final String COLUMN_ID = "_id";
    private static final String COL_TAG_NAME = "tagname";

    private static final String TABLE_TAGS = "tags";

    private static final String DATABASE_NAME = "sabadmin.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_TAGS =
            "create table "
                    + TABLE_TAGS
                    + "("
                    + COLUMN_ID
                    + " integer primary key autoincrement, "
                    + COL_TAG_NAME
                    + " TEXT NOT NULL);";

    public MyDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_TAGS);
    }


    public boolean addTag(String tagName) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TAG_NAME, tagName);
        if (db.insert(TABLE_TAGS, null, cv) == -1) {
            db.close();
            return false;
        }
        db.close();
        return true;
    }

    public void removeTag(String tagName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TAGS, COL_TAG_NAME + "=?", new String[]{tagName});
        db.close();
    }

    public boolean isTagSaved(String tagName) {

        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_TAGS + " where " + COL_TAG_NAME + " = " + "'" + tagName + "'" + ";", null);
        Log.v(TAG, "Query : " + "select * from " + TABLE_TAGS + " where " + COL_TAG_NAME + " = " + "'" + tagName + "'" + ";");
        int count = c.getCount();
        Log.v(TAG, "Cursor : " + count);
        c.close();
        db.close();
        return count > 0;
    }

    public List<String> getTags() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("select * from " + TABLE_TAGS + ";", null);
        List<String> tagList = new ArrayList<>();

        if (c != null) {
            if (c.moveToFirst()) {
                do {

                    tagList.add(c.getString(c.getColumnIndex(COL_TAG_NAME)));

                } while (c.moveToNext());
            }
        }

        return tagList;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
        onCreate(db);
    }
}