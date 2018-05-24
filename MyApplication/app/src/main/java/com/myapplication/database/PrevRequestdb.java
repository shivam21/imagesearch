package com.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.myapplication.utils.Url;

public class PrevRequestdb extends SQLiteOpenHelper {
    private SQLiteDatabase database;
    private String table = "prevreq";


    public PrevRequestdb(Context context) {
        super(context, Url.appfolder + "/ImageSearch.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE table prevreq(reqid INTEGER PRIMARY KEY AUTOINCREMENT,page INTEGER,requestquery text,response text)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addData(int page, String query, String response) {
        database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + table + " WHERE page=" + page + " AND requestquery='" + query + "'", null);
        if (cursor == null || cursor.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("page", page);
            contentValues.put("requestquery", query);
            contentValues.put("response", response);
            database.insert(table, null, contentValues);
        }

    }

    public Cursor getData(int page, String query) {
        database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT response FROM " + table + " WHERE page=" + page + " AND requestquery='" + query + "'", null);
        return cursor;
    }
}
