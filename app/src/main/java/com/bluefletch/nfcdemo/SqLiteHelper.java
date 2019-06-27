package com.bluefletch.nfcdemo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "santa_wallet_db";
    public static final String TABLE_NAME = "santa_wallet";

    public SqLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( id INTEGER PRIMARY KEY AUTOINCREMENT, balance INTEGER);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public int getCurrentBalance() {
        Cursor cursor = this.getReadableDatabase().rawQuery("select balance from santa_wallet order by id desc limit 1", null);
        cursor.moveToNext();
        return (cursor.getCount() > 0 ?  cursor.getInt(0) : 0);
    }

    public void addBalance(int amount) {
        this.getWritableDatabase().execSQL("insert into santa_wallet ( balance ) values( " + (getCurrentBalance() + amount) +")");
    }

}