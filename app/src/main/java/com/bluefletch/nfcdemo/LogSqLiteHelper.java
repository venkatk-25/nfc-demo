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
        Cursor cursor = this.getReadableDatabase().rawQuery("select logs from tappay_logs order by id desc limit 5", null);
//        cursor.moveToNext();
        String currLogs = "";
        int cursorCount = cursor.getCount();
        System.out.println("cursorCount=" + cursorCount);
//        System.out.println("cursorString=" + cursor.getString(0));
        int count = 0;
        if(cursorCount > 0 ) {
            while(cursor.moveToNext()) {
                if(count!=0) {
                    System.out.println("inside If");
                    currLogs = currLogs + "\n" + cursor.getString(0);
                    count++;
                }
                else {
                    System.out.println("inside else");
                    currLogs = cursor.getString(0);
                    count++;
                }
                System.out.println("currLogs=" + currLogs);
            }
        }
        cursor.close();
        System.out.println("LOGS=" + currLogs);
        return currLogs;
    }

    public void addLog(String newLog) {
//        String oldLog = getCurrentLogs();
//        String finalLog;
//        if(oldLog.equals("")) {
//            finalLog = "\"" + newLog + "\"";
//        }
//        else {
//            finalLog = "\"" + oldLog + "\n" + newLog + "\"";
//        }
        System.out.println("FINAL LOGS=" + newLog);
        this.getWritableDatabase().execSQL("insert into tappay_logs ( logs ) values(\"" + newLog + "\")");
    }
}
