package com.bluefletch.nfcdemo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LogSqLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "tappay_db_logs";
    public static final String TABLE_NAME = "tappay_logs";

    public LogSqLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( id INTEGER PRIMARY KEY AUTOINCREMENT, logs VARCHAR);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public String getCurrentLogs() {
        Cursor cursor = this.getReadableDatabase().rawQuery("select logs from tappay_logs order by id desc limit 1", null);
        cursor.moveToNext();
        String currLogs;
        if(cursor.getCount() > 0 ) {
            currLogs = cursor.getString(0);
        }
        else {
            currLogs = "";
        }
        System.out.println("LOGS=" + currLogs);
        return currLogs;
    }

    public void addLog(String newLog) {
        String oldLog = getCurrentLogs();
        String finalLog;
        if(oldLog.equals("")) {
            finalLog = "\"" + newLog + "\"";
        }
        else {
            finalLog = "\"" + oldLog + "\n" + newLog + "\"";
        }
        System.out.println("FINAL LOGS=" + finalLog);
        this.getWritableDatabase().execSQL("insert into tappay_logs ( logs ) values(" + finalLog + ")");
    }
}
