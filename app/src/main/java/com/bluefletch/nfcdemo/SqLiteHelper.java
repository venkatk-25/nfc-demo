package com.bluefletch.nfcdemo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "tappay_db";
    public static final String TABLE_NAME = "tappay";

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
        Cursor cursor = this.getReadableDatabase().rawQuery("select balance from tappay order by id desc limit 1", null);
        cursor.moveToNext();
        return (cursor.getCount() > 0 ?  cursor.getInt(0) : 0);
    }

    public void addBalance(int amount) {
        this.getWritableDatabase().execSQL("insert into tappay ( balance ) values( " + (getCurrentBalance() + amount) +")");
    }

//    public String getCurrentLogs() {
//        Cursor cursor = this.getReadableDatabase().rawQuery("select logs from tappay order by id desc limit 1", null);
//        cursor.moveToNext();
//        String currLogs;
//        System.out.println("cursor count=" + cursor.getCount());
//        System.out.println("cursor value=" + cursor.getString(0));
//        if(cursor.getCount() > 0 ) {
//            currLogs = cursor.getString(0);
//        }
//        else {
//            currLogs = "";
//        }
//        System.out.println("LOGS=" + currLogs);
//        return currLogs;
//    }
//
//    public void addLog(String newLog) {
//        String oldLog = getCurrentLogs();
//        String finalLog = "\"" + oldLog + newLog + "\"";
//        System.out.println("FINAL LOGS=" + finalLog);
//        this.getWritableDatabase().execSQL("insert into tappay ( logs ) values(" + finalLog + ")");
//    }
}